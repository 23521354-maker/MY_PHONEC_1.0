package com.example.myphonec

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildRigScreen(
    onBackClick: () -> Unit,
    viewModel: PCBuilderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showCpuDialog by remember { mutableStateOf(false) }
    var showGpuDialog by remember { mutableStateOf(false) }
    var showMoboDialog by remember { mutableStateOf(false) }
    var showRamDialog by remember { mutableStateOf(false) }
    var showPsuDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("BUILD RIG", color = Color.White, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold))
                        Text("CUSTOM CONFIGURATION", color = Color.Gray, style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xff00e5ff))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "CORE COMPONENTS",
                    color = Color(0xff71717a),
                    style = TextStyle(fontSize = 12.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Bold)
                )
            }

            // Component Slots
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    ComponentSlot(
                        label = "PROCESSOR",
                        value = state.cpu?.name ?: "Select Processor",
                        icon = Icons.Default.Build,
                        onClick = { showCpuDialog = true }
                    )
                    ComponentSlot(
                        label = "GRAPHICS CARD",
                        value = state.gpu?.name ?: "Select GPU",
                        icon = Icons.Default.Info, 
                        onClick = { showGpuDialog = true }
                    )
                    ComponentSlot(
                        label = "MOTHERBOARD",
                        value = state.motherboard?.name ?: "Select Motherboard",
                        icon = Icons.Default.Settings,
                        onClick = { showMoboDialog = true }
                    )
                    ComponentSlot(
                        label = "MEMORY",
                        value = state.ram?.name ?: "Select RAM",
                        icon = Icons.Default.List,
                        onClick = { showRamDialog = true }
                    )
                    ComponentSlot(
                        label = "POWER SUPPLY",
                        value = state.psu?.name ?: "Select PSU",
                        icon = Icons.Default.Refresh,
                        onClick = { showPsuDialog = true }
                    )
                }
            }

            item {
                Button(
                    onClick = { /* Full report logic */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xff00e5ff), contentColor = Color.Black)
                ) {
                    Text("VIEW FULL REPORT", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                }
            }

            // Compatibility Check Section
            item {
                CompatibilityCard(state)
            }
        }
    }

    // Dialogs
    if (showCpuDialog) ComponentSelectionDialog("Select Processor", PCRepository.cpus, { viewModel.selectCPU(it as CPU); showCpuDialog = false }, { showCpuDialog = false })
    if (showGpuDialog) ComponentSelectionDialog("Select GPU", PCRepository.gpus, { viewModel.selectGPU(it as GPU); showGpuDialog = false }, { showGpuDialog = false })
    if (showMoboDialog) ComponentSelectionDialog("Select Motherboard", PCRepository.motherboards, { viewModel.selectMotherboard(it as Motherboard); showMoboDialog = false }, { showMoboDialog = false })
    if (showRamDialog) ComponentSelectionDialog("Select RAM", PCRepository.rams, { viewModel.selectRAM(it as RAM); showRamDialog = false }, { showRamDialog = false })
    if (showPsuDialog) ComponentSelectionDialog("Select PSU", PCRepository.psus, { viewModel.selectPSU(it as PSU); showPsuDialog = false }, { showPsuDialog = false })
}

@Composable
fun ComponentSlot(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xff1f1f1f).copy(alpha = 0.3f),
        border = BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.15f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(Color(0xff22d3ee).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color(0xff00e5ff), modifier = Modifier.size(24.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(label, color = Color(0xff71717a), fontSize = 10.sp, letterSpacing = 1.sp)
                Text(value, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xff52525b))
        }
    }
}

@Composable
fun CompatibilityCard(state: PCBuildState) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xff111111),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("COMPATIBILITY CHECK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Icon(
                    imageVector = if (state.compatibilityScore > 50) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (state.compatibilityScore > 50) Color(0xff2ff801) else Color.Red
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0xff353535))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((state.compatibilityScore / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xff00e5ff), Color(0xff2ff801))))
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SYSTEM INTEGRITY", color = Color.Gray, fontSize = 10.sp)
                    Text("${state.compatibilityScore}% STABLE", color = if (state.compatibilityScore > 70) Color(0xff2ff801) else Color.Yellow, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(color = Color.White.copy(alpha = 0.05f))

            CheckRow("CPU Socket", state.socketCompatible)
            CheckRow("RAM Type", state.ramCompatible)
            CheckRow("Power Supply", state.psuStatus)
            
            if (state.totalTdp > 0) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Est. Draw", color = Color.Gray, fontSize = 12.sp)
                    Text("${state.totalTdp}W", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun CheckRow(label: String, result: CompatibilityResult) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 13.sp)
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val color = when(result.status) {
                CompatibilityResult.Status.OK -> Color(0xff2ff801)
                CompatibilityResult.Status.WARNING -> Color.Yellow
                CompatibilityResult.Status.ERROR -> Color.Red
            }
            Icon(
                imageVector = if (result.isCompatible) Icons.Default.Check else Icons.Default.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(14.dp)
            )
            Text(result.message, color = color, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ComponentSelectionDialog(
    title: String,
    items: List<PCComponent>,
    onSelect: (PCComponent) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xff1a1a1a),
            modifier = Modifier.fillMaxWidth().heightIn(max = 500.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(items) { item ->
                        Surface(
                            onClick = { onSelect(item) },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.05f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(item.name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text(item.brand, color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
