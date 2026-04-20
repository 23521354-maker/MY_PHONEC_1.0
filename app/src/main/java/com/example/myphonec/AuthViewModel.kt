package com.example.myphonec

import android.content.Context
import android.util.Log
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
        checkInitialSession()
    }

    private fun checkInitialSession() {
        viewModelScope.launch {
            try {
                sessionManager.userSession.collect { savedState ->
                    // Only update if we are not already in a specific state (like loading from a sign-in)
                    if (!_authState.value.isLoggedIn) {
                        _authState.update { savedState.copy(isLoading = false) }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error checking initial session", e)
                _authState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        if (_authState.value.isLoading) return
        
        viewModelScope.launch {
            Log.d(TAG, "Sign in started")
            _authState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val credentialManager = CredentialManager.create(context)
                val serverClientId = "272304530561-girvp4jvd6g7ol01mr1h38umakpumof3.apps.googleusercontent.com"
                
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(serverClientId) 
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                handleSignInResult(result)
            } catch (e: GetCredentialException) {
                Log.e(TAG, "Credential Manager error", e)
                _authState.update { it.copy(isLoading = false, error = "Google Sign-In failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected login error", e)
                _authState.update { it.copy(isLoading = false, error = "An error occurred: ${e.localizedMessage ?: "Unknown error"}") }
            }
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            if (credential is GoogleIdTokenCredential) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user
                
                if (user != null) {
                    Log.d(TAG, "Firebase Auth success: ${user.uid}")
                    
                    val uid = user.uid
                    val name = user.displayName ?: "User"
                    val email = user.email ?: ""
                    val photo = user.photoUrl?.toString()

                    // 1. Sync to Firestore (Crucial: try/catch this specifically)
                    try {
                        firebaseRepository.saveOrUpdateUser(uid, name, email, photo)
                        Log.d(TAG, "Firestore sync success")
                    } catch (e: Exception) {
                        Log.e(TAG, "Firestore sync failed but continuing", e)
                    }

                    // 2. Save local session
                    sessionManager.saveSession(uid, name, email, photo)
                    Log.d(TAG, "Local session saved")

                    // 3. Update UI state (This triggers navigation)
                    _authState.update {
                        it.copy(
                            isLoggedIn = true,
                            uid = uid,
                            userName = name,
                            userEmail = email,
                            photoUrl = photo,
                            isLoading = false,
                            error = null
                        )
                    }
                    Log.d(TAG, "Auth state updated - Navigation should trigger")
                } else {
                    _authState.update { it.copy(isLoading = false, error = "User object is null after sign-in") }
                }
            } else {
                Log.e(TAG, "Unexpected credential type: ${credential.type}")
                _authState.update { it.copy(isLoading = false, error = "Unexpected credential type") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth error", e)
            _authState.update { it.copy(isLoading = false, error = "Firebase Auth failed: ${e.localizedMessage}") }
        }
    }

    fun onContinueAsGuest() {
        Log.d(TAG, "Continuing as Guest")
        _authState.update {
            it.copy(
                isLoggedIn = false,
                userName = "Guest User",
                isGuest = true,
                isLoading = false,
                error = null
            )
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                sessionManager.clearSession()
                _authState.update { AuthState(isLoading = false) }
                Log.d(TAG, "Signed out successfully")
            } catch (e: Exception) {
                Log.e(TAG, "Sign out error", e)
            }
        }
    }
}
