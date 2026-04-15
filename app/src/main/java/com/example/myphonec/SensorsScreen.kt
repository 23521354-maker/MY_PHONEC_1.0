package com.example.myphonec

import android.hardware.Sensor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorsScreen(
    onBackClick: () -> Unit,
    viewModel: SensorsViewModel = viewModel()
) {
    val state by viewModel.sensorsState.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.startListening()
            } else if (event == Lifecycle.Event.ON_PAUSE) {
                viewModel.stopListening()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopListening()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            painter = painterResource(id = R.drawable.container),
                            contentDescription = null,
                            tint = Color(0xff00e5ff),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sensors",
                            color = Color(0xffe2e2e2),
                            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color(0xff00e5ff)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Background Glow
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(Color(0xff00e5ff).copy(alpha = 0.15f), Color.Transparent),
                            center = Offset(200f, -100f),
                            radius = 800f
                        )
                    )
            )

            if (state.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xff00e5ff))
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                    contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
                ) {
                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(
                                text = "Sensors",
                                color = Color(0xffe2e2e2),
                                style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1.8).sp)
                            )
                            Text(
                                text = "Real-time sensor data",
                                color = Color(0xffbac9cc).copy(alpha = 0.7f),
                                style = TextStyle(fontSize = 16.sp)
                            )
                        }
                    }

                    item {
                        SystemStatusCard(count = state.activeSensorsCount)
                    }

                    item {
                        SensorSection(title = "MOTION SENSORS") {
                            SensorItem(
                                name = "Accelerometer",
                                subtitle = "STANDARD\nGRAVITY",
                                values = formatValues(state.sensorValues[Sensor.TYPE_ACCELEROMETER], "X: %.2f, Y: %.2f, Z: %.2f"),
                                unit = "m/s²",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_ACCELEROMETER)
                            )
                            SensorItem(
                                name = "Gyroscope",
                                subtitle = "ANGULAR\nSPEED",
                                values = formatValues(state.sensorValues[Sensor.TYPE_GYROSCOPE], "α: %.2f, β: %.2f, γ: %.2f"),
                                unit = "rad/s",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_GYROSCOPE)
                            )
                            SensorItem(
                                name = "Linear Acceleration",
                                subtitle = "NON-GRAVITATIONAL",
                                values = formatValues(state.sensorValues[Sensor.TYPE_LINEAR_ACCELERATION], "%.3f"),
                                unit = "m/s²",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_LINEAR_ACCELERATION)
                            )
                            SensorItem(
                                name = "Rotation Vector",
                                subtitle = "FUSED DATA",
                                values = formatValues(state.sensorValues[Sensor.TYPE_ROTATION_VECTOR], "%.2f [X,Y,Z,W]"),
                                unit = "",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_ROTATION_VECTOR)
                            )
                        }
                    }

                    item {
                        SensorSection(title = "POSITION SENSORS") {
                            SensorItem(
                                name = "Magnetometer",
                                subtitle = "AMBIENT MAGNETIC FIELD",
                                values = formatValues(state.sensorValues[Sensor.TYPE_MAGNETIC_FIELD], "%.1f μT • NORTH"),
                                unit = "",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_MAGNETIC_FIELD)
                            )
                            SensorItem(
                                name = "Proximity",
                                subtitle = "OBJECT DISTANCE",
                                values = formatValues(state.sensorValues[Sensor.TYPE_PROXIMITY], "%.1f cm"),
                                unit = "",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_PROXIMITY)
                            )
                        }
                    }

                    item {
                        SensorSection(title = "ENVIRONMENT SENSORS") {
                            SensorItem(
                                name = "Ambient Light",
                                subtitle = "ILLUMINANCE LEVEL",
                                values = formatValues(state.sensorValues[Sensor.TYPE_LIGHT], "%.0f LUX"),
                                unit = "",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_LIGHT)
                            )
                            SensorItem(
                                name = "Barometer",
                                subtitle = "ATMOSPHERIC PRESSURE",
                                values = formatValues(state.sensorValues[Sensor.TYPE_PRESSURE], "%.2f hPa"),
                                unit = "",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_PRESSURE)
                            )
                            SensorItem(
                                name = "Temperature",
                                subtitle = "INTERNAL THERMISTOR",
                                values = formatValues(state.sensorValues[Sensor.TYPE_AMBIENT_TEMPERATURE], "%.1f °C", "CALIBRATING..."),
                                unit = "",
                                isActive = state.sensorValues.containsKey(Sensor.TYPE_AMBIENT_TEMPERATURE),
                                isItalicValue = !state.sensorValues.containsKey(Sensor.TYPE_AMBIENT_TEMPERATURE)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SystemStatusCard(count: Int) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xff1f1f1f).copy(alpha = 0.7f),
        border = BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth().shadow(elevation = 20.dp, shape = RoundedCornerShape(14.dp))
    ) {
        Box(modifier = Modifier.padding(32.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "SYSTEM STATUS",
                    color = Color(0xff00e5ff),
                    style = TextStyle(fontSize = 12.sp, letterSpacing = 2.4.sp)
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = count.toString(),
                        color = Color(0xffe2e2e2),
                        style = TextStyle(fontSize = 60.sp, fontWeight = FontWeight.Bold, letterSpacing = (-3).sp)
                    )
                    Text(
                        text = "Active Sensors",
                        color = Color(0xff00e5ff),
                        style = TextStyle(fontSize = 18.sp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Text(
                    text = "Monitoring low-latency diagnostic feed",
                    color = Color(0xffbac9cc).copy(alpha = 0.6f),
                    style = TextStyle(fontSize = 14.sp)
                )
            }
            
            // Subtle glow in card
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 40.dp, y = 40.dp)
                    .size(120.dp)
                    .blur(40.dp)
                    .background(Color(0xff00e5ff).copy(alpha = 0.05f))
            )
        }
    }
}

@Composable
fun SensorSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = title,
            color = Color(0xffbac9cc).copy(alpha = 0.5f),
            style = TextStyle(fontSize = 12.sp, letterSpacing = 3.6.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            content()
        }
    }
}

@Composable
fun SensorItem(
    name: String,
    subtitle: String,
    values: String,
    unit: String,
    isActive: Boolean,
    isItalicValue: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.15f)), RoundedCornerShape(12.dp))
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Badge(containerColor = if (isActive) Color(0xff2ff801) else Color(0xff353535))
                Text(text = name, color = Color(0xffe2e2e2), style = TextStyle(fontSize = 14.sp))
            }
            Text(
                text = subtitle,
                color = Color(0xffbac9cc).copy(alpha = 0.4f),
                style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp, lineHeight = 1.5.em)
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = values,
                color = if (isActive) Color(0xff00e5ff) else Color(0xffbac9cc).copy(alpha = 0.4f),
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    fontStyle = if (isItalicValue) FontStyle.Italic else FontStyle.Normal,
                    textAlign = TextAlign.End
                )
            )
            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    color = Color(0xffbac9cc).copy(alpha = 0.4f),
                    style = TextStyle(fontSize = 10.sp)
                )
            }
        }
    }
}

private fun formatValues(values: FloatArray?, format: String, default: String = "NO DATA"): String {
    if (values == null) return default
    return try {
        when (values.size) {
            1 -> String.format(format, values[0])
            2 -> String.format(format, values[0], values[1])
            3 -> String.format(format, values[0], values[1], values[2])
            4 -> String.format(format, values[0], values[1], values[2], values[3])
            else -> values.joinToString(", ") { String.format("%.2f", it) }
        }
    } catch (e: Exception) {
        values.joinToString(", ") { String.format("%.2f", it) }
    }
}
