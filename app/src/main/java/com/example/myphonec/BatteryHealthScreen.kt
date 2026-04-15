package com.example.myphonec

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphonec.ui.theme.MyPhoneCTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHealthScreen(
    onBackClick: () -> Unit,
    viewModel: BatteryViewModel = viewModel()
) {
    val batteryInfo by viewModel.batteryState.collectAsState()

    DisposableEffect(viewModel) {
        viewModel.registerReceiver()
        onDispose {
            viewModel.unregisterReceiver()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "BATTERY HEALTH",
                            color = Color.White,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                        )
                        if (!batteryInfo.isLoading) {
                            Text(
                                text = "${batteryInfo.status.uppercase()} • ${batteryInfo.level}%",
                                color = Color(0xff00e5ff),
                                style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp)
                            )
                        }
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
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Info",
                            tint = Color(0xff00e5ff)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        if (batteryInfo.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xff00e5ff))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
            ) {
                // Header Section
                item {
                    BatteryHeaderCard(batteryInfo)
                }

                // Status Section
                item {
                    BatterySection(title = "STATUS") {
                        val healthColor = when(batteryInfo.estimatedHealth) {
                            "Good" -> Color(0xff2ff801)
                            "Average" -> Color(0xFFFFB300)
                            else -> Color(0xFFFF5252)
                        }
                        BatteryInfoRow(label = "ESTIMATED HEALTH", value = batteryInfo.estimatedHealth, valueColor = healthColor)
                        BatteryInfoRow(label = "SYSTEM REPORT", value = batteryInfo.health)
                        BatteryInfoRow(label = "STATUS", value = batteryInfo.status)
                        BatteryInfoRow(label = "POWER SOURCE", value = batteryInfo.powerSource)
                    }
                }

                // Level Section
                item {
                    BatterySection(title = "LEVEL") {
                        BatteryInfoRow(label = "BATTERY LEVEL", value = "${batteryInfo.level}%", valueColor = Color(0xff00e5ff))
                    }
                }

                // Technical Section
                item {
                    BatterySection(title = "TECHNICAL") {
                        BatteryInfoRow(label = "TECHNOLOGY", value = batteryInfo.technology)
                        BatteryInfoRow(label = "VOLTAGE", value = "${batteryInfo.voltage} mV")
                        BatteryInfoRow(label = "TEMPERATURE", value = "${batteryInfo.temperature} °C", valueColor = if (batteryInfo.temperature > 40f) Color(0xFFFF5252) else Color.White)
                        BatteryInfoRow(label = "CAPACITY", value = batteryInfo.capacity)
                    }
                }

                // Bottom Stats
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        StatBox(
                            title = "DISCHARGE RATE",
                            value = if (batteryInfo.status == "Charging") "N/A" else "120 mA",
                            iconId = R.drawable.margin,
                            modifier = Modifier.weight(1f)
                        )
                        StatBox(
                            title = "TIME TO FULL",
                            value = if (batteryInfo.status == "Charging") "42 min" else "N/A",
                            iconId = R.drawable.container,
                            modifier = Modifier.weight(1f),
                            iconTint = Color(0xff2ff801)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BatteryHeaderCard(info: BatteryInfo) {
    val levelColor by animateColorAsState(
        targetValue = when {
            info.level < 20 -> Color(0xFFFF5252)
            info.level < 50 -> Color(0xFFFFB300)
            else -> Color(0xff00e5ff)
        },
        animationSpec = tween(1000),
        label = "levelColor"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(BorderStroke(1.dp, levelColor.copy(alpha = 0.2f)), RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${info.level}%",
                color = levelColor,
                style = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold, letterSpacing = (-3.6).sp)
            )
            Text(
                text = "REMAINING",
                color = Color(0xff717171),
                style = TextStyle(fontSize = 10.sp, letterSpacing = 4.sp)
            )
        }

        // Progress Bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(info.level / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(levelColor)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HeaderStat(label = "HEALTH", value = info.estimatedHealth, valueColor = Color(0xff2ff801))
            HeaderStat(label = "STATUS", value = info.status)
            HeaderStat(label = "TEMP", value = "${info.temperature.toInt()}°C", valueColor = if (info.temperature > 40f) Color(0xFFFF5252) else Color.White)
        }
    }
}

@Composable
fun HeaderStat(label: String, value: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xff717171), style = TextStyle(fontSize = 9.sp, letterSpacing = 0.9.sp))
        Text(text = value, color = valueColor, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun BatterySection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color(0xff717171),
            style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White.copy(alpha = 0.03f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
        ) {
            content()
        }
    }
}

@Composable
fun BatteryInfoRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xff717171), style = TextStyle(fontSize = 12.sp))
        Text(text = value, color = valueColor, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun StatBox(title: String, value: String, iconId: Int, modifier: Modifier = Modifier, iconTint: Color = Color(0xff00e5ff)) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Column {
            Text(text = title, color = Color(0xff717171), style = TextStyle(fontSize = 9.sp, letterSpacing = 0.9.sp))
            Text(text = value, color = Color.White, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
        }
    }
}

@Preview
@Composable
fun BatteryHealthScreenPreview() {
    MyPhoneCTheme {
        BatteryHealthScreen(onBackClick = {})
    }
}
