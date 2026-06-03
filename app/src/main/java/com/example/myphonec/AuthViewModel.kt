package com.example.myphonec

import android.content.Context
import android.util.Log
import androidx.credentials.CustomCredential
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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

            val credentialManager = CredentialManager.create(context)
            val serverClientId = context.getString(R.string.default_web_client_id)

            try {
                // Bước 1: Thử lấy tài khoản đã ủy quyền trước (One-Tap silent).
                val authorizedOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(true)
                    .setServerClientId(serverClientId)
                    .setAutoSelectEnabled(true)
                    .build()
                val authorizedRequest = GetCredentialRequest.Builder()
                    .addCredentialOption(authorizedOption)
                    .build()
                val result = credentialManager.getCredential(context, authorizedRequest)
                handleSignInResult(result)
            } catch (e: NoCredentialException) {
                // Bước 2: Không có tài khoản đã ủy quyền → mở account picker tường minh.
                Log.d(TAG, "No authorized account found, falling back to explicit Sign-in With Google flow")
                try {
                    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId).build()
                    val signInRequest = GetCredentialRequest.Builder()
                        .addCredentialOption(signInOption)
                        .build()
                    val result = credentialManager.getCredential(context, signInRequest)
                    handleSignInResult(result)
                } catch (e2: GetCredentialException) {
                    handleCredentialException(e2)
                } catch (e2: Exception) {
                    Log.e(TAG, "Unexpected error in fallback sign-in", e2)
                    _authState.update {
                        it.copy(isLoading = false, error = "Đã xảy ra lỗi: ${e2.localizedMessage}")
                    }
                }
            } catch (e: GetCredentialException) {
                handleCredentialException(e)
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected login error", e)
                _authState.update {
                    it.copy(isLoading = false, error = "Đã xảy ra lỗi: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun handleCredentialException(e: GetCredentialException) {
        Log.e(TAG, "Credential Manager error: type=${e.type}, message=${e.message}", e)
        val userFriendlyMessage = when {
            e is NoCredentialException ->
                "Không tìm thấy tài khoản Google trên thiết bị. Vui lòng vào Cài đặt → Tài khoản → thêm tài khoản Google rồi thử lại."
            e.type.contains("USER_CANCELED", ignoreCase = true) ->
                "Đăng nhập đã bị hủy."
            e.type.contains("INTERRUPTED", ignoreCase = true) ->
                "Đăng nhập bị gián đoạn. Vui lòng thử lại."
            e.type.contains("PROVIDER_CONFIGURATION", ignoreCase = true) ->
                "Google Play Services chưa sẵn sàng. Vui lòng cập nhật Google Play Services."
            else -> "Lỗi đăng nhập Google: ${e.message}"
        }
        _authState.update { it.copy(isLoading = false, error = userFriendlyMessage) }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            val googleIdTokenCredential = when {
                credential is GoogleIdTokenCredential -> credential
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    try {
                        GoogleIdTokenCredential.createFrom(credential.data)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Failed to parse Google ID token", e)
                        null
                    }
                }
                else -> {
                    Log.e(TAG, "Unsupported credential type: ${credential::class.java.name}")
                    null
                }
            }

            if (googleIdTokenCredential != null) {
                val idToken = googleIdTokenCredential.idToken
                if (idToken.isBlank()) {
                    Log.e(TAG, "ID token is blank")
                    _authState.update {
                        it.copy(isLoading = false, error = "Không nhận được ID token từ Google.")
                    }
                    return
                }
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
