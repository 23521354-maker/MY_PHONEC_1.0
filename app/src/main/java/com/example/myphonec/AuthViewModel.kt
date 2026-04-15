package com.example.myphonec

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AuthState(
    val isLoggedIn: Boolean = false,
    val userName: String? = null,
    val userEmail: String? = null,
    val isGuest: Boolean = false
)

class AuthViewModel : ViewModel() {
    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun onSignInSuccess(name: String, email: String) {
        _authState.update {
            it.copy(
                isLoggedIn = true,
                userName = name,
                userEmail = email,
                isGuest = false
            )
        }
    }

    fun onContinueAsGuest() {
        _authState.update {
            it.copy(
                isLoggedIn = false,
                userName = "Guest User",
                isGuest = true
            )
        }
    }

    fun signOut() {
        _authState.update {
            AuthState()
        }
    }
}
