package com.example.myphonec

enum class Resolution(val label: String, val cpuMultiplier: Float, val gpuMultiplier: Float) {
    R1080P("1080p", 1.10f, 1.0f),
    R1440P("1440p", 1.0f, 1.0f),
    R4K("4K", 1.0f, 1.10f)
}

data class BottleneckResult(
    val percentage: Int,
    val status: String,
    val statusColor: Long, // Hex color
    val description: String,
    val cpuLoad: Int,
    val gpuLoad: Int,
    val direction: String
)
