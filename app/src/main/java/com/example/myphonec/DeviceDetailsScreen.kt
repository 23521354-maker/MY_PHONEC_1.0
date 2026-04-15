package com.example.myphonec

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun DeviceDetailsScreen(
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
                            text = "Device Details",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        )
                        if (!deviceInfo.isLoading) {
                            Text(
                                text = deviceInfo.model.uppercase(),
                                style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp, color = Color(0xff22d3ee))
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back",
                            tint = Color(0xff22d3ee)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color.Transparent
    ) { paddingValues ->
        if (deviceInfo.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xff00e5ff))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(
                    top = paddingValues.calculateTopPadding() + 24.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 40.dp
                )
            ) {
                // GENERAL
                item {
                    SectionWrapper(title = "GENERAL") {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DetailItem("Model", deviceInfo.model, isHighlighted = true)
                            DetailItem("Manufacturer", deviceInfo.manufacturer)
                            DetailItem("Board", deviceInfo.board)
                            DetailItem("Hardware", deviceInfo.hardware, isCaps = true)
                        }
                    }
                }

                // DISPLAY
                item {
                    SectionWrapper(title = "DISPLAY") {
                        DetailItem("Resolution", deviceInfo.screenResolution, isHighlighted = true)
                        DetailItem("Density", deviceInfo.screenDensity)
                    }
                }

                // MEMORY
                item {
                    SectionWrapper(title = "MEMORY") {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            InfoCardSmall("TOTAL RAM", deviceInfo.totalRam, Modifier.weight(1f), hasBorder = true)
                            InfoCardSmall("AVAILABLE", deviceInfo.availableRam, Modifier.weight(1f), valueColor = Color(0xff00e5ff))
                        }
                    }
                }

                // STORAGE
                item {
                    val usedStorageBytes = deviceInfo.totalStorageBytes - deviceInfo.availableStorageBytes
                    val storagePercentage = if (deviceInfo.totalStorageBytes > 0) usedStorageBytes.toFloat() / deviceInfo.totalStorageBytes else 0f
                    val usedStorageGb = usedStorageBytes / (1024 * 1024 * 1024.0)
                    
                    SectionWrapper(title = "STORAGE") {
                        StorageProgressCard(
                            title = "Internal Storage",
                            subtitle = "MAIN PARTITION",
                            used = "%.0f".format(usedStorageGb),
                            total = deviceInfo.totalStorage.replace(" GB", ""),
                            percentage = storagePercentage,
                            usedLabel = "${(storagePercentage * 100).toInt()}% USED",
                            freeLabel = "${deviceInfo.availableStorage} FREE"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceDetailsScreenPreview() {
    MyPhoneCTheme {
        DeviceDetailsScreen(onBackClick = {})
    }
}
