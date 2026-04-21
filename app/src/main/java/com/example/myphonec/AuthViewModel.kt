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
                // Read the first emission to set initial state before UI starts navigating
                val savedState = sessionManager.userSession.first()
                _authState.value = savedState.copy(isLoading = false)
                Log.d(TAG, "Initial auth state: isLoggedIn=${savedState.isLoggedIn}, isGuest=${savedState.isGuest}")
                
                // Then continue collecting updates
                sessionManager.userSession.collect { updatedState ->
                    // Only update from background if we're not in the middle of a transition
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
                Log.e(TAG, "Credential Manager error: ${e.type}", e)
                _authState.update { it.copy(isLoading = false, error = "Google Sign-In failed: ${e.message}") }
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected login error", e)
                _authState.update { it.copy(isLoading = false, error = "An error occurred: ${e.localizedMessage}") }
            }
        }
    }

    private suspend fun handleSignInResult(result: GetCredentialResponse) {
        try {
            val credential = result.credential
            Log.d(TAG, "Received credential type: ${credential.type}")

            val googleIdTokenCredential = when {
                credential is GoogleIdTokenCredential -> {
                    Log.d(TAG, "Direct GoogleIdTokenCredential match")
                    credential
                }
                credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                    Log.d(TAG, "CustomCredential match for Google ID Token")
                    GoogleIdTokenCredential.createFrom(credential.data)
                }
                else -> {
                    Log.e(TAG, "Unsupported credential type: ${credential.type}")
                    null
                }
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

                    // Save session first, then update UI state
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
                    Log.d(TAG, "Google Sign-in fully completed")
                } else {
                    _authState.update { it.copy(isLoading = false, error = "Firebase user is null") }
                }
            } else {
                _authState.update { it.copy(isLoading = false, error = "This login method is not supported yet.") }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth error", e)
            _authState.update { it.copy(isLoading = false, error = "Auth failed: ${e.localizedMessage}") }
        }
    }

    fun onContinueAsGuest() {
        if (_authState.value.isLoading) return
        
        viewModelScope.launch {
            Log.d(TAG, "Setting up Guest session...")
            _authState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 1. Persist to DataStore
                sessionManager.saveGuestSession()
                
                // 2. Update UI State
                _authState.update {
                    it.copy(
                        isLoggedIn = false,
                        isGuest = true,
                        userName = "Guest User",
                        uid = null,
                        userEmail = null,
                        photoUrl = null,
                        isLoading = false,
                        error = null
                    )
                }
                Log.d(TAG, "Guest session active")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to save guest session", e)
                _authState.update { it.copy(isLoading = false, error = "Could not enter guest mode") }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                sessionManager.clearSession()
                _authState.update { AuthState(isLoading = false) }
                Log.d(TAG, "Sign out successful")
            } catch (e: Exception) {
                Log.e(TAG, "Sign out failed", e)
            }
        }
    }
}
