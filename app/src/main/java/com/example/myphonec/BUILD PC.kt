package com.example.myphonec

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildRigScreen(
    onBackClick: () -> Unit,
    viewModel: PCBuilderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiRecommendation by viewModel.aiRecommendation.collectAsState()
    
    val cpuList by viewModel.cpus.collectAsState()
    val gpuList by viewModel.gpus.collectAsState()
    val motherboardList by viewModel.motherboards.collectAsState()
    val ramList by viewModel.rams.collectAsState()
    val psuList by viewModel.psus.collectAsState()

    var showCpuDialog by remember { mutableStateOf(false) }
    var showGpuDialog by remember { mutableStateOf(false) }
    var showMoboDialog by remember { mutableStateOf(false) }
    var showRamDialog by remember { mutableStateOf(false) }
    var showPsuDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showAiSheet by remember { mutableStateOf(false) }
    var showAiChat by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
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
                contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
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
                            icon = Icons.Default.Memory,
                            onClick = { showCpuDialog = true }
                        )
                        ComponentSlot(
                            label = "GRAPHICS CARD",
                            value = state.gpu?.name ?: "Select GPU",
                            icon = Icons.Default.DeveloperBoard, 
                            onClick = { showGpuDialog = true }
                        )
                        ComponentSlot(
                            label = "MOTHERBOARD",
                            value = state.motherboard?.name ?: "Select Motherboard",
                            icon = Icons.Default.SettingsInputComponent,
                            onClick = { showMoboDialog = true }
                        )
                        ComponentSlot(
                            label = "MEMORY",
                            value = state.ram?.name ?: "Select RAM",
                            icon = Icons.Default.Dns,
                            onClick = { showRamDialog = true }
                        )
                        ComponentSlot(
                            label = "POWER SUPPLY",
                            value = state.psu?.name ?: "Select PSU",
                            icon = Icons.Default.Power,
                            onClick = { showPsuDialog = true }
                        )
                    }
                }

                // AI Recommendation Card
                if (aiRecommendation != null) {
                    item {
                        AiRecommendationCard(aiRecommendation!!)
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // View Report Button
                        Button(
                            onClick = { showReportDialog = true },
                            enabled = state.cpu != null || state.gpu != null,
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff00e5ff), contentColor = Color.Black)
                        ) {
                            Text("VIEW FULL REPORT", fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.Launch, contentDescription = null, modifier = Modifier.size(18.dp))
                        }
                    }
                }

                // Compatibility Check Section
                item {
                    CompatibilityCard(state)
                }
            }
        }

        // Floating AI Assistant Button
        AiAssistantFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            onClick = { showAiChat = true }
        )

        // AI Chat Overlay
        AnimatedVisibility(
            visible = showAiChat,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            AiChatOverlay(
                viewModel = viewModel,
                onDismiss = { showAiChat = false }
            )
        }
    }

    // AI Configuration Bottom Sheet
    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = sheetState,
            containerColor = Color(0xff0a0a0a),
            contentColor = Color.White
        ) {
            AiConfigForm(
                onGenerate = { budget, usage, res, brand, upgrade ->
                    viewModel.suggestBuildWithAi(budget, usage, res, brand, upgrade)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) showAiSheet = false
                    }
                }
            )
        }
    }

    // Dialogs
    if (showCpuDialog) ComponentSelectionDialog("Select Processor", cpuList, { viewModel.selectCPU(it as CPU); showCpuDialog = false }, { showCpuDialog = false })
    if (showGpuDialog) ComponentSelectionDialog("Select GPU", gpuList, { viewModel.selectGPU(it as GPU); showGpuDialog = false }, { showGpuDialog = false })
    if (showMoboDialog) ComponentSelectionDialog("Select Motherboard", motherboardList, { viewModel.selectMotherboard(it as Motherboard); showMoboDialog = false }, { showMoboDialog = false })
    if (showRamDialog) ComponentSelectionDialog("Select RAM", ramList, { viewModel.selectRAM(it as RAM); showRamDialog = false }, { showRamDialog = false })
    if (showPsuDialog) ComponentSelectionDialog("Select PSU", psuList, { viewModel.selectPSU(it as PSU); showPsuDialog = false }, { showPsuDialog = false })
    
    if (showReportDialog) {
        FullBuildReportDialog(state = state, onDismiss = { showReportDialog = false })
    }
}

