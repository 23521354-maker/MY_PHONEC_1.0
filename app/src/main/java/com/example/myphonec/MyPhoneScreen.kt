package com.example.myphonec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.myphonec.ui.theme.*

@Composable
fun MyPhoneScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceInfoViewModel = viewModel(),
    onNavigateToDetails: () -> Unit,
    onNavigateToProcessor: () -> Unit,
    onNavigateToSystemDetails: () -> Unit,
    onNavigateToScreenTest: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .statusBarsPadding()
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PHONEC",
                color = Color(0xff22d3ee),
                style = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1.2).sp
                )
            )
            IconButton(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.user),
                    contentDescription = "Profile",
                    tint = Color(0xff22d3ee),
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Header Section
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "My Phone",
                color = Color(0xffe2e2e2),
                style = TextStyle(
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-1.8).sp
                )
            )
            Text(
                text = "ACTIVE SESSION: LOCAL DEVICE",
                color = Color(0xffbac9cc),
                style = TextStyle(
                    fontSize = 14.sp,
                    letterSpacing = 2.8.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }

        // Device Info Section
        DeviceInfoSectionRefined(
            deviceInfo = deviceInfo,
            onSystemModelClick = onNavigateToDetails,
            onProcessorClick = onNavigateToProcessor,
            onOSVersionClick = onNavigateToSystemDetails
        )

        // Diagnostic Section
        DiagnosticSectionRefined(
            onScreenTestClick = onNavigateToScreenTest,
            onSensorsClick = onNavigateToSensors,
            onBatteryClick = onNavigateToBattery,
            onPerformanceClick = onNavigateToPerformance
        )
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun DeviceInfoSectionRefined(
    deviceInfo: DeviceInfo,
    onSystemModelClick: () -> Unit,
    onProcessorClick: () -> Unit,
    onOSVersionClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(32.dp))
            .background(color = Color(0xff1b1b1b))
            .border(BorderStroke(1.dp, Color(0xffffffff).copy(alpha = 0.05f)), RoundedCornerShape(32.dp))
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        InfoCardRefined(
            title = "SYSTEM MODEL",
            value = deviceInfo.model,
            modifier = Modifier.fillMaxWidth(),
            isHighlight = true,
            onClick = onSystemModelClick
        )

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCardRefined(
                title = "PROCESSOR",
                value = deviceInfo.processor,
                modifier = Modifier.weight(1f),
                onClick = onProcessorClick
            )
            InfoCardRefined(
                title = "OS VERSION",
                value = "Android ${deviceInfo.osVersion}",
                modifier = Modifier.weight(1f),
                onClick = onOSVersionClick
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoCardRefined("MEMORY", "${deviceInfo.totalRam}\nRAM", Modifier.weight(1f))
            StorageCardRefined(
                totalStorage = deviceInfo.totalStorage,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun InfoCardRefined(
    title: String, 
    value: String, 
    modifier: Modifier = Modifier, 
    isHighlight: Boolean = false,
    onClick: (() -> Unit)? = null
) {
    val cardModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Column(
        modifier = cardModifier
            .then(if (!isHighlight) Modifier.height(120.dp) else Modifier.wrapContentHeight())
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color(0xff242424))
            .border(BorderStroke(1.dp, Color(0xff00daf3).copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            color = if (onClick != null || isHighlight) Color(0xff00e5ff) else Color(0xffbac9cc),
            style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = value,
            color = Color(0xffe2e2e2),
            style = TextStyle(
                fontSize = if (isHighlight) 32.sp else 16.sp, 
                fontWeight = FontWeight.Bold
            ),
            lineHeight = if (isHighlight) 24.sp else 20.sp
        )
    }
}

@Composable
fun StorageCardRefined(
    totalStorage: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color = Color(0xff242424))
            .border(BorderStroke(1.dp, Color(0xff00daf3).copy(alpha = 0.1f)), RoundedCornerShape(12.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight(0.4f)
                .width(3.dp)
                .align(Alignment.CenterStart)
                .background(Color(0xff00e5ff))
        )
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(text = "STORAGE", color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = totalStorage, color = Color(0xffe2e2e2), style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold))
                Text(text = " TOTAL", color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp))
            }
            Text(text = "Internal", color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp))
        }
    }
}

@Composable
fun DiagnosticSectionRefined(
    onScreenTestClick: () -> Unit,
    onSensorsClick: () -> Unit,
    onBatteryClick: () -> Unit,
    onPerformanceClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(text = "DIAGNOSTIC CLUSTER", color = Color(0xffbac9cc), style = TextStyle(fontSize = 12.sp, letterSpacing = 3.6.sp, fontWeight = FontWeight.Bold))
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticCardRefined("Screen test", "OLED INTEGRITY", iconId = R.drawable.screen, modifier = Modifier.weight(1f), onClick = onScreenTestClick)
                DiagnosticCardRefined("Sensors test", "IMU CALIBRATION", iconId = R.drawable.sensor, modifier = Modifier.weight(1f), onClick = onSensorsClick)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DiagnosticCardRefined("Battery status", "HEALTH & CYCLE", iconId = R.drawable.battery, modifier = Modifier.weight(1f), onClick = onBatteryClick)
                DiagnosticCardRefined("Performance", "OPTIMAL STATE", iconId = R.drawable.margin, modifier = Modifier.weight(1f), onClick = onPerformanceClick)
            }
        }
    }
}

@Composable
fun DiagnosticCardRefined(title: String, subtitle: String, iconId: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(color = Color(0xff1f1f1f))
            .clickable(onClick = onClick)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Icon(painter = painterResource(id = iconId), contentDescription = null, tint = Color(0xff00e5ff), modifier = Modifier.size(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = title, color = Color(0xffe2e2e2), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text(text = subtitle, color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp))
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF131313)
@Composable
fun MyPhoneScreenPreview() {
    MyPhoneCTheme {
        MyPhoneScreen(onNavigateToDetails = {}, onNavigateToProcessor = {}, onNavigateToSystemDetails = {}, onNavigateToScreenTest = {}, onNavigateToSensors = {}, onNavigateToBattery = {}, onNavigateToPerformance = {}, onNavigateToLogin = {})
    }
}
