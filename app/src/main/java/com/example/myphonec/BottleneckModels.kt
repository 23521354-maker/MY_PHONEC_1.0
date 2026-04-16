package com.example.myphonec

enum class Resolution(val label: String, val gpuWeight: Float) {
    R1080P("1080p", 1.0f),
    R1440P("1440p", 1.3f),
    R4K("4K", 1.8f)
}

data class BottleneckResult(
    val percentage: Int,
    val status: String,
    val statusColor: Long, // Hex color
    val description: String,
    val cpuLoad: Int,
    val gpuLoad: Int
)

object BottleneckRepository {
    // Re-using ComparisonData to avoid duplicates
    fun getCPUs() = ComparisonData.cpus
    fun getGPUs() = ComparisonData.gpus
    fun getResolutions() = Resolution.values().toList()
}
