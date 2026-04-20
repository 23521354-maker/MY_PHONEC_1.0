package com.example.myphonec

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PCBuilderViewModel(private val firebaseRepository: FirebaseRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(PCBuildState())
    val uiState: StateFlow<PCBuildState> = _uiState.asStateFlow()

    fun selectCPU(cpu: CPU) {
        _uiState.update { it.copy(cpu = cpu) }
        checkCompatibility()
    }

    fun selectGPU(gpu: GPU) {
        _uiState.update { it.copy(gpu = gpu) }
        checkCompatibility()
    }

    fun selectMotherboard(motherboard: Motherboard) {
        _uiState.update { it.copy(motherboard = motherboard) }
        checkCompatibility()
    }

    fun selectRAM(ram: RAM) {
        _uiState.update { it.copy(ram = ram) }
        checkCompatibility()
    }

    fun selectPSU(psu: PSU) {
        _uiState.update { it.copy(psu = psu) }
        checkCompatibility()
    }

    private fun checkCompatibility() {
        val currentState = _uiState.value
        val cpu = currentState.cpu
        val mobo = currentState.motherboard
        val ram = currentState.ram
        val gpu = currentState.gpu
        val psu = currentState.psu

        // Socket Check
        val socketResult = when {
            cpu == null || mobo == null -> CompatibilityResult(true, "Select CPU and Motherboard", CompatibilityResult.Status.OK)
            cpu.socket == mobo.socket -> CompatibilityResult(true, "Socket ${cpu.socket} Match", CompatibilityResult.Status.OK)
            else -> CompatibilityResult(false, "Socket Mismatch: ${cpu.socket} vs ${mobo.socket}", CompatibilityResult.Status.ERROR)
        }

        // RAM Check
        val ramResult = when {
            ram == null || mobo == null -> CompatibilityResult(true, "Select RAM and Motherboard", CompatibilityResult.Status.OK)
            ram.type == mobo.ramType -> CompatibilityResult(true, "${ram.type} Match", CompatibilityResult.Status.OK)
            else -> CompatibilityResult(false, "RAM Mismatch: ${ram.type} vs ${mobo.ramType}", CompatibilityResult.Status.ERROR)
        }

        // PSU Check
        val totalTdp = (cpu?.tdp ?: 0) + (gpu?.tdp ?: 0) + (mobo?.powerDraw ?: 0) + (ram?.powerDraw ?: 0) + 100 // 100W overhead
        val psuResult = when {
            psu == null -> CompatibilityResult(true, "Select PSU", CompatibilityResult.Status.OK)
            psu.wattage >= totalTdp -> CompatibilityResult(true, "Wattage Sufficient", CompatibilityResult.Status.OK)
            else -> CompatibilityResult(false, "Low Wattage: ${psu.wattage}W vs ${totalTdp}W req.", CompatibilityResult.Status.ERROR)
        }

        // Calculate Score
        var score = 0
        if (cpu != null) score += 20
        if (gpu != null) score += 20
        if (mobo != null) score += 20
        if (ram != null) score += 20
        if (psu != null) score += 20
        
        if (!socketResult.isCompatible) score -= 40
        if (!ramResult.isCompatible) score -= 40
        if (!psuResult.isCompatible) score -= 40

        _uiState.update {
            it.copy(
                socketCompatible = socketResult,
                ramCompatible = ramResult,
                psuStatus = psuResult,
                totalTdp = totalTdp,
                compatibilityScore = score.coerceIn(0, 100)
            )
        }
    }
}
