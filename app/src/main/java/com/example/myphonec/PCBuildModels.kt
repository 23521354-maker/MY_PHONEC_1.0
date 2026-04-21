package com.example.myphonec

import com.google.firebase.firestore.PropertyName

interface PCComponent {
    val id: String
    val name: String
    val brand: String
}

data class CPU(
    override val id: String = "",
    override val name: String = "",
    override val brand: String = "",
    val socket: String = "",
    val tdp: Int = 0,
    val score: Double = 0.0,
    val cores: Int = 0,
    val threads: Int = 0,
    val baseClock: Double = 0.0,
    val boostClock: Double = 0.0,
    val process: String = "",
    val description: String = ""
) : PCComponent

data class GPU(
    override val id: String = "",
    override val name: String = "",
    override val brand: String = "",
    val tdp: Int = 0,
    val score: Double = 0.0,
    val vram: Int = 0,
    val boostClock: Int = 0,
    val architecture: String = "",
    val description: String = ""
) : PCComponent

data class Motherboard(
    override val id: String = "",
    override val name: String = "",
    override val brand: String = "",
    val socket: String = "",
    @get:PropertyName("ramType") @set:PropertyName("ramType")
    var ramType: String = ""
) : PCComponent

data class RAM(
    override val id: String = "",
    override val name: String = "",
    override val brand: String = "",
    val type: String = ""
) : PCComponent

data class PSU(
    override val id: String = "",
    override val name: String = "",
    override val brand: String = "",
    val watt: Int = 0
) : PCComponent

data class CompatibilityResult(
    val isCompatible: Boolean,
    val message: String,
    val status: Status = Status.OK
) {
    enum class Status { OK, WARNING, ERROR }
}

data class PCBuildState(
    val cpu: CPU? = null,
    val gpu: GPU? = null,
    val motherboard: Motherboard? = null,
    val ram: RAM? = null,
    val psu: PSU? = null,
    val totalTdp: Int = 0,
    val requiredPower: Int = 0,
    val buildScore: Int = 0,
    val buildStability: String = "Select components",
    val socketCompatible: CompatibilityResult = CompatibilityResult(true, "Select CPU & Motherboard"),
    val ramCompatible: CompatibilityResult = CompatibilityResult(true, "Select RAM & Motherboard"),
    val psuStatus: CompatibilityResult = CompatibilityResult(true, "Select Power Supply"),
    val isLoading: Boolean = false,
    val error: String? = null
)
