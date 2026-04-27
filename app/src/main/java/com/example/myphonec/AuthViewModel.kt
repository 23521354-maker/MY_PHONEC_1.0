package com.example.myphonec

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthState(
    val isLoggedIn: Boolean = false,
    val uid: String? = null,
    val userName: String? = null,
    val userEmail: String? = null,
    val photoUrl: String? = null,
    val isGuest: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null
)

class AuthViewModel(
    private val sessionManager: SessionManager,
    private val firebaseRepository: FirebaseRepository
) : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    init {
        initializeAuthState()
    }

    private fun initializeAuthState() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Initializing auth state from DataStore...")
                val savedState = sessionManager.userSession.first()
                _authState.value = savedState.copy(isLoading = false)
                
                sessionManager.userSession.collect { updatedState ->
                    if (!_authState.value.isLoading) {
                        _authState.update { current ->
                            updatedState.copy(isLoading = current.isLoading)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error initializing auth state", e)
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        if (_authState.value.isLoading) return
        
        viewModelScope.launch {
            Log.d(TAG, "Google Sign-in started")
            _authState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Server Client ID (client_type: 3) từ google-services.json
                val serverClientId = "272304530561-girvp4jvd6g7ol01mr1h38umakpumof3.apps.googleusercontent.com"
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false) // Hiển thị tất cả tài khoản
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(false) // Tắt tự động chọn để tránh lỗi khi có nhiều tài khoản
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager error: ${e.type} - ${e.message}", e)
                val userFriendlyMessage = when (e.type) {
                    "android.credentials.GetCredentialException.TYPE_USER_CANCELED" -> "Đăng nhập đã bị hủy."
                    "android.credentials.GetCredentialException.TYPE_NO_CREDENTIAL" -> "Không tìm thấy tài khoản Google. Vui lòng kiểm tra cài đặt thiết bị."
                    else -> "Lỗi đăng nhập Google: ${e.message}"
                }
                _authState.update { it.copy(isLoading = false, error = userFriendlyMessage) }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected login error", e)
                _authState.update { it.copy(isLoading = false, error = "Đã xảy ra lỗi: ${e.localizedMessage}") }
            }
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            val googleIdTokenCredential = when {
                credential is GoogleIdTokenCredential -> credential
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    GoogleIdTokenCredential.createFrom(credential.data)
                }
                else -> null
            }

            if (googleIdTokenCredential != null) {
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                
                if (user != null) {
                    val uid = user.uid
                    val name = user.displayName ?: "User"
                    val email = user.email ?: ""
                    val photo = user.photoUrl?.toString()

                    try {
                        firebaseRepository.saveOrUpdateUser(uid, name, email, photo)
                    } catch (e: Exception) {
                        Log.e(TAG, "Firestore sync failed", e)
                    }

                    sessionManager.saveSession(uid, name, email, photo)
                    
                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            isGuest = false,
                            uid = uid,
                            userName = name,
                            userEmail = email,
                            photoUrl = photo,
                            isLoading = false,
                            error = null
                        )
                    }
                } else {
                    _authState.update { it.copy(isLoading = false, error = "Lỗi xác thực Firebase.") }
                }
            } else {
                _authState.update { it.copy(isLoading = false, error = "Phương thức đăng nhập không được hỗ trợ.") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth error", e)
            _authState.update { it.copy(isLoading = false, error = "Đăng nhập thất bại: ${e.localizedMessage}") }
        }
    }

    fun onContinueAsGuest() {
        if (_authState.value.isLoading) return
        viewModelScope.launch {
            _authState.update { it.copy(isLoading = true, error = null) }
            try {
                sessionManager.saveGuestSession()
                _authState.update {
                    it.copy(
                        isLoggedIn = false,
                        isGuest = true,
                        userName = "Guest User",
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _authState.update { it.copy(isLoading = false, error = "Không thể vào chế độ khách") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                sessionManager.clearSession()
                _authState.update { AuthState(isLoading = false) }
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
            }
        }
    }
}
