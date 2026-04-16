package com.example.myphonec

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class PCBuilderViewModel : ViewModel() {
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

    fun selectMotherboard(mobo: Motherboard) {
        _uiState.update { it.copy(motherboard = mobo) }
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
        _uiState.update { state ->
            val cpuMoboComp = checkCpuMobo(state.cpu, state.motherboard)
            val ramMoboComp = checkRamMobo(state.ram, state.motherboard)
            val totalTdp = calculateTotalTdp(state)
            val psuComp = checkPsu(totalTdp, state.psu)
            
            val score = calculateIntegrityScore(cpuMoboComp, ramMoboComp, psuComp)
            
            state.copy(
                totalTdp = totalTdp,
                compatibilityScore = score,
                socketCompatible = cpuMoboComp,
                ramCompatible = ramMoboComp,
                psuStatus = psuComp
            )
        }
    }

    private fun checkCpuMobo(cpu: CPU?, mobo: Motherboard?): CompatibilityResult {
        if (cpu == null || mobo == null) return CompatibilityResult(true, "Select CPU & Motherboard")
        return if (cpu.socket == mobo.socket) {
            CompatibilityResult(true, "Compatible", CompatibilityResult.Status.OK)
        } else {
            CompatibilityResult(false, "Socket Mismatch: ${cpu.socket} vs ${mobo.socket}", CompatibilityResult.Status.ERROR)
        }
    }

    private fun checkRamMobo(ram: RAM?, mobo: Motherboard?): CompatibilityResult {
        if (ram == null || mobo == null) return CompatibilityResult(true, "Select RAM & Motherboard")
        return if (ram.type == mobo.ramType) {
            CompatibilityResult(true, "${ram.type} Supported", CompatibilityResult.Status.OK)
        } else {
            CompatibilityResult(false, "Incompatible RAM Type", CompatibilityResult.Status.ERROR)
        }
    }

    private fun calculateTotalTdp(state: PCBuildState): Int {
        var tdp = 0
        tdp += state.cpu?.tdp ?: 0
        tdp += state.gpu?.tdp ?: 0
        tdp += state.motherboard?.powerDraw ?: 0
        tdp += state.ram?.powerDraw ?: 0
        return tdp
    }

    private fun checkPsu(totalTdp: Int, psu: PSU?): CompatibilityResult {
        if (psu == null) return CompatibilityResult(true, "Select PSU")
        val buffer = 100 // Safety margin
        return when {
            psu.wattage < totalTdp -> CompatibilityResult(false, "Insufficient Power", CompatibilityResult.Status.ERROR)
            psu.wattage < totalTdp + buffer -> CompatibilityResult(true, "Near Limit", CompatibilityResult.Status.WARNING)
            else -> CompatibilityResult(true, "Optimal Power", CompatibilityResult.Status.OK)
        }
    }

    private fun calculateIntegrityScore(
        cpuMobo: CompatibilityResult,
        ramMobo: CompatibilityResult,
        psu: CompatibilityResult
    ): Int {
        if (cpuMobo.status == CompatibilityResult.Status.ERROR || ramMobo.status == CompatibilityResult.Status.ERROR || psu.status == CompatibilityResult.Status.ERROR) {
            return 30 // System unstable/incompatible
        }
        
        var score = 100
        if (psu.status == CompatibilityResult.Status.WARNING) score -= 15
        
        // If some parts missing, reduce score slightly to indicate "Incomplete"
        return score
    }
}
