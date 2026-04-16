package com.example.myphonec

enum class SocketType { LGA1700, AM4, AM5, LGA1200 }
enum class RamType { DDR4, DDR5 }

interface PCComponent {
    val id: String
    val name: String
    val brand: String
}

data class CPU(
    override val id: String,
    override val name: String,
    override val brand: String,
    val socket: SocketType,
    val tdp: Int,
    val cores: Int,
    val threads: Int,
    val performanceScore: Int
) : PCComponent

data class GPU(
    override val id: String,
    override val name: String,
    override val brand: String,
    val vram: Int,
    val tdp: Int,
    val performanceScore: Int
) : PCComponent

data class Motherboard(
    override val id: String,
    override val name: String,
    override val brand: String,
    val socket: SocketType,
    val ramType: RamType,
    val powerDraw: Int = 50
) : PCComponent

data class RAM(
    override val id: String,
    override val name: String,
    override val brand: String,
    val sizeGb: Int,
    val type: RamType,
    val speed: Int,
    val powerDraw: Int = 10
) : PCComponent

data class PSU(
    override val id: String,
    override val name: String,
    override val brand: String,
    val wattage: Int
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
    val compatibilityScore: Int = 0,
    val socketCompatible: CompatibilityResult = CompatibilityResult(true, "Select components"),
    val ramCompatible: CompatibilityResult = CompatibilityResult(true, "Select components"),
    val psuStatus: CompatibilityResult = CompatibilityResult(true, "Select components")
)

object PCRepository {
    val cpus = listOf(
        CPU("cpu_1", "Core i5-12400F", "Intel", SocketType.LGA1700, 65, 6, 12, 75),
        CPU("cpu_2", "Ryzen 5 5600", "AMD", SocketType.AM4, 65, 6, 12, 72),
        CPU("cpu_3", "Core i9-13900K", "Intel", SocketType.LGA1700, 125, 24, 32, 98),
        CPU("cpu_4", "Ryzen 9 7950X", "AMD", SocketType.AM5, 170, 16, 32, 96)
    )

    val gpus = listOf(
        GPU("gpu_1", "RTX 3060", "NVIDIA", 12, 170, 70),
        GPU("gpu_2", "RTX 4080", "NVIDIA", 16, 320, 92),
        GPU("gpu_3", "RX 6600", "AMD", 8, 132, 65),
        GPU("gpu_4", "RX 7900 XTX", "AMD", 24, 355, 95)
    )

    val motherboards = listOf(
        Motherboard("mobo_1", "ASUS Prime B660", "ASUS", SocketType.LGA1700, RamType.DDR4),
        Motherboard("mobo_2", "MSI B550-A PRO", "MSI", SocketType.AM4, RamType.DDR4),
        Motherboard("mobo_3", "ROG STRIX Z790-E", "ASUS", SocketType.LGA1700, RamType.DDR5),
        Motherboard("mobo_4", "GIGABYTE X670E", "Gigabyte", SocketType.AM5, RamType.DDR5)
    )

    val rams = listOf(
        RAM("ram_1", "16GB DDR4-3200", "Corsair", 16, RamType.DDR4, 3200),
        RAM("ram_2", "32GB DDR5-6000", "G.Skill", 32, RamType.DDR5, 6000),
        RAM("ram_3", "8GB DDR4-2666", "Crucial", 8, RamType.DDR4, 2666)
    )

    val psus = listOf(
        PSU("psu_1", "500W Bronze", "EVGA", 500),
        PSU("psu_2", "750W Gold", "Corsair", 750),
        PSU("psu_3", "1000W Platinum", "Seasonic", 1000)
    )
}
