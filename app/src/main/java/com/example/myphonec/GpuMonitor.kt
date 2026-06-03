package com.example.myphonec

import android.util.Log
import java.io.File

/**
 * Reads REAL GPU utilization from vendor sysfs nodes (kgsl for Adreno, mali for ARM).
 *
 * Android has no public API for GPU usage. We probe known sysfs paths at init; if SELinux
 * blocks the read on user builds, [hasRealSource] stays false and [readPercent] returns -1.
 * No heuristic / no fake number — caller can render "N/A" when there's no real source.
 */
class GpuMonitor {

    companion object { private const val TAG = "GpuMonitor" }

    private enum class Source { NONE, ADRENO_DELTA, ADRENO_PCT, MALI_UTIL }

    private var source: Source = Source.NONE
    private var sourceFile: File? = null

    // For delta-style sources (Adreno gpubusy reports cumulative busy/total microseconds).
    private var lastBusy = -1L
    private var lastTotal = -1L

    init { detect() }

    val hasRealSource: Boolean get() = source != Source.NONE

    fun describe(): String = when (source) {
        Source.NONE -> "unavailable (SELinux blocked or unsupported SoC)"
        Source.ADRENO_DELTA -> "Adreno kgsl gpubusy (busy/total μs)"
        Source.ADRENO_PCT -> "Adreno kgsl gpu busy %"
        Source.MALI_UTIL -> "Mali utilization %"
    }

    fun reset() {
        lastBusy = -1L
        lastTotal = -1L
    }

    /** Returns 0..100 if available, or -1 when no real source / first delta sample. */
    fun readPercent(): Int {
        val f = sourceFile ?: return -1
        val text = runCatching { f.readText().trim() }.getOrNull() ?: return -1
        return when (source) {
            Source.ADRENO_DELTA -> readAdrenoDelta(text)
            Source.ADRENO_PCT, Source.MALI_UTIL -> text.toIntOrNull()?.coerceIn(0, 100) ?: -1
            Source.NONE -> -1
        }
    }

    private fun readAdrenoDelta(text: String): Int {
        val parts = text.split(' ', '\t', '\n').filter { it.isNotBlank() }
        if (parts.size < 2) return -1
        val busy = parts[0].toLongOrNull() ?: return -1
        val total = parts[1].toLongOrNull() ?: return -1
        val pct = if (lastBusy < 0L || lastTotal < 0L) {
            -1  // First sample: nothing to diff against yet.
        } else {
            val dB = busy - lastBusy
            val dT = total - lastTotal
            if (dT <= 0L) -1 else ((dB * 100L) / dT).toInt().coerceIn(0, 100)
        }
        lastBusy = busy
        lastTotal = total
        return pct
    }

    private fun detect() {
        // Don't trust canRead() — SELinux on Android 10+ may deny reads even when Unix perms
        // would allow them. Probe by actually trying to read each path.
        val candidates = listOf(
            // Adreno (Qualcomm) — most common SoC family on Android.
            "/sys/class/kgsl/kgsl-3d0/gpubusy" to Source.ADRENO_DELTA,
            "/sys/class/kgsl/kgsl-3d0/gpu_busy_percentage" to Source.ADRENO_PCT,
            "/sys/class/kgsl/kgsl-3d0/devfreq/gpu_load" to Source.ADRENO_PCT,
            // Mali (ARM / many MediaTek + Exynos).
            "/sys/class/misc/mali0/device/utilization" to Source.MALI_UTIL,
            "/sys/devices/platform/mali.0/utilization" to Source.MALI_UTIL,
            "/sys/devices/platform/gpusysfs/gpu_busy" to Source.MALI_UTIL,
        )
        for ((path, type) in candidates) {
            val f = File(path)
            val text = runCatching { f.readText().trim() }.getOrNull() ?: continue
            if (text.isEmpty()) continue
            val ok = when (type) {
                Source.ADRENO_DELTA -> {
                    val parts = text.split(' ', '\t', '\n').filter { it.isNotBlank() }
                    parts.size >= 2 && parts[0].toLongOrNull() != null && parts[1].toLongOrNull() != null
                }
                Source.ADRENO_PCT, Source.MALI_UTIL -> text.toIntOrNull()?.let { it in 0..100 } ?: false
                Source.NONE -> false
            }
            if (ok) {
                source = type
                sourceFile = f
                Log.d(TAG, "Using GPU source: ${describe()} at $path")
                return
            }
        }
        Log.d(TAG, "No real GPU source found — will report N/A")
    }
}
