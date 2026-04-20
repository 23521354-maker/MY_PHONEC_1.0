package com.example.myphonec

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BenchmarkResult(
    val deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val gpuName: String = "OpenGL ES 3.0",
    val averageFps: Int = 0,
    val minFps: Int = 0,
    val maxFps: Int = 0,
    val stability: Int = 0,
    val score: Int = 0,
    val tier: String = ""
)

data class BenchmarkState(
    val isRunning: Boolean = false,
    val isCountingDown: Boolean = false,
    val countdownValue: Int = 0,
    val progress: Float = 0f,
    val currentFps: Int = 0,
    val result: BenchmarkResult? = null,
    val status: String = "Ready to start",
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null
)

class BenchmarkViewModel(
    private val firebaseRepository: FirebaseRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {
    private val _benchmarkState = MutableStateFlow(BenchmarkState())
    val benchmarkState: StateFlow<BenchmarkState> = _benchmarkState.asStateFlow()

    private var benchmarkJob: Job? = null
    private val fpsList = mutableListOf<Int>()
    private var isResultUploaded = false

    fun startBenchmark() {
        if (_benchmarkState.value.isRunning || _benchmarkState.value.isCountingDown) return
        isResultUploaded = false

        benchmarkJob = viewModelScope.launch {
            // Phase 1: Countdown
            _benchmarkState.update { it.copy(isCountingDown = true, result = null, uploadSuccess = false, error = null) }
            for (i in 3 downTo 1) {
                _benchmarkState.update { it.copy(countdownValue = i) }
                delay(1000)
            }
            
            // Phase 2: Start
            _benchmarkState.update { it.copy(isCountingDown = false, isRunning = true, status = "Benchmarking...", progress = 0f) }
            fpsList.clear()
            
            val duration = 30000L // 30 seconds
            val startTime = System.currentTimeMillis()
            
            while (System.currentTimeMillis() - startTime < duration) {
                val elapsed = System.currentTimeMillis() - startTime
                _benchmarkState.update { it.copy(progress = elapsed.toFloat() / duration) }
                delay(500) // Update UI progress
            }
            
            stopBenchmark()
        }
    }

    fun updateFps(fps: Int) {
        if (_benchmarkState.value.isRunning) {
            fpsList.add(fps)
            _benchmarkState.update { it.copy(currentFps = fps) }
        }
    }

    private fun stopBenchmark() {
        _benchmarkState.update { it.copy(isRunning = false, progress = 1f, status = "Finished") }
        calculateResult()
    }

    private fun calculateResult() {
        if (fpsList.isEmpty()) return
        
        val avgFps = fpsList.average().toInt()
        val minFps = fpsList.minOrNull() ?: 0
        val maxFps = fpsList.maxOrNull() ?: 0
        val score = avgFps * 1500
        
        val stability = if (avgFps > 0) {
            val stableFrames = fpsList.count { it >= avgFps * 0.8 && it <= avgFps * 1.2 }
            (stableFrames.toFloat() / fpsList.size * 100).toInt()
        } else 0

        val tier = when {
            score >= 80000 -> "Flagship"
            score >= 60000 -> "High-End"
            score >= 40000 -> "Midrange"
            else -> "Entry Level"
        }

        val result = BenchmarkResult(
            averageFps = avgFps,
            minFps = minFps,
            maxFps = maxFps,
            stability = stability,
            score = score,
            tier = tier
        )

        _benchmarkState.update { it.copy(result = result) }
        
        // Auto-upload if logged in
        uploadResult(result)
    }

    fun uploadResult(result: BenchmarkResult) {
        val auth = authViewModel.authState.value
        if (!auth.isLoggedIn || auth.uid == null || isResultUploaded) return

        viewModelScope.launch {
            _benchmarkState.update { it.copy(isUploading = true) }
            try {
                firebaseRepository.saveBenchmarkResult(
                    uid = auth.uid,
                    userName = auth.userName ?: "Anonymous",
                    deviceModel = result.deviceName,
                    chipset = result.gpuName, // Or use a proper chipset detection
                    score = result.score,
                    fps = result.averageFps
                )
                isResultUploaded = true
                _benchmarkState.update { it.copy(isUploading = false, uploadSuccess = true) }
            } catch (e: Exception) {
                _benchmarkState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun reset() {
        _benchmarkState.update { BenchmarkState() }
        isResultUploaded = false
    }
}
