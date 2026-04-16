package com.example.myphonec

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs

data class BottleneckUIState(
    val selectedCpu: CPUData? = ComparisonData.cpus[0],
    val selectedGpu: GPUData? = ComparisonData.gpus[0],
    val selectedResolution: Resolution = Resolution.R1080P,
    val result: BottleneckResult? = null,
    val isCalculating: Boolean = false
)

class BottleneckViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(BottleneckUIState())
    val uiState: StateFlow<BottleneckUIState> = _uiState.asStateFlow()

    fun onCpuSelected(cpu: CPUData) {
        _uiState.update { it.copy(selectedCpu = cpu) }
    }

    fun onGpuSelected(gpu: GPUData) {
        _uiState.update { it.copy(selectedGpu = gpu) }
    }

    fun onResolutionSelected(resolution: Resolution) {
        _uiState.update { it.copy(selectedResolution = resolution) }
    }

    fun calculateBottleneck() {
        val currentState = _uiState.value
        val cpu = currentState.selectedCpu ?: return
        val gpu = currentState.selectedGpu ?: return
        val res = currentState.selectedResolution

        _uiState.update { it.copy(isCalculating = true) }

        // Logic: Calculate ratio based on performance scores
        // CPU matters more at 1080p, GPU matters more at 4K
        val cpuPower = cpu.performanceScore
        val gpuPower = gpu.performanceScore / res.gpuWeight

        val ratio = cpuPower / gpuPower
        val diffPercentage = (abs(1.0f - ratio) * 100).toInt().coerceIn(0, 100)

        val (status, description) = when {
            ratio > 1.15f -> {
                // CPU is much stronger -> GPU Bottleneck
                val gpuL = 100
                val cpuL = (100 / ratio).toInt()
                Triple("GPU BOTTLENECK", 0xfff87171, "Your GPU is significantly limiting your CPU's potential at this resolution.") to Pair(cpuL, gpuL)
            }
            ratio < 0.85f -> {
                // GPU is much stronger -> CPU Bottleneck
                val cpuL = 100
                val gpuL = (100 * ratio).toInt()
                Triple("CPU BOTTLENECK", 0xfffbbf24, "Your CPU is limiting your GPU performance. Consider a faster processor or higher resolution.") to Pair(cpuL, gpuL)
            }
            else -> {
                // Balanced
                Triple("OPTIMAL", 0xff2ff801, "Your CPU and GPU are well balanced. No significant performance loss expected.") to Pair(90, 98)
            }
        }

        val result = BottleneckResult(
            percentage = diffPercentage,
            status = status.first,
            statusColor = status.second,
            description = status.third,
            cpuLoad = description.first,
            gpuLoad = description.second
        )

        _uiState.update { it.copy(result = result, isCalculating = false) }
    }
}
