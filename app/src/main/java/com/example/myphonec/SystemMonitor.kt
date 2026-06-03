package com.example.myphonec

import android.system.Os
import android.system.OsConstants
import java.io.File
import java.io.RandomAccessFile

/**
 * Samples process CPU and system RAM from /proc. No permissions required.
 * CPU is normalized to total cores (so 100% means all cores saturated).
 */
class SystemMonitor {

    private val clockTicksPerSecond: Long = runCatching {
        Os.sysconf(OsConstants._SC_CLK_TCK)
    }.getOrDefault(100L).coerceAtLeast(1L)

    private val cores: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

    private var lastCpuTicks = 0L
    private var lastWallNs = 0L

    fun reset() {
        lastCpuTicks = 0L
        lastWallNs = 0L
    }

    /** Process CPU% across all cores, 0..100. Returns 0 on the first sample. */
    fun readCpuPercent(): Int = runCatching {
        val line = RandomAccessFile("/proc/self/stat", "r").use { it.readLine() } ?: return 0
        // Format: pid (comm) state ppid pgrp session tty_nr tpgid flags
        //         minflt cminflt majflt cmajflt utime stime cutime cstime ...
        // (comm) may contain spaces — locate the LAST ')' to skip it safely.
        val closeParen = line.lastIndexOf(')')
        if (closeParen < 0) return 0
        val rest = line.substring(closeParen + 1).trim().split(' ').filter { it.isNotEmpty() }
        // After ')': index 0=state, 1=ppid, ..., 11=utime, 12=stime
        if (rest.size < 13) return 0
        val utime = rest[11].toLong()
        val stime = rest[12].toLong()
        val ticks = utime + stime

        val nowNs = System.nanoTime()
        val pct = if (lastCpuTicks == 0L) {
            0
        } else {
            val deltaTicks = (ticks - lastCpuTicks).coerceAtLeast(0L)
            val deltaWallNs = (nowNs - lastWallNs).coerceAtLeast(1L)
            // ticks → nanoseconds of CPU time, normalized over (wall_ns × cores).
            val cpuNs = deltaTicks * 1_000_000_000L / clockTicksPerSecond
            ((cpuNs * 100L) / (deltaWallNs * cores)).toInt()
        }
        lastCpuTicks = ticks
        lastWallNs = nowNs
        pct.coerceIn(0, 100)
    }.getOrDefault(0)

    /** System RAM used %, 0..100. Reads /proc/meminfo. */
    fun readRamPercent(): Int = runCatching {
        var total = 0L
        var available = 0L
        File("/proc/meminfo").useLines { lines ->
            for (l in lines) {
                if (l.startsWith("MemTotal:")) total = parseKb(l)
                else if (l.startsWith("MemAvailable:")) available = parseKb(l)
                if (total > 0 && available > 0) return@useLines
            }
        }
        if (total <= 0) return 0
        val used = (total - available).coerceAtLeast(0L)
        ((used * 100L) / total).toInt().coerceIn(0, 100)
    }.getOrDefault(0)

    private fun parseKb(line: String): Long {
        // "MemTotal:       11774792 kB"
        var i = 0
        while (i < line.length && !line[i].isDigit()) i++
        var v = 0L
        while (i < line.length && line[i].isDigit()) {
            v = v * 10 + (line[i].code - '0'.code)
            i++
        }
        return v
    }
}