@Composable
fun AiAssistantFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.size(60.dp),
        shape = CircleShape,
        color = Color(0xff1e1b4b),
        border = BorderStroke(2.dp, Color(0xff22d3ee)),
        shadowElevation = 8.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = "AI Assistant",
                tint = Color(0xff22d3ee),
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
fun AiChatOverlay(
    viewModel: PCBuilderViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xff22d3ee).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xff22d3ee))
                    }
                    Column {
                        Text("PC BUILD ASSISTANT", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("Online | Powered by AI", color = Color(0xff22d3ee), fontSize = 10.sp)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(messages) { message ->
                    ChatBubble(message)
                }
                if (isLoading) {
                    item {
                        AiTypingIndicator()
                    }
                }
            }

            // Input Field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xff0a0a0a),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about your build...", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = Color(0xff1a1a1a),
                            focusedContainerColor = Color(0xff1a1a1a),
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = Color(0xff22d3ee).copy(alpha = 0.5f)
                        ),
                        maxLines = 4
                    )
                    IconButton(
                        onClick = {
                            if (inputText.isNotBlank()) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank() && !isLoading,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (inputText.isNotBlank() && !isLoading) Color(0xff22d3ee) else Color(0xff1a1a1a))
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank() && !isLoading) Color.Black else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isUser) Color(0xff22d3ee) else Color(0xff1e1e1e)
    val textColor = if (message.isUser) Color.Black else Color.White
    val shape = if (message.isUser) {
        RoundedCornerShape(20.dp, 20.dp, 4.dp, 20.dp)
    } else {
        RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Surface(
            color = bgColor,
            shape = shape,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Text(
                text = message.text,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                color = textColor,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun AiTypingIndicator() {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .background(Color(0xff1e1e1e), RoundedCornerShape(20.dp, 20.dp, 20.dp, 4.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) {
            val alpha by rememberInfiniteTransition().animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = it * 200),
                    repeatMode = RepeatMode.Reverse
                )
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(Color(0xff22d3ee).copy(alpha = alpha))
            )
        }
    }
}

@Composable
fun AiConfigForm(onGenerate: (String, String, String, String, String) -> Unit) {
    var budget by remember { mutableStateOf("1000") }
    var usage by remember { mutableStateOf("Gaming") }
    var resolution by remember { mutableStateOf("1440p") }
    var brand by remember { mutableStateOf("No Preference") }
    var upgrade by remember { mutableStateOf("Yes") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text("AI BUILD CONFIGURATOR", color = Color(0xff22d3ee), fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 2.sp)
        
        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("Budget (USD)") },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = Color.White,
                focusedTextColor = Color.White,
                unfocusedBorderColor = Color.Gray,
                focusedBorderColor = Color(0xff22d3ee)
            )
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AiDropdownField("Usage", usage, listOf("Gaming", "Work", "Streaming"), { usage = it }, Modifier.weight(1f))
            AiDropdownField("Resolution", resolution, listOf("1080p", "1440p", "4K"), { resolution = it }, Modifier.weight(1f))
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AiDropdownField("Brand", brand, listOf("Intel", "AMD", "NVIDIA", "No Preference"), { brand = it }, Modifier.weight(1f))
            AiDropdownField("Future Upgrade", upgrade, listOf("Yes", "No"), { upgrade = it }, Modifier.weight(1f))
        }

        Button(
            onClick = { onGenerate(budget, usage, resolution, brand, upgrade) },
            modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d3ee), contentColor = Color.Black)
        ) {
            Text("GENERATE AI BUILD", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AiDropdownField(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            colors = CardDefaults.outlinedCardColors(containerColor = Color.Transparent, contentColor = Color.White),
            border = BorderStroke(1.dp, Color.Gray)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(label, color = Color.Gray, fontSize = 10.sp)
                Text(selected, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xff1a1a1a))) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun AiRecommendationCard(rec: AiBuildRecommendation) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xff1e1b4b).copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color(0xff22d3ee).copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xff22d3ee), modifier = Modifier.size(20.dp))
                Text("AI RECOMMENDED BUILD", color = Color(0xff22d3ee), fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp)
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                AiPartRow("CPU", rec.cpu)
                AiPartRow("GPU", rec.gpu)
                AiPartRow("Board", rec.motherboard)
                AiPartRow("RAM", rec.ram)
                AiPartRow("PSU", rec.psu)
            }
            
            HorizontalDivider(color = Color(0xff22d3ee).copy(alpha = 0.1f))
            
            Text(
                text = rec.reason,
                color = Color(0xffbac9cc),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )
        }
    }
}

