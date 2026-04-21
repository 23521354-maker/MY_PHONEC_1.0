package com.example.myphonec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminUiState(
    val isLoading: Boolean = false,
    val message: String = "",
    val progress: String = ""
)

class AdminViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    private val gson = Gson()

    fun importCPUs(json: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Parsing JSON...", progress = "") }
            try {
                val type = object : TypeToken<List<CPU>>() {}.type
                val cpus: List<CPU> = gson.fromJson(json, type)
                
                _uiState.update { it.copy(message = "Importing ${cpus.size} CPUs...") }
                
                cpus.chunked(400).forEachIndexed { index, chunk ->
                    repository.bulkImportCPUs(chunk)
                    _uiState.update { it.copy(progress = "Batch ${index + 1} uploaded") }
                }
                
                _uiState.update { it.copy(isLoading = false, message = "Successfully imported ${cpus.size} CPUs") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "Import failed: ${e.message}") }
            }
        }
    }

    fun importGPUs(json: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Parsing JSON...", progress = "") }
            try {
                val type = object : TypeToken<List<GPU>>() {}.type
                val gpus: List<GPU> = gson.fromJson(json, type)
                
                _uiState.update { it.copy(message = "Importing ${gpus.size} GPUs...") }
                
                gpus.chunked(400).forEachIndexed { index, chunk ->
                    repository.bulkImportGPUs(chunk)
                    _uiState.update { it.copy(progress = "Batch ${index + 1} uploaded") }
                }
                
                _uiState.update { it.copy(isLoading = false, message = "Successfully imported ${gpus.size} GPUs") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "Import failed: ${e.message}") }
            }
        }
    }

    fun clearCPUs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Clearing CPUs...") }
            try {
                repository.clearCollection("cpus")
                _uiState.update { it.copy(isLoading = false, message = "CPUs collection cleared") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "Clear failed: ${e.message}") }
            }
        }
    }

    fun clearGPUs() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = "Clearing GPUs...") }
            try {
                repository.clearCollection("gpus")
                _uiState.update { it.copy(isLoading = false, message = "GPUs collection cleared") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, message = "Clear failed: ${e.message}") }
            }
        }
    }
}
