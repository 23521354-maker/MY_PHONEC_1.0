package com.example.myphonec

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphonec.ui.theme.MyPhoneCTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemDetailsScreen(
    viewModel: DeviceInfoViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "System",
                            color = Color.White,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Medium)
                        )
                        if (!deviceInfo.isLoading) {
                            Text(
                                text = "ANDROID ${deviceInfo.osVersion}",
                                color = Color.White.copy(alpha = 0.6f),
                                style = TextStyle(fontSize = 12.sp, letterSpacing = 1.sp)
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
                            tint = Color.White.copy(alpha = 0.6f)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xff131313),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color(0xff00e5ff),
                    actionIconContentColor = Color.White.copy(alpha = 0.6f)
                )
            )
        },
        containerColor = Color(0xff131313)
    ) { padding ->
        if (deviceInfo.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xff00e5ff))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                item {
                    SystemHeaderCard(
                        version = deviceInfo.osVersion, 
                        apiLevel = deviceInfo.apiLevel.toString(), 
                        securityPatch = deviceInfo.securityPatch
                    )
                }

                item {
                    SystemInfoSection(title = "ANDROID SYSTEM") {
                        SystemInfoRow(label = "Android Version", value = deviceInfo.osVersion)
                        SystemInfoRow(label = "API Level", value = deviceInfo.apiLevel.toString())
                        SystemInfoRow(label = "Security Patch Level", value = deviceInfo.securityPatch)
                        SystemInfoRow(label = "Build ID", value = deviceInfo.buildId)
                    }
                }

                item {
                    SystemInfoSection(title = "KERNEL") {
                        SystemInfoRow(label = "Kernel Architecture", value = deviceInfo.architecture)
                        SystemInfoRow(label = "Kernel Version", value = deviceInfo.kernelVersion)
                    }
                }

                item {
                    SystemInfoSection(title = "DEVICE STATUS") {
                        SystemInfoRow(label = "Bootloader", value = deviceInfo.bootloader.uppercase(), valueColor = Color(0xff2ff801))
                        SystemInfoRow(label = "Manufacturer", value = deviceInfo.manufacturer)
                        SystemInfoRow(label = "Board", value = deviceInfo.board)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemHeaderCard(version: String, apiLevel: String, securityPatch: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xff1f1f1f).copy(alpha = 0.7f))
            .border(BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.1f)), RoundedCornerShape(24.dp))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = version,
            color = Color(0xff00e5ff),
            style = TextStyle(fontSize = 72.sp, fontWeight = FontWeight.Bold)
        )
        Text(
            text = "ANDROID VERSION",
            color = Color(0xffbac9cc),
            style = TextStyle(fontSize = 14.sp, letterSpacing = 4.sp, fontWeight = FontWeight.Medium)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HeaderSubInfo(label = "API LEVEL", value = apiLevel)
            HeaderSubInfo(label = "SECURITY PATCH", value = securityPatch)
        }
    }
}

@Composable
fun HeaderSubInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp))
        Text(text = value, color = Color.White, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun SystemInfoSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color(0xffbac9cc).copy(alpha = 0.6f),
            style = TextStyle(fontSize = 14.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xff1f1f1f).copy(alpha = 0.7f))
                .border(BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.1f)), RoundedCornerShape(16.dp))
        ) {
            content()
        }
    }
}

@Composable
fun SystemInfoRow(label: String, value: String, valueColor: Color = Color.White) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = Color(0xffbac9cc),
            style = TextStyle(fontSize = 14.sp)
        )
        Text(
            text = value,
            color = valueColor,
            style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.End),
            modifier = Modifier.weight(1f, fill = false).padding(start = 16.dp)
        )
    }
}

@Preview
@Composable
fun SystemDetailsScreenPreview() {
    MyPhoneCTheme {
        SystemDetailsScreen(onBackClick = {})
    }
}
