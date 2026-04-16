package com.example.myphonec

import androidx.compose.ui.graphics.Color

data class CPUData(
    val id: String,
    val name: String,
    val cores: Int,
    val threads: Int,
    val clockSpeed: String,
    val clockSpeedValue: Float, // in GHz for comparison
    val l3Cache: String,
    val l3CacheValue: Int, // in MB
    val tdp: String,
    val tdpValue: Int, // in Watts
    val performanceScore: Float // Out of 100
)

data class GPUData(
    val id: String,
    val name: String,
    val vram: String,
    val vramValue: Int, // in GB
    val coreCount: Int,
    val clockSpeed: String,
    val clockSpeedValue: Int, // in MHz
    val memoryType: String,
    val tdp: String,
    val tdpValue: Int, // in Watts
    val performanceScore: Float // Out of 100
)

object ComparisonData {
    val cpus = listOf(
        CPUData("i5_12400f", "Core i5-12400F", 6, 12, "2.5GHz", 2.5f, "18MB", 18, "65W", 65, 75.5f),
        CPUData("r5_5600", "Ryzen 5 5600", 6, 12, "3.5GHz", 3.5f, "32MB", 32, "65W", 65, 82.3f),
        CPUData("i7_12700k", "Core i7-12700K", 12, 20, "3.6GHz", 3.6f, "25MB", 25, "125W", 125, 92.1f),
        CPUData("r7_5800x", "Ryzen 7 5800X", 8, 16, "3.8GHz", 3.8f, "32MB", 32, "105W", 105, 88.5f),
        CPUData("i9_13900k", "Core i9-13900K", 24, 32, "3.0GHz", 3.0f, "36MB", 36, "125W", 125, 98.8f)
    )

    val gpus = listOf(
        GPUData("rtx_3060", "RTX 3060", "12GB", 12, 3584, "1320MHz", 1320, "GDDR6", "170W", 170, 72.4f),
        GPUData("rx_6600", "RX 6600", "8GB", 8, 1792, "1626MHz", 1626, "GDDR6", "132W", 132, 68.1f),
        GPUData("rtx_4070", "RTX 4070", "12GB", 12, 5888, "1920MHz", 1920, "GDDR6X", "200W", 200, 91.2f),
        GPUData("rx_7800xt", "RX 7800 XT", "16GB", 16, 3840, "2124MHz", 2124, "GDDR6", "263W", 263, 89.5f),
        GPUData("rtx_4090", "RTX 4090", "24GB", 24, 16384, "2235MHz", 2235, "GDDR6X", "450W", 450, 100.0f)
    )
}
