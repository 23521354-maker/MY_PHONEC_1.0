package com.example.myphonec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class UserProfileUiState(
    val benchmarkedDevices: List<BenchmarkedDevice> = emptyList(),
    val totalDevices: Int = 0,
    val highestScore: Int = 0,
    val isLoading: Boolean = false
)

class UserProfileViewModel(
    private val repository: UserBenchmarkRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    init {
        observeBenchmarks()
    }

    private fun observeBenchmarks() {
        viewModelScope.launch {
            authViewModel.authState.collectLatest { authState ->
                val uid = authState.uid
                if (uid != null) {
                    _uiState.update { it.copy(isLoading = true) }
                    repository.getUserBenchmarks(uid).collect { devices ->
                        _uiState.update { state ->
                            state.copy(
                                benchmarkedDevices = devices,
                                totalDevices = devices.size,
                                highestScore = devices.maxOfOrNull { it.score } ?: 0,
                                isLoading = false
                            )
                        }
                    }
                } else {
                    _uiState.update { UserProfileUiState() }
                }
            }
        }
    }
}
