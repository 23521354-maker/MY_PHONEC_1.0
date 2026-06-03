package com.example.myphonec

import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class BenchmarkPhase {
    IDLE,
    WARMUP,
    MEASUREMENT,
    SCORING,
    RESULT,
    ERROR,
}

data class FpsSample(val fps: Float, val timestampMs: Long)

data class BenchmarkResult(
    val deviceName: String = "${Build.MANUFACTURER} ${Build.MODEL}",
    val gpuName: String = "",
    val score: Int = 0,
    val averageFps: Float = 0f,
    val minFps: Float = 0f,
    val maxFps: Float = 0f,
    val p1LowFps: Float = 0f,
    /** 0..1 — how steady FPS was within the measurement window. */
    val stability: Float = 0f,
    /** 0..1 — lastSegmentFps / firstSegmentFps. <0.7 = severe thermal throttle. */
    val sustainedRatio: Float = 0f,
    val firstSegFps: Float = 0f,
    val lastSegFps: Float = 0f,
    val totalFrames: Long = 0L,
)

data class SystemUsage(
    val cpuPercent: Int = 0,
    val gpuPercent: Int = 0,
    val ramPercent: Int = 0,
)

data class BenchmarkState(
    val phase: BenchmarkPhase = BenchmarkPhase.IDLE,
    val progress: Float = 0f,
    val remainingSeconds: Int = 0,
    val currentFps: Int = 0,
    val averageFps: Float = 0f,
    val minFps: Int = 0,
    val maxFps: Int = 0,
    val fpsHistory: List<Int> = emptyList(),
    val gpuName: String = "",
    val systemUsage: SystemUsage = SystemUsage(),
    val result: BenchmarkResult? = null,
    val isUploading: Boolean = false,
    val uploadSuccess: Boolean = false,
    val error: String? = null,
)

