package com.example.myphonec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PhoneRankItem(
    val rank: Int,
    val name: String,
    val chipset: String,
    val score: Int,
    val fps: Int = 0,
    val userName: String? = null
)

data class LeaderboardUiState(
    val items: List<PhoneRankItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class LeaderboardViewModel(
    private val repository: LeaderboardRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        observeLeaderboard()
    }

    fun observeLeaderboard() {
        _uiState.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            repository.getTopScores()
                .catch { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "Unknown error occurred") }
                }
                .collect { items ->
                    _uiState.update { it.copy(items = items, isLoading = false) }
                }
        }
    }
}
