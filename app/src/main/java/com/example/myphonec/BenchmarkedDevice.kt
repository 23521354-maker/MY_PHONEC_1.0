package com.example.myphonec

import java.util.Date

data class BenchmarkedDevice(
    val id: String = "",
    val uid: String = "",
    val deviceModel: String = "",
    val chipset: String = "",
    val score: Int = 0,
    val fps: Int = 0,
    val testedAt: Date? = null
)