class BenchmarkViewModel(
    private val firebaseRepository: FirebaseRepository,
    private val authViewModel: AuthViewModel
) : ViewModel() {

    companion object {
        private const val TAG = "BenchmarkVM"
        private const val WARMUP_MS = 15_000L
        private const val MEASURE_MS = 60_000L
        private const val THERMAL_SEGMENTS = 6
        private const val WARMUP_END_PCT = 0.20f
        private const val MEASURE_END_PCT = 0.95f
        private const val TICK_MS = 100L
    }

    private val _benchmarkState = MutableStateFlow(BenchmarkState())
    val benchmarkState: StateFlow<BenchmarkState> = _benchmarkState.asStateFlow()

    private var benchmarkJob: Job? = null
    private var monitorJob: Job? = null
    private var renderer: BenchmarkRenderer? = null

    private val sampleLock = Any()
    private val fpsSamples = ArrayList<FpsSample>(400)
    @Volatile private var measurementStartMs: Long = 0L

    private val systemMonitor = SystemMonitor()
    private val gpuMonitor = GpuMonitor()
    @Volatile private var detectedGpuName: String = ""
    private var isResultUploaded = false

    fun attachRenderer(r: BenchmarkRenderer) {
        renderer = r
        r.onFpsSample = ::onFpsSample
        r.onGpuDetected = ::onGpuDetected
    }

    fun detachRenderer() {
        renderer?.stop()
        renderer = null
    }

    fun startBenchmark() {
        val cur = _benchmarkState.value.phase
        if (cur == BenchmarkPhase.WARMUP || cur == BenchmarkPhase.MEASUREMENT || cur == BenchmarkPhase.SCORING) return
        isResultUploaded = false
        synchronized(sampleLock) { fpsSamples.clear() }

        _benchmarkState.update {
            BenchmarkState(
                phase = BenchmarkPhase.WARMUP,
                progress = 0f,
                gpuName = detectedGpuName,
            )
        }

        startSystemMonitor()

        benchmarkJob = viewModelScope.launch {
            try {
                renderer?.beginWarmup()
                runWarmup()
                if (_benchmarkState.value.phase == BenchmarkPhase.ERROR) return@launch

                renderer?.beginMeasurement()
                runMeasurement()
                if (_benchmarkState.value.phase == BenchmarkPhase.ERROR) return@launch

                runScoring()
            } catch (ce: CancellationException) {
                throw ce
            } catch (t: Throwable) {
                _benchmarkState.update {
                    it.copy(phase = BenchmarkPhase.ERROR, error = t.message ?: "Unknown error")
                }
            } finally {
                renderer?.stop()
                stopSystemMonitor()
            }
        }
    }

    private fun startSystemMonitor() {
        stopSystemMonitor()
        systemMonitor.reset()
        gpuMonitor.reset()
        monitorJob = viewModelScope.launch(Dispatchers.IO) {
            systemMonitor.readCpuPercent()
            gpuMonitor.readPercent()
            while (currentCoroutineContext().isActive) {
                val cpu = systemMonitor.readCpuPercent()
                val ram = systemMonitor.readRamPercent()
                val gpu = gpuMonitor.readPercent()
                _benchmarkState.update {
                    it.copy(systemUsage = SystemUsage(cpu, gpu, ram))
                }
                delay(500)
            }
        }
    }

    private fun stopSystemMonitor() {
        monitorJob?.cancel()
        monitorJob = null
    }

    private suspend fun runWarmup() {
        val start = System.currentTimeMillis()
        while (currentCoroutineContext().isActive) {
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= WARMUP_MS) break
            if (_benchmarkState.value.phase == BenchmarkPhase.ERROR) return
            val p = (elapsed.toFloat() / WARMUP_MS) * WARMUP_END_PCT
            val remaining = ((WARMUP_MS - elapsed) / 1000L).toInt() + 1
            _benchmarkState.update {
                it.copy(
                    phase = BenchmarkPhase.WARMUP,
                    progress = p,
                    remainingSeconds = remaining,
                )
            }
            delay(TICK_MS)
        }
        _benchmarkState.update { it.copy(progress = WARMUP_END_PCT, remainingSeconds = 0) }
    }

    private suspend fun runMeasurement() {
        measurementStartMs = System.currentTimeMillis()
        val start = measurementStartMs
        while (currentCoroutineContext().isActive) {
            val elapsed = System.currentTimeMillis() - start
            if (elapsed >= MEASURE_MS) break
            if (_benchmarkState.value.phase == BenchmarkPhase.ERROR) return
            val p = WARMUP_END_PCT + (elapsed.toFloat() / MEASURE_MS) * (MEASURE_END_PCT - WARMUP_END_PCT)
            val remaining = ((MEASURE_MS - elapsed) / 1000L).toInt() + 1
            _benchmarkState.update {
                it.copy(
                    phase = BenchmarkPhase.MEASUREMENT,
                    progress = p,
                    remainingSeconds = remaining,
                )
            }
            delay(TICK_MS)
        }
        val sampleCount = synchronized(sampleLock) { fpsSamples.size }
        Log.d(TAG, "Measurement done — samples=$sampleCount")
        _benchmarkState.update { it.copy(progress = MEASURE_END_PCT, remainingSeconds = 0) }
    }

    private suspend fun runScoring() {
        _benchmarkState.update {
            it.copy(
                phase = BenchmarkPhase.SCORING,
                progress = MEASURE_END_PCT,
                remainingSeconds = 0,
            )
        }
        delay(400)
        val result = calculateScore()
        _benchmarkState.update {
            it.copy(
                phase = BenchmarkPhase.RESULT,
                progress = 1f,
                result = result,
            )
        }
        uploadResult(result)
    }

    /** Called from the GL thread. Thread-safe. */
    fun onFpsSample(fps: Float, timestampMs: Long) {
        if (_benchmarkState.value.phase != BenchmarkPhase.MEASUREMENT) return
        if (fps.isNaN() || fps <= 0f) return

        synchronized(sampleLock) { fpsSamples.add(FpsSample(fps, timestampMs)) }

        val snapshot = synchronized(sampleLock) { fpsSamples.toList() }
        if (snapshot.isEmpty()) return
        var sum = 0f; var lo = Float.MAX_VALUE; var hi = 0f
        for (s in snapshot) { sum += s.fps; if (s.fps < lo) lo = s.fps; if (s.fps > hi) hi = s.fps }
        val avg = sum / snapshot.size
        val historyValues = if (snapshot.size <= 60) snapshot else snapshot.takeLast(60)
        val history = historyValues.map { it.fps.toInt() }
        _benchmarkState.update {
            it.copy(
                currentFps = fps.toInt(),
                averageFps = avg,
                minFps = lo.toInt(),
                maxFps = hi.toInt(),
                fpsHistory = history,
            )
        }
    }

    fun onGpuDetected(name: String) {
        detectedGpuName = name
        _benchmarkState.update { it.copy(gpuName = name) }
    }

    fun reportError(message: String) {
        benchmarkJob?.cancel()
        stopSystemMonitor()
        renderer?.stop()
        _benchmarkState.update { it.copy(phase = BenchmarkPhase.ERROR, error = message) }
    }

    private fun calculateScore(): BenchmarkResult {
        val raw = synchronized(sampleLock) { fpsSamples.toList() }
        val totalFrames = renderer?.frameCounter?.get() ?: 0L

        if (raw.isEmpty()) {
            return BenchmarkResult(gpuName = detectedGpuName, totalFrames = totalFrames)
        }

        val measureStart = measurementStartMs
        val segmentMs = MEASURE_MS / THERMAL_SEGMENTS
        val segmentAvgs = FloatArray(THERMAL_SEGMENTS) { idx ->
            val segStart = measureStart + idx * segmentMs
            val segEnd = segStart + segmentMs
            val seg = raw.filter { it.timestampMs in segStart until segEnd }
            if (seg.isEmpty()) 0f else seg.map { it.fps }.average().toFloat()
        }
        // Find first/last segments that actually got samples — guards against
        // edge cases where samples land just outside a boundary.
        val firstSeg = segmentAvgs.firstOrNull { it > 0f } ?: 0f
        val lastSeg = segmentAvgs.indices.reversed()
            .map { segmentAvgs[it] }
            .firstOrNull { it > 0f } ?: 0f
        val sustainedRatio = if (firstSeg > 0f) {
            (lastSeg / firstSeg).coerceIn(0.3f, 1.0f)
        } else 0f

        // FPS distribution (trim outliers).
        val sortedFps = raw.map { it.fps }.sorted()
        val trimStart = (sortedFps.size * 0.05).toInt()
        val trimEnd = (sortedFps.size * 0.95).toInt().coerceAtLeast(trimStart + 1)
        val trimmed = sortedFps.subList(trimStart, trimEnd)

        val avgFps = trimmed.average().toFloat()
        val minFps = sortedFps.first()
        val maxFps = sortedFps.last()
        val p1Idx = (sortedFps.size * 0.01).toInt().coerceAtLeast(0)
        val p1Low = sortedFps[p1Idx]

        // Stability via coefficient of variation: 1 - (stdDev / mean).
        var varSum = 0.0
        for (v in trimmed) {
            val d = v - avgFps
            varSum += d * d
        }
        val variance = (varSum / trimmed.size).toFloat()
        val stdDev = sqrt(variance)
        val stability = if (avgFps > 0f) {
            (1f - (stdDev / avgFps)).coerceIn(0f, 1f)
        } else 0f

        // Score:
        //   base       = avgFps × 50
        //   sustained  = base × sustainedRatio        (rewards thermal headroom)
        //   stable     = sustained × (0.8 + stab×0.2) (rewards consistency)
        //   p1 bonus   = p1Low × 20                   (rewards frame floor)
        val baseScore = avgFps * 50f
        val sustainedScore = baseScore * sustainedRatio
        val stableScore = sustainedScore * (0.8f + stability * 0.2f)
        val finalScore = (stableScore + p1Low * 20f).toInt()

        return BenchmarkResult(
            gpuName = detectedGpuName,
            score = finalScore,
            averageFps = avgFps,
            minFps = minFps,
            maxFps = maxFps,
            p1LowFps = p1Low,
            stability = stability,
            sustainedRatio = sustainedRatio,
            firstSegFps = firstSeg,
            lastSegFps = lastSeg,
            totalFrames = totalFrames,
        )
    }

    fun uploadResult(result: BenchmarkResult) {
        val auth = authViewModel.authState.value
        if (!auth.isLoggedIn || auth.uid == null || isResultUploaded || result.score <= 0) return

        viewModelScope.launch {
            _benchmarkState.update { it.copy(isUploading = true) }
            try {
                firebaseRepository.saveBenchmarkResult(
                    uid = auth.uid,
                    userName = auth.userName ?: "Anonymous",
                    deviceModel = result.deviceName,
                    chipset = result.gpuName.ifBlank { "Unknown GPU" },
                    score = result.score,
                    fps = result.averageFps.toInt(),
                )
                isResultUploaded = true
                _benchmarkState.update { it.copy(isUploading = false, uploadSuccess = true) }
            } catch (e: Exception) {
                _benchmarkState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun reset() {
        benchmarkJob?.cancel()
        stopSystemMonitor()
        renderer?.stop()
        synchronized(sampleLock) { fpsSamples.clear() }
        isResultUploaded = false
        _benchmarkState.update { BenchmarkState(gpuName = detectedGpuName) }
    }

    override fun onCleared() {
        super.onCleared()
        benchmarkJob?.cancel()
        stopSystemMonitor()
        renderer?.stop()
        renderer = null
    }
}
