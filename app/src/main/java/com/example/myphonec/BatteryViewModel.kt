package com.example.myphonec

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class BatteryInfo(
    val level: Int = 0,
    val status: String = "Unknown",
    val health: String = "Unknown",
    val estimatedHealth: String = "Good",
    val powerSource: String = "Battery",
    val voltage: Int = 0,
    val temperature: Float = 0f,
    val technology: String = "Unknown",
    val capacity: String = "5000 mAh",
    val isLoading: Boolean = true
)

class BatteryViewModel(application: Application) : AndroidViewModel(application) {

    private val _batteryState = MutableStateFlow(BatteryInfo())
    val batteryState: StateFlow<BatteryInfo> = _batteryState.asStateFlow()

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent?.let {
                val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                val batteryPct = if (level != -1 && scale != -1) (level * 100 / scale.toFloat()).toInt() else 0

                val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                val statusString = when (status) {
                    BatteryManager.BATTERY_STATUS_CHARGING -> "Charging"
                    BatteryManager.BATTERY_STATUS_DISCHARGING -> "Discharging"
                    BatteryManager.BATTERY_STATUS_FULL -> "Full"
                    BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Not Charging"
                    else -> "Unknown"
                }

                val systemHealth = it.getIntExtra(BatteryManager.EXTRA_HEALTH, -1)
                val healthString = when (systemHealth) {
                    BatteryManager.BATTERY_HEALTH_COLD -> "Cold"
                    BatteryManager.BATTERY_HEALTH_DEAD -> "Dead"
                    BatteryManager.BATTERY_HEALTH_GOOD -> "Good"
                    BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Overheat"
                    BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Over Voltage"
                    BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Failure"
                    else -> "Unknown"
                }

                val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
                val powerSource = when (plugged) {
                    BatteryManager.BATTERY_PLUGGED_AC -> "AC Charger"
                    BatteryManager.BATTERY_PLUGGED_USB -> "USB Port"
                    BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Wireless"
                    else -> "Battery"
                }

                val voltage = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                val temperature = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f
                val technology = it.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY) ?: "Unknown"

                // Intelligent Health Estimation
                val estimatedHealth = estimateBatteryHealth(systemHealth, voltage, temperature)

                _batteryState.update { state ->
                    state.copy(
                        level = batteryPct,
                        status = statusString,
                        health = healthString,
                        estimatedHealth = estimatedHealth,
                        powerSource = powerSource,
                        voltage = voltage,
                        temperature = temperature,
                        technology = technology,
                        isLoading = false
                    )
                }
            }
        }
    }

    private fun estimateBatteryHealth(systemHealth: Int, voltage: Int, temperature: Float): String {
        // Base estimation on system health first
        if (systemHealth == BatteryManager.BATTERY_HEALTH_DEAD) return "Poor"
        if (systemHealth == BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE) return "Average"
        
        // Custom logic based on thresholds
        return when {
            // Temperature over 45C is bad for longevity
            temperature > 45f -> "Average"
            // If voltage is very low while battery level is high (theoretical sign of wear)
            // Normal voltage for Li-ion is ~3.7V to 4.2V. 
            // 3000mV is typically very low/critical.
            voltage > 0 && voltage < 3200 -> "Average"
            temperature > 50f -> "Poor"
            else -> "Good"
        }
    }

    private var isReceiverRegistered = false

    fun registerReceiver() {
        if (isReceiverRegistered) return
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        getApplication<Application>().registerReceiver(batteryReceiver, filter)
        isReceiverRegistered = true
    }

    fun unregisterReceiver() {
        if (!isReceiverRegistered) return
        try {
            getApplication<Application>().unregisterReceiver(batteryReceiver)
            isReceiverRegistered = false
        } catch (e: Exception) {
            // Receiver not registered
        }
    }

    override fun onCleared() {
        super.onCleared()
        unregisterReceiver()
    }
}
