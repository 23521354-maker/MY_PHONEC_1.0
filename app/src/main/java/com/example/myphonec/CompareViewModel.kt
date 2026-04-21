package com.example.myphonec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CompareUiState(
    val cpus: List<CPU> = emptyList(),
    val gpus: List<GPU> = emptyList(),
    val selectedCpuA: CPU? = null,
    val selectedCpuB: CPU? = null,
    val selectedGpuA: GPU? = null,
    val selectedGpuB: GPU? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class CompareViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(CompareUiState())
    val uiState: StateFlow<CompareUiState> = _uiState.asStateFlow()

    init {
        loadHardwareData()
    }

    private fun loadHardwareData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val cpus = repository.getCPUs()
                val gpus = repository.getGPUs()
                _uiState.update { 
                    it.copy(
                        cpus = cpus, 
                        gpus = gpus, 
                        isLoading = false,
                        selectedCpuA = cpus.getOrNull(0),
                        selectedCpuB = cpus.getOrNull(1),
                        selectedGpuA = gpus.getOrNull(0),
                        selectedGpuB = gpus.getOrNull(1)
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Unable to load hardware data") }
            }
        }
    }

    fun selectCpuA(cpuName: String) {
        val cpu = _uiState.value.cpus.find { it.name == cpuName }
        _uiState.update { it.copy(selectedCpuA = cpu) }
    }

    fun selectCpuB(cpuName: String) {
        val cpu = _uiState.value.cpus.find { it.name == cpuName }
        _uiState.update { it.copy(selectedCpuB = cpu) }
    }

    fun selectGpuA(gpuName: String) {
        val gpu = _uiState.value.gpus.find { it.name == gpuName }
        _uiState.update { it.copy(selectedGpuA = gpu) }
    }

    fun selectGpuB(gpuName: String) {
        val gpu = _uiState.value.gpus.find { it.name == gpuName }
        _uiState.update { it.copy(selectedGpuB = gpu) }
    }
}
