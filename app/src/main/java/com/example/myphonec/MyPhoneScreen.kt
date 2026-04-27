package com.example.myphonec

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.myphonec.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MyPhoneScreen(
    modifier: Modifier = Modifier,
    viewModel: DeviceInfoViewModel = viewModel(),
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    onNavigateToDetails: () -> Unit,
    onNavigateToProcessor: () -> Unit,
    onNavigateToSystemDetails: () -> Unit,
    onNavigateToScreenTest: () -> Unit,
    onNavigateToSensors: () -> Unit,
    onNavigateToBattery: () -> Unit,
    onNavigateToPerformance: () -> Unit,
    onNavigateToBenchmark: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToLeaderboard: () -> Unit,
    onNavigateToAdmin: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val profileState by userProfileViewModel.uiState.collectAsState()
    var showUserMenu by remember { mutableStateOf(false) }

    LaunchedEffect(authState.isLoggedIn, authState.isGuest) {
        if (!authState.isLoggedIn && !authState.isGuest && !authState.isLoading) {
            onNavigateToLogin()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
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
                    modifier = Modifier.clickable { onNavigateToAdmin() },
                    style = TextStyle(
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = (-1.2).sp
                    )
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onNavigateToLeaderboard,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.05f))
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.score_board),
                            contentDescription = "Leaderboard",
                            tint = Color(0xff22d3ee),
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box {
                        IconButton(
                            onClick = { showUserMenu = true },
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White.copy(alpha = 0.05f))
                        ) {
                            if (authState.photoUrl != null) {
                                AsyncImage(
                                    model = authState.photoUrl,
                                    contentDescription = "Profile",
                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    painter = painterResource(id = R.drawable.user),
                                    contentDescription = "Profile",
                                    tint = Color(0xff22d3ee),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        if (showUserMenu) {
                            UserMenuPopup(
                                authState = authState,
                                profileState = profileState,
                                onDismiss = { showUserMenu = false },
                                onLogout = {
                                    authViewModel.signOut()
                                    showUserMenu = false
                                }
                            )
                        }
                    }
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
                    text = "ACTIVE SESSION: ${authState.userName?.uppercase() ?: "LOCAL DEVICE"}",
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

            // Diagnostic Section Redesigned
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(
                    text = "DIAGNOSTIC CLUSTER", 
                    color = Color(0xffbac9cc), 
                    style = TextStyle(fontSize = 12.sp, letterSpacing = 3.6.sp, fontWeight = FontWeight.Bold)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Row 1: Screen & Sensors
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DiagnosticCardSmall("Screen test", "OLED INTEGRITY", iconId = R.drawable.screen, modifier = Modifier.weight(1f), onClick = onNavigateToScreenTest)
                        DiagnosticCardSmall("Sensors test", "IMU CALIBRATION", iconId = R.drawable.sensor, modifier = Modifier.weight(1f), onClick = onNavigateToSensors)
                    }
                    
                    // Row 2: Battery & Performance
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        DiagnosticCardSmall("Battery status", "HEALTH & CYCLE", iconId = R.drawable.battery, modifier = Modifier.weight(1f), onClick = onNavigateToBattery)
                        DiagnosticCardSmall("Performance", "OPTIMAL STATE", iconId = R.drawable.bottleneck, modifier = Modifier.weight(1f), onClick = onNavigateToPerformance)
                    }
                    
                    // Row 3: Benchmark Test (Large Full Width)
                    BenchmarkCard(onClick = onNavigateToBenchmark)
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun BenchmarkCard(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .scale(scale)
            .clip(RoundedCornerShape(32.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xff1f1f1f), Color(0xff151515))
                )
            )
            .border(
                BorderStroke(
                    1.5.dp, 
                    if (isPressed) Color(0xff00e5ff) else Color(0xff00e5ff).copy(alpha = 0.3f)
                ), 
                RoundedCornerShape(32.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
    ) {
        // Glowing background effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xff22d3ee).copy(alpha = if (isPressed) 0.15f else 0.05f),
                            Color.Transparent
                        ),
                        radius = 600f
                    )
                )
        )

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Surface(
                    color = Color(0xff00e5ff),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "NEW",
                        color = Color.Black,
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Black),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Benchmark Test",
                    color = Color.White,
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = "GPU STRESS + FPS SCORE",
                    color = Color(0xffbac9cc),
                    style = TextStyle(fontSize = 12.sp, letterSpacing = 1.sp)
                )
            }
            
            // Rocket/Gauge Icon (using container.xml as placeholder for rocket style)
            Icon(
                painter = painterResource(id = R.drawable.container),
                contentDescription = null,
                tint = Color(0xff00e5ff),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
fun DiagnosticCardSmall(title: String, subtitle: String, iconId: Int, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .height(140.dp)
            .clip(RoundedCornerShape(32.dp))
            .background(color = Color(0xff1f1f1f))
            .clickable(onClick = onClick)
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Icon(painter = painterResource(id = iconId), contentDescription = null, tint = Color(0xff00e5ff), modifier = Modifier.size(28.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = title, color = Color(0xffe2e2e2), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
            Text(text = subtitle, color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp))
        }
    }
}

