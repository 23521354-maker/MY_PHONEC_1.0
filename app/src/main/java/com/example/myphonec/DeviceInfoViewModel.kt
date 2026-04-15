package com.example.myphonec

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class DeviceInfo(
    val model: String = "Unknown",
    val manufacturer: String = "Unknown",
    val board: String = "Unknown",
    val hardware: String = "Unknown",
    val processor: String = "Unknown",
    val osVersion: String = "Unknown",
    val apiLevel: Int = 0,
    val totalRam: String = "0 GB",
    val availableRam: String = "0 GB",
    val ramType: String = "Unknown RAM",
    val totalRamBytes: Long = 0L,
    val availableRamBytes: Long = 0L,
    val totalStorage: String = "0 GB",
    val availableStorage: String = "0 GB",
    val storageType: String = "Internal Storage",
    val totalStorageBytes: Long = 0L,
    val availableStorageBytes: Long = 0L,
    val architecture: String = "Unknown",
    val screenResolution: String = "Unknown",
    val screenDensity: String = "Unknown",
    val cores: Int = Runtime.getRuntime().availableProcessors(),
    val securityPatch: String = "Unknown",
    val buildId: String = "Unknown",
    val kernelVersion: String = System.getProperty("os.version") ?: "Unknown",
    val bootloader: String = "Unknown",
    val isLoading: Boolean = true
)

class DeviceInfoViewModel(application: Application) : AndroidViewModel(application) {

    private val _deviceInfo = MutableStateFlow(DeviceInfo())
    val deviceInfo: StateFlow<DeviceInfo> = _deviceInfo.asStateFlow()

    init {
        loadDeviceInfo()
    }

    private fun loadDeviceInfo() {
        viewModelScope.launch {
            _deviceInfo.update { it.copy(isLoading = true) }
            
            val info = withContext(Dispatchers.IO) {
                fetchDeviceInfo()
            }
            
            _deviceInfo.value = info.copy(isLoading = false)
        }
    }

    private fun fetchDeviceInfo(): DeviceInfo {
        val context = getApplication<Application>().applicationContext
        
        // RAM Info
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val totalRamGb = memoryInfo.totalMem / (1024 * 1024 * 1024.0)
        val availRamGb = memoryInfo.availMem / (1024 * 1024 * 1024.0)

        // Storage Info
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        val blockSize = stat.blockSizeLong
        val totalBlocks = stat.blockCountLong
        val availableBlocks = stat.availableBlocksLong
        val totalStorageBytes = totalBlocks * blockSize
        val availStorageBytes = availableBlocks * blockSize
        val totalStorageGb = totalStorageBytes / (1024 * 1024 * 1024.0)
        val availStorageGb = availStorageBytes / (1024 * 1024 * 1024.0)

        // Display Info
        val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val metrics = DisplayMetrics()
        @Suppress("DEPRECATION")
        windowManager.defaultDisplay.getRealMetrics(metrics)
        val resolution = "${metrics.widthPixels} x ${metrics.heightPixels} px"
        val density = "${metrics.densityDpi} ppi"

        // Processor
        val hardware = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL
        } else {
            Build.HARDWARE
        }

        return DeviceInfo(
            model = Build.MODEL,
            manufacturer = Build.MANUFACTURER,
            board = Build.BOARD,
            hardware = Build.HARDWARE,
            processor = hardware ?: "Unknown",
            osVersion = Build.VERSION.RELEASE,
            apiLevel = Build.VERSION.SDK_INT,
            totalRam = "%.0f GB".format(totalRamGb),
            availableRam = "%.1f GB".format(availRamGb),
            ramType = detectRamType(),
            totalRamBytes = memoryInfo.totalMem,
            availableRamBytes = memoryInfo.availMem,
            totalStorage = "%.0f GB".format(totalStorageGb),
            availableStorage = "%.1f GB".format(availStorageGb),
            storageType = detectStorageType(),
            totalStorageBytes = totalStorageBytes,
            availableStorageBytes = availStorageBytes,
            architecture = Build.SUPPORTED_ABIS.getOrNull(0) ?: "Unknown",
            screenResolution = resolution,
            screenDensity = density,
            securityPatch = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) Build.VERSION.SECURITY_PATCH else "Unknown",
            buildId = Build.ID,
            bootloader = Build.BOOTLOADER
        )
    }

    private fun detectRamType(): String {
        return try {
            val ramTypeProp = getSystemProperty("ro.boot.ram_type")
            if (ramTypeProp.isNotEmpty()) return ramTypeProp
            
            val hardware = Build.HARDWARE.lowercase()
            when {
                hardware.contains("qcom") || hardware.contains("snapdragon") -> "LPDDR4X/5"
                hardware.contains("mt") || hardware.contains("mediatek") -> "LPDDR4/5X"
                else -> "LPDDR"
            }
        } catch (e: Exception) {
            "Unknown RAM type"
        }
    }

    private fun detectStorageType(): String {
        return try {
            val ufsPath = File("/sys/block/sda/device/model")
            val mmcPath = File("/sys/block/mmcblk0/device/name")
            
            when {
                ufsPath.exists() -> "UFS Storage"
                mmcPath.exists() -> "eMMC Storage"
                else -> {
                    val prop = getSystemProperty("ro.boot.storage_type")
                    if (prop.isNotEmpty()) prop else "Internal Storage"
                }
            }
        } catch (e: Exception) {
            "Internal Storage"
        }
    }

    private fun getSystemProperty(key: String): String {
        return try {
            val c = Class.forName("android.os.SystemProperties")
            val get = c.getMethod("get", String::class.java)
            get.invoke(c, key) as String
        } catch (e: Exception) {
            ""
        }
    }
}
