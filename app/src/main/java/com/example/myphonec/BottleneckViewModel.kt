package com.example.myphonec

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class BottleneckUIState(
    val cpus: List<CPU> = emptyList(),
    val gpus: List<GPU> = emptyList(),
    val selectedCpu: CPU? = null,
    val selectedGpu: GPU? = null,
    val selectedResolution: Resolution = Resolution.R1080P,
    val result: BottleneckResult? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

class BottleneckViewModel(private val repository: FirebaseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(BottleneckUIState())
    val uiState: StateFlow<BottleneckUIState> = _uiState.asStateFlow()

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
                        selectedCpu = cpus.getOrNull(0),
                        selectedGpu = gpus.getOrNull(0)
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Unable to load hardware database") }
            }
        }
    }

    fun onCpuSelected(cpuName: String) {
        val cpu = _uiState.value.cpus.find { it.name == cpuName }
        _uiState.update { it.copy(selectedCpu = cpu) }
    }

    fun onGpuSelected(gpuName: String) {
        val gpu = _uiState.value.gpus.find { it.name == gpuName }
        _uiState.update { it.copy(selectedGpu = gpu) }
    }

    fun onResolutionSelected(resolution: Resolution) {
        _uiState.update { it.copy(selectedResolution = resolution) }
    }

    fun calculateBottleneck() {
        val state = _uiState.value
        val cpu = state.selectedCpu ?: return
        val gpu = state.selectedGpu ?: return
        val res = state.selectedResolution

        // --- NEW CORRECT SYSTEM ---

        // Step 1: Alignment of score scales using Resolution-based CPU_WEIGHT
        val cpuWeight = when(res) {
            Resolution.R1080P -> 3.5f
            Resolution.R1440P -> 3.2f
            Resolution.R4K -> 2.6f
        }
        val gpuWeight = 1.0f

        val cpuPower = cpu.score * cpuWeight
        val gpuPower = gpu.score * gpuWeight

        // Step 2: Difference calculation
        val difference = abs(cpuPower - gpuPower)
        val average = (cpuPower + gpuPower) / 2.0
        
        val rawPercent = if (average > 0) (difference / average) * 100.0 else 0.0

        // Step 3: Soften and Clamp
        var bottleneckPercent = (rawPercent * 0.35).roundToInt()
        bottleneckPercent = bottleneckPercent.coerceIn(0, 45)

        // Status and Interpretation
        val (status, statusColor) = when {
            bottleneckPercent <= 5 -> "EXCELLENT BALANCE" to 0xff2ff801
            bottleneckPercent <= 12 -> "VERY GOOD" to 0xff00e5ff
            bottleneckPercent <= 20 -> "MINOR BOTTLENECK" to 0xfffbbf24
            bottleneckPercent <= 30 -> "NOTICEABLE BOTTLENECK" to 0xffffa500
            else -> "SEVERE BOTTLENECK" to 0xfff87171
        }

        // Direction Rule
        val direction = if (cpuPower < gpuPower) "CPU bottleneck" else "GPU bottleneck"
        
        val description = when {
            bottleneckPercent <= 5 -> "Your CPU and GPU are perfectly balanced for this resolution."
            bottleneckPercent <= 12 -> "Your system has a very good balance with minimal performance loss."
            cpuPower < gpuPower -> "Your CPU is slightly underpowered for this GPU, which may limit maximum FPS."
            else -> "Your GPU is the limiting factor in this configuration, which is common at higher resolutions."
        }

        // Load Visualization (Calculated for UI feedback)
        val cpuLoad = if (cpuPower < gpuPower) 95 else (100 - bottleneckPercent).coerceAtLeast(60)
        val gpuLoad = if (gpuPower < cpuPower) 98 else (100 - bottleneckPercent).coerceAtLeast(60)

        val result = BottleneckResult(
            percentage = bottleneckPercent,
            status = status,
            statusColor = statusColor,
            description = description,
            cpuLoad = cpuLoad,
            gpuLoad = gpuLoad,
            direction = direction
        )

        _uiState.update { it.copy(result = result) }
    }
}