@Composable
fun UserMenuPopup(
    authState: AuthState,
    profileState: UserProfileUiState,
    onDismiss: () -> Unit,
    onLogout: () -> Unit
) {
    var selectedDevice by remember { mutableStateOf<BenchmarkedDevice?>(null) }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, 140),
        onDismissRequest = onDismiss,
        properties = PopupProperties(focusable = true)
    ) {
        Surface(
            modifier = Modifier
                .width(320.dp)
                .padding(end = 24.dp),
            color = Color(0xff0a0a0a),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, Color(0xff22d3ee).copy(alpha = 0.2f)),
            shadowElevation = 16.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // User Info Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(Color(0xff22d3ee).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (authState.photoUrl != null) {
                            AsyncImage(
                                model = authState.photoUrl,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                painter = painterResource(id = R.drawable.user),
                                contentDescription = null,
                                tint = Color(0xff22d3ee),
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = authState.userName ?: "Guest User",
                            color = Color.White,
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = authState.userEmail ?: "No email provider",
                            color = Color.Gray,
                            style = TextStyle(fontSize = 12.sp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Devices Benchmarked Section
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "DEVICES BENCHMARKED",
                        color = Color(0xff22d3ee),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))

                    // Summary Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.03f))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SummaryStat("Total Tested", profileState.totalDevices.toString())
                        SummaryStat("Highest Score", profileState.highestScore.toString())
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Compact Scrollable List
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                    ) {
                        if (profileState.benchmarkedDevices.isEmpty()) {
                            Text(
                                text = "No records found",
                                color = Color.Gray,
                                style = TextStyle(fontSize = 13.sp),
                                modifier = Modifier.padding(vertical = 16.dp).align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(profileState.benchmarkedDevices) { device ->
                                    DeviceBenchmarkRow(
                                        device = device,
                                        onClick = { selectedDevice = device }
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.05f))
                Spacer(modifier = Modifier.height(16.dp))

                // Logout Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onLogout() }
                        .padding(vertical = 12.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = Color(0xfff87171),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        color = Color(0xfff87171),
                        style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
    }

    if (selectedDevice != null) {
        DeviceDetailDialog(
            device = selectedDevice!!,
            onDismiss = { selectedDevice = null }
        )
    }
}

@Composable
fun SummaryStat(label: String, value: String) {
    Column {
        Text(text = label, color = Color.Gray, style = TextStyle(fontSize = 10.sp))
        Text(text = value, color = Color.White, style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun DeviceBenchmarkRow(
    device: BenchmarkedDevice,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val dateStr = device.testedAt?.let { dateFormat.format(it) } ?: "N/A"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.phone),
            contentDescription = null,
            tint = Color(0xff22d3ee).copy(alpha = 0.7f),
            modifier = Modifier.size(24.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.deviceModel,
                color = Color.White,
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Bold),
                maxLines = 1
            )
            Text(
                text = "${device.chipset} • $dateStr",
                color = Color.Gray,
                style = TextStyle(fontSize = 10.sp),
                maxLines = 1
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = device.score.toString(),
                color = Color(0xff22d3ee),
                style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black)
            )
            Text(
                text = "${device.fps} FPS",
                color = Color.Gray,
                style = TextStyle(fontSize = 10.sp)
            )
        }
    }
}

@Composable
fun DeviceDetailDialog(
    device: BenchmarkedDevice,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            color = Color(0xff121212),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xff22d3ee).copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "BENCHMARK DETAIL",
                        color = Color(0xff22d3ee),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }

                Icon(
                    painter = painterResource(id = R.drawable.container),
                    contentDescription = null,
                    tint = Color(0xff22d3ee),
                    modifier = Modifier.size(64.dp)
                )

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = device.deviceModel,
                        color = Color.White,
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = device.chipset,
                        color = Color.Gray,
                        style = TextStyle(fontSize = 14.sp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DetailCard("SCORE", device.score.toString(), Modifier.weight(1f))
                    DetailCard("FPS", device.fps.toString(), Modifier.weight(1f))
                }

                val dateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy", Locale.getDefault())
                Text(
                    text = "Tested on ${device.testedAt?.let { dateFormat.format(it) } ?: "N/A"}",
                    color = Color.Gray,
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
    }
}

@Composable
fun DetailCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, color = Color(0xff22d3ee), style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Bold))
        Text(text = value, color = Color.White, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Black))
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
