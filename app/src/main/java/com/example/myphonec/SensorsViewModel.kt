package com.example.myphonec

import android.app.Application
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensorsState(
    val sensorValues: Map<Int, FloatArray> = emptyMap(),
    val activeSensorsCount: Int = 0,
    val isLoading: Boolean = true
)

class SensorsViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager = application.getSystemService(Application.SENSOR_SERVICE) as SensorManager

    private val _sensorsState = MutableStateFlow(SensorsState())
    val sensorsState = _sensorsState.asStateFlow()

    private val supportedSensors = listOf(
        Sensor.TYPE_ACCELEROMETER,
        Sensor.TYPE_GYROSCOPE,
        Sensor.TYPE_LINEAR_ACCELERATION,
        Sensor.TYPE_ROTATION_VECTOR,
        Sensor.TYPE_MAGNETIC_FIELD,
        Sensor.TYPE_PROXIMITY,
        Sensor.TYPE_LIGHT,
        Sensor.TYPE_PRESSURE,
        Sensor.TYPE_AMBIENT_TEMPERATURE
    )

    private var lastUpdateTimestamp = 0L
    private val THROTTLE_MS = 200L // Throttle sensor updates to every 200ms

    fun startListening() {
        viewModelScope.launch(Dispatchers.Default) {
            _sensorsState.update { it.copy(isLoading = true) }
            var count = 0
            supportedSensors.forEach { sensorType ->
                val sensor = sensorManager.getDefaultSensor(sensorType)
                if (sensor != null) {
                    // Use SENSOR_DELAY_NORMAL for better performance/battery
                    sensorManager.registerListener(this@SensorsViewModel, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                    count++
                }
            }
            _sensorsState.update { it.copy(activeSensorsCount = count, isLoading = false) }
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTimestamp < THROTTLE_MS) return
        
        event?.let {
            lastUpdateTimestamp = currentTime
            val newValues = _sensorsState.value.sensorValues.toMutableMap()
            newValues[it.sensor.type] = it.values.copyOf()
            _sensorsState.update { state -> state.copy(sensorValues = newValues) }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
    }

    override fun onCleared() {
        super.onCleared()
        stopListening()
    }
}
