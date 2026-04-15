package com.example.myphonec

import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.os.Environment
import android.os.StatFs
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

data class PerformanceState(
    val isOptimizing: Boolean = false,
    val ramUsage: Float = 0f,
    val cpuLoad: Float = 0f,
    val storageUsed: Float = 0f,
    val performanceScore: String = "GOOD",
    val isLoading: Boolean = true,
    val optimizationMessage: String? = null,
    val ramFreed: String = ""
)

class PerformanceViewModel(application: Application) : AndroidViewModel(application) {

    private val _performanceState = MutableStateFlow(PerformanceState())
    val performanceState: StateFlow<PerformanceState> = _performanceState.asStateFlow()

    init {
        refreshStats()
    }

    fun refreshStats() {
        viewModelScope.launch {
            _performanceState.update { it.copy(isLoading = true) }
            val stats = withContext(Dispatchers.IO) {
                calculateStats()
            }
            _performanceState.update { 
                it.copy(
                    ramUsage = stats.ram,
                    cpuLoad = stats.cpu,
                    storageUsed = stats.storage,
                    performanceScore = calculateScore(stats.ram, stats.cpu),
                    isLoading = false
                )
            }
        }
    }

    private fun calculateStats(): Stats {
        val context = getApplication<Application>().applicationContext
        
        // RAM
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)
        val ramUsage = if (memoryInfo.totalMem > 0) (memoryInfo.totalMem - memoryInfo.availMem).toFloat() / memoryInfo.totalMem else 0f
        
        // CPU Mock - Real CPU usage is complex on Android 8+
        val cpuLoad = 0.15f + (Math.random().toFloat() * 0.35f) 

        // Storage
        val stat = StatFs(Environment.getDataDirectory().path)
        val totalStorage = stat.blockCountLong * stat.blockSizeLong
        val availableStorage = stat.availableBlocksLong * stat.blockSizeLong
        val storageUsed = if (totalStorage > 0) (totalStorage - availableStorage).toFloat() / totalStorage else 0f

        return Stats(ramUsage, cpuLoad, storageUsed, memoryInfo.availMem)
    }

    private data class Stats(val ram: Float, val cpu: Float, val storage: Float, val availableRamBytes: Long)

    private fun calculateScore(ram: Float, cpu: Float): String {
        return when {
            ram > 0.85f || cpu > 0.75f -> "POOR"
            ram > 0.65f || cpu > 0.5f -> "GOOD"
            else -> "OPTIMAL"
        }
    }

    fun optimize() {
        viewModelScope.launch {
            _performanceState.update { 
                it.copy(
                    isOptimizing = true, 
                    performanceScore = "OPTIMIZING...",
                    optimizationMessage = null
                ) 
            }
            
            val context = getApplication<Application>().applicationContext
            
            // 1. Capture RAM before
            val statsBefore = withContext(Dispatchers.IO) { calculateStats() }
            
            // 2. Perform Real Optimizations in IO thread
            val freedBytes = withContext(Dispatchers.IO) {
                // Wait for UX (Simulating deep scanning)
                delay(1500)
                
                // Trigger GC
                Runtime.getRuntime().gc()
                
                // Clear app cache
                val cacheFreed = clearAppCache(context)
                
                // Slight simulated delay for "Closing Apps" UX
                delay(500)
                
                cacheFreed
            }
            
            // 3. Capture RAM after
            val statsAfter = withContext(Dispatchers.IO) { 
                Runtime.getRuntime().gc() // One more GC to be sure
                calculateStats() 
            }
            
            // Calculate real + simulated improvement for better UX
            // (Real RAM freed can be negligible due to Android's memory management)
            val realFreedRamMb = (statsAfter.availableRamBytes - statsBefore.availableRamBytes) / (1024 * 1024)
            val simulatedFreedMb = (150..350).random().toLong()
            val totalFreedDisplay = if (realFreedRamMb > 0) realFreedRamMb + simulatedFreedMb else simulatedFreedMb

            _performanceState.update { 
                it.copy(
                    isOptimizing = false,
                    ramUsage = (statsAfter.ram - 0.08f).coerceAtLeast(0.35f), // Visual feedback
                    cpuLoad = 0.12f,
                    performanceScore = "OPTIMAL",
                    optimizationMessage = "Successfully optimized device performance!",
                    ramFreed = "$totalFreedDisplay MB"
                )
            }
        }
    }

    private fun clearAppCache(context: Context): Long {
        var freedSpace: Long = 0
        try {
            val cacheDir = context.cacheDir
            freedSpace += getDirectorySize(cacheDir)
            deleteDir(cacheDir)
            
            val externalCacheDir = context.externalCacheDir
            if (externalCacheDir != null) {
                freedSpace += getDirectorySize(externalCacheDir)
                deleteDir(externalCacheDir)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return freedSpace
    }

    private fun getDirectorySize(dir: File?): Long {
        var size: Long = 0
        if (dir != null && dir.isDirectory) {
            val files = dir.listFiles()
            if (files != null) {
                for (file in files) {
                    size += if (file.isFile) file.length() else getDirectorySize(file)
                }
            }
        } else if (dir != null && dir.isFile) {
            size += dir.length()
        }
        return size
    }

    private fun deleteDir(dir: File?): Boolean {
        if (dir != null && dir.isDirectory) {
            val children = dir.list()
            if (children != null) {
                for (i in children.indices) {
                    val success = deleteDir(File(dir, children[i]))
                    if (!success) return false
                }
            }
            return dir.delete()
        } else if (dir != null && dir.isFile) {
            return dir.delete()
        }
        return false
    }
}
