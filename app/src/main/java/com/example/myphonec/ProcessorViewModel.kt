package com.example.myphonec

import android.app.Application
import android.os.Build
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class CpuCoreInfo(
    val id: Int,
    val currentFreq: String,
    val maxFreq: String,
    val usage: Float
)

data class ProcessorState(
    val model: String = "Loading...",
    val cores: Int = 0,
    val architecture: String = "Unknown",
    val maxFreq: String = "Unknown",
    val minFreq: String = "Unknown",
    val gpuVendor: String = "Unknown",
    val gpuRenderer: String = "Unknown",
    val coreDetails: List<CpuCoreInfo> = emptyList(),
    val hardware: String = "Unknown",
    val revision: String = "Unknown",
    val isLoading: Boolean = true
)

class ProcessorViewModel(application: Application) : AndroidViewModel(application) {

    private val _processorState = MutableStateFlow(ProcessorState())
    val processorState: StateFlow<ProcessorState> = _processorState.asStateFlow()

    private var liveUpdateJob: Job? = null

    init {
        loadProcessorData()
    }

    private fun loadProcessorData() {
        viewModelScope.launch {
            _processorState.update { it.copy(isLoading = true) }
            
            // Fetch static info in background
            val staticInfo = withContext(Dispatchers.IO) {
                fetchStaticInfo()
            }
            
            _processorState.update { 
                it.copy(
                    model = staticInfo.model,
                    cores = staticInfo.cores,
                    architecture = staticInfo.architecture,
                    maxFreq = staticInfo.maxFreq,
                    minFreq = staticInfo.minFreq,
                    hardware = staticInfo.hardware,
                    revision = staticInfo.revision,
                    isLoading = false
                )
            }
            
            startLiveUpdates()
        }
    }

    private fun fetchStaticInfo(): StaticProcessorInfo {
        val cores = Runtime.getRuntime().availableProcessors()
        val architecture = Build.SUPPORTED_ABIS.joinToString(", ")
        
        var hardware = "Unknown"
        var revision = "Unknown"
        var model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            Build.HARDWARE
        } ?: "Unknown"

        try {
            File("/proc/cpuinfo").forEachLine { line ->
                when {
                    line.contains("Hardware", ignoreCase = true) -> {
                        val parts = line.split(":")
                        if (parts.size > 1) hardware = parts[1].trim()
                    }
                    line.contains("Revision", ignoreCase = true) -> {
                        val parts = line.split(":")
                        if (parts.size > 1) revision = parts[1].trim()
                    }
                    line.contains("model name", ignoreCase = true) && (model == "Unknown" || model == Build.HARDWARE) -> {
                        val parts = line.split(":")
                        if (parts.size > 1) model = parts[1].trim()
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val maxFreqStr = getCpuMaxFreq()
        val minFreqStr = getCpuMinFreq()

        return StaticProcessorInfo(model, cores, architecture, maxFreqStr, minFreqStr, hardware, revision)
    }

    private data class StaticProcessorInfo(
        val model: String,
        val cores: Int,
        val architecture: String,
        val maxFreq: String,
        val minFreq: String,
        val hardware: String,
        val revision: String
    )

    fun startLiveUpdates() {
        if (liveUpdateJob?.isActive == true) return
        
        liveUpdateJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive) {
                val coreInfos = mutableListOf<CpuCoreInfo>()
                val numCores = Runtime.getRuntime().availableProcessors()

                for (i in 0 until numCores) {
                    val current = getCoreCurrentFreq(i)
                    val max = getCoreMaxFreq(i)
                    
                    val currentLong = current.toLongOrNull() ?: 0L
                    val maxLong = max.toLongOrNull() ?: 0L
                    
                    val usage = if (maxLong > 0) {
                        currentLong.toFloat() / maxLong.toFloat()
                    } else 0f

                    coreInfos.add(
                        CpuCoreInfo(
                            id = i,
                            currentFreq = if (currentLong > 0) "$currentLong MHz" else "Offline",
                            maxFreq = "$maxLong MHz",
                            usage = usage.coerceIn(0f, 1f)
                        )
                    )
                }

                _processorState.update { it.copy(coreDetails = coreInfos) }
                delay(1000) // 1 second update rate for CPU cores
            }
        }
    }

    fun stopLiveUpdates() {
        liveUpdateJob?.cancel()
    }

    fun updateGpuInfo(vendor: String, renderer: String) {
        _processorState.update {
            it.copy(
                gpuVendor = vendor,
                gpuRenderer = renderer
            )
        }
    }

    private fun getCoreCurrentFreq(coreIndex: Int): String {
        return try {
            val path = "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/scaling_cur_freq"
            val file = File(path)
            if (file.exists()) {
                val freq = file.readText().trim().toLong()
                (freq / 1000).toString()
            } else "0"
        } catch (e: Exception) {
            "0"
        }
    }

    private fun getCoreMaxFreq(coreIndex: Int): String {
        return try {
            val path = "/sys/devices/system/cpu/cpu$coreIndex/cpufreq/cpuinfo_max_freq"
            val file = File(path)
            if (file.exists()) {
                val freq = file.readText().trim().toLong()
                (freq / 1000).toString()
            } else "0"
        } catch (e: Exception) {
            "0"
        }
    }

    private fun getCpuMaxFreq(): String {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq")
            if (freqFile.exists()) {
                val freq = freqFile.readText().trim().toLong()
                "%.2f GHz".format(freq / 1000000.0)
            } else "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    private fun getCpuMinFreq(): String {
        return try {
            val freqFile = File("/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq")
            if (freqFile.exists()) {
                val freq = freqFile.readText().trim().toLong()
                "${freq / 1000} MHz"
            } else "Unknown"
        } catch (e: Exception) {
            "Unknown"
        }
    }
}