@Composable
fun AiPartRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "$label:", color = Color(0xff22d3ee).copy(alpha = 0.7f), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp))
        Text(text = value, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
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
                    imageVector = if (state.buildScore > 70) Icons.Default.CheckCircle else Icons.Default.Warning,
                    contentDescription = null,
                    tint = if (state.buildScore > 70) Color(0xff2ff801) else if (state.buildScore > 40) Color.Yellow else Color.Red
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape).background(Color(0xff353535))) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth((state.buildScore / 100f).coerceIn(0f, 1f))
                            .fillMaxHeight()
                            .background(Brush.horizontalGradient(listOf(Color(0xff00e5ff), Color(0xff2ff801))))
                    )
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("SYSTEM INTEGRITY", color = Color.Gray, fontSize = 10.sp)
                    Text("${state.buildScore}% ${state.buildStability}", color = if (state.buildScore > 70) Color(0xff2ff801) else if (state.buildScore > 40) Color.Yellow else Color.Red, fontSize = 10.sp, fontWeight = FontWeight.Bold)
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
                imageVector = if (result.isCompatible) Icons.Default.Check else Icons.Default.Close,
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
                if (items.isEmpty()) {
                    Text("No components found. Import data in Admin screen.", color = Color.Gray, fontSize = 14.sp, modifier = Modifier.padding(vertical = 20.dp))
                }
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

@Composable
fun FullBuildReportDialog(state: PCBuildState, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xff0a0a0a),
            border = BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("BUILD REPORT", color = Color(0xff00e5ff), style = TextStyle(fontSize = 14.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp))
                    IconButton(onClick = onDismiss, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.Close, contentDescription = null, tint = Color.Gray)
                    }
                }

                // Summary Items
                ReportItem("Processor", state.cpu?.name ?: "N/A")
                ReportItem("Graphics", state.gpu?.name ?: "N/A")
                ReportItem("Motherboard", state.motherboard?.name ?: "N/A")
                ReportItem("Memory", state.ram?.name ?: "N/A")
                ReportItem("Power Supply", state.psu?.name ?: "N/A")

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ReportStatusRow("CPU Socket", state.socketCompatible)
                    ReportStatusRow("RAM Type", state.ramCompatible)
                    ReportStatusRow("Power Supply", state.psuStatus)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xff1a1a1a))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text("ESTIMATED BUILD SCORE", color = Color.Gray, fontSize = 10.sp)
                        Text(state.buildScore.toString(), color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.Black)
                        Text(
                            text = if (state.buildScore > 75) "GAMING READY: YES" else "GAMING READY: NO",
                            color = if (state.buildScore > 75) Color(0xff2ff801) else Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReportItem(label: String, value: String) {
    Column {
        Text(label, color = Color(0xff71717a), fontSize = 10.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ReportStatusRow(label: String, result: CompatibilityResult) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.Gray, fontSize = 12.sp)
        Text(
            text = result.message,
            color = when(result.status) {
                CompatibilityResult.Status.OK -> Color(0xff2ff801)
                CompatibilityResult.Status.WARNING -> Color.Yellow
                CompatibilityResult.Status.ERROR -> Color.Red
            },
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
