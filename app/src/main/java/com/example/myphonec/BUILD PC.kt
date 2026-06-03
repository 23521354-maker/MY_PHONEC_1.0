package com.example.myphonec

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.SettingsInputComponent
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphonec.ui.components.DataRowGroup
import com.example.myphonec.ui.components.MonoNumber
import com.example.myphonec.ui.components.SectionHeader
import com.example.myphonec.ui.components.StatusPill
import com.example.myphonec.ui.components.SurfaceCard
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyLarge
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.DisplayMedium
import com.example.myphonec.ui.theme.LabelUppercase
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.MonoHero
import com.example.myphonec.ui.theme.RadiusFull
import com.example.myphonec.ui.theme.RadiusLg
import com.example.myphonec.ui.theme.RadiusXl
import com.example.myphonec.ui.theme.TitleLarge
import com.example.myphonec.ui.theme.TitleMedium
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildRigScreen(
    onBackClick: () -> Unit,
    viewModel: PCBuilderViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
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
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
        ) {
            TopBar(onBackClick = onBackClick)

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
            ) {
                BuildRigHeader(state = state)

                ComponentTimeline(
                    state = state,
                    onCpu = { showCpuDialog = true },
                    onGpu = { showGpuDialog = true },
                    onMobo = { showMoboDialog = true },
                    onRam = { showRamDialog = true },
                    onPsu = { showPsuDialog = true },
                )

                aiRecommendation?.let { AiRecommendationCard(it) }

                CompatibilityBlock(state = state)

                ReportButton(
                    enabled = state.cpu != null || state.gpu != null,
                    onClick = { showReportDialog = true },
                )

                Spacer(modifier = Modifier.height(96.dp))
            }
        }

        AiAssistantFab(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 32.dp, end = 24.dp),
            onClick = { showAiChat = true }
        )

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

    if (showAiSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAiSheet = false },
            sheetState = sheetState,
            containerColor = colors.surfaceLevel1,
            contentColor = colors.textPrimary
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

    if (showCpuDialog) ComponentSelectionDialog("Select processor", cpuList, { viewModel.selectCPU(it as CPU); showCpuDialog = false }, { showCpuDialog = false })
    if (showGpuDialog) ComponentSelectionDialog("Select GPU", gpuList, { viewModel.selectGPU(it as GPU); showGpuDialog = false }, { showGpuDialog = false })
    if (showMoboDialog) ComponentSelectionDialog("Select motherboard", motherboardList, { viewModel.selectMotherboard(it as Motherboard); showMoboDialog = false }, { showMoboDialog = false })
    if (showRamDialog) ComponentSelectionDialog("Select memory", ramList, { viewModel.selectRAM(it as RAM); showRamDialog = false }, { showRamDialog = false })
    if (showPsuDialog) ComponentSelectionDialog("Select power supply", psuList, { viewModel.selectPSU(it as PSU); showPsuDialog = false }, { showPsuDialog = false })

    if (showReportDialog) {
        FullBuildReportDialog(state = state, onDismiss = { showReportDialog = false })
    }
}

@Composable
private fun TopBar(onBackClick: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppTheme.spacing.space12, vertical = AppTheme.spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
            )
        }
        Text(text = "BUILD RIG", style = LabelUppercase, color = colors.textTertiary)
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun BuildRigHeader(state: PCBuildState) {
    val colors = AppTheme.colors
    val selectedCount = listOfNotNull(state.cpu, state.gpu, state.motherboard, state.ram, state.psu).size
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space8)) {
        Text(text = "Build rig", style = DisplayMedium, color = colors.textPrimary)
        Text(
            text = "Configuration draft · $selectedCount of 5 selected",
            style = BodyMedium,
            color = colors.textSecondary,
        )
    }
}

// ─────────────────────────────────────────────────────────────
// Timeline of 5 component slots
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComponentTimeline(
    state: PCBuildState,
    onCpu: () -> Unit,
    onGpu: () -> Unit,
    onMobo: () -> Unit,
    onRam: () -> Unit,
    onPsu: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "Core components")
        SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20, vertical = AppTheme.spacing.space8)) {
            Column {
                ComponentSlot(
                    label = "Processor",
                    value = state.cpu?.name,
                    detail = state.cpu?.let { "Socket ${it.socket} · ${it.cores}C/${it.threads}T · ${it.tdp}W" },
                    icon = Icons.Default.Memory,
                    selected = state.cpu != null,
                    isFirst = true,
                    onClick = onCpu,
                )
                ComponentSlot(
                    label = "Graphics card",
                    value = state.gpu?.name,
                    detail = state.gpu?.let { "${it.vram} GB · ${it.tdp}W" },
                    icon = Icons.Default.DeveloperBoard,
                    selected = state.gpu != null,
                    onClick = onGpu,
                )
                ComponentSlot(
                    label = "Motherboard",
                    value = state.motherboard?.name,
                    detail = state.motherboard?.let { "Socket ${it.socket} · ${it.ramType}" },
                    icon = Icons.Default.SettingsInputComponent,
                    selected = state.motherboard != null,
                    onClick = onMobo,
                )
                ComponentSlot(
                    label = "Memory",
                    value = state.ram?.name,
                    detail = state.ram?.let { it.type },
                    icon = Icons.Default.Dns,
                    selected = state.ram != null,
                    onClick = onRam,
                )
                ComponentSlot(
                    label = "Power supply",
                    value = state.psu?.name,
                    detail = state.psu?.let { "${it.watt} W" },
                    icon = Icons.Default.Power,
                    selected = state.psu != null,
                    isLast = true,
                    onClick = onPsu,
                )
            }
        }
    }
}

@Composable
private fun ComponentSlot(
    label: String,
    value: String?,
    detail: String?,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    isFirst: Boolean = false,
    isLast: Boolean = false,
) {
    val colors = AppTheme.colors

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top,
    ) {
        // Timeline column
        Column(
            modifier = Modifier
                .width(28.dp)
                .padding(top = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(16.dp)
                        .background(colors.borderSubtle)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(if (selected) colors.cyanPrimary else colors.surfaceLevel3)
                    .border(1.dp, if (selected) colors.cyanPrimary else colors.borderDefault, CircleShape)
            )
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(40.dp)
                        .background(colors.borderSubtle)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colors.cyanWash else colors.surfaceLevel2),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) colors.cyanInk else colors.textSecondary,
                    modifier = Modifier.size(18.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(text = label.uppercase(), style = LabelUppercase, color = colors.textTertiary)
                Text(
                    text = value ?: "Choose component",
                    style = TitleMedium,
                    color = if (value != null) colors.textPrimary else colors.textTertiary,
                    maxLines = 1,
                )
                if (!detail.isNullOrBlank()) {
                    Text(text = detail, style = Mono.copy(fontSize = 11.sp), color = colors.textTertiary, maxLines = 1)
                }
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textTertiary,
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Compatibility block
// ─────────────────────────────────────────────────────────────

@Composable
private fun CompatibilityBlock(state: PCBuildState) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "Compatibility", caption = state.buildStability)
        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space20)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space20)) {
                BuildScoreBlock(score = state.buildScore, stability = state.buildStability)
                HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CheckRow("CPU socket", state.socketCompatible)
                    CheckRow("Memory type", state.ramCompatible)
                    CheckRow("Power supply", state.psuStatus)
                }
                if (state.totalTdp > 0) {
                    HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(text = "Estimated draw", style = BodyMedium, color = colors.textSecondary)
                        MonoNumber(text = "${state.totalTdp} W", color = colors.textPrimary)
                    }
                }
            }
        }
    }
}

@Composable
private fun BuildScoreBlock(score: Int, stability: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = "BUILD SCORE", style = LabelUppercase, color = colors.textTertiary)
            Row(verticalAlignment = Alignment.Bottom) {
                MonoNumber(text = score.toString(), style = MonoHero, color = colors.textPrimary)
                MonoNumber(text = "/100", style = Mono.copy(fontSize = 16.sp), color = colors.textTertiary)
            }
        }
        StatusPill(
            label = stability,
            color = when (score) {
                in 71..100 -> colors.success
                in 41..70 -> colors.warning
                else -> colors.danger
            },
        )
    }
}

@Composable
private fun CheckRow(label: String, result: CompatibilityResult) {
    val colors = AppTheme.colors
    val tone = when (result.status) {
        CompatibilityResult.Status.OK -> colors.success
        CompatibilityResult.Status.WARNING -> colors.warning
        CompatibilityResult.Status.ERROR -> colors.danger
    }
    val pillShape = RoundedCornerShape(RadiusFull)
    val pillTextStyle = BodyMedium.copy(
        fontSize = 11.sp,
        letterSpacing = 0.3.sp,
        fontWeight = FontWeight.SemiBold,
    )
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = BodyMedium,
            color = colors.textSecondary,
            modifier = Modifier.width(110.dp),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .clip(pillShape)
                .background(tone.copy(alpha = 0.12f), pillShape)
                .border(1.dp, tone.copy(alpha = 0.24f), pillShape)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = result.message,
                style = pillTextStyle,
                color = tone,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ReportButton(enabled: Boolean, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    val bg = if (enabled) colors.cyanPrimary else colors.cyanPrimary.copy(alpha = 0.3f)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(bg, shape)
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "View full report", style = TitleMedium, color = colors.surfaceBase)
    }
}

// ─────────────────────────────────────────────────────────────
// AI Assistant
// ─────────────────────────────────────────────────────────────

@Composable
private fun AiAssistantFab(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .background(colors.cyanWash)
            .border(1.dp, colors.cyanPrimary.copy(alpha = 0.4f), CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "AI Assistant",
            tint = colors.cyanPrimary,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
private fun AiRecommendationCard(rec: AiBuildRecommendation) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "AI recommendation", caption = "Auto-applied to your config")
        SurfaceCard(
            background = colors.surfaceLevel1,
            borderColor = colors.cyanPrimary.copy(alpha = 0.3f),
            contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20, vertical = AppTheme.spacing.space20),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = colors.cyanPrimary,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(text = "GEMINI", style = LabelUppercase, color = colors.cyanPrimary)
                }
                DataRowGroup(
                    rows = listOf(
                        "CPU" to rec.cpu.ifBlank { "—" },
                        "GPU" to rec.gpu.ifBlank { "—" },
                        "Motherboard" to rec.motherboard.ifBlank { "—" },
                        "Memory" to rec.ram.ifBlank { "—" },
                        "PSU" to rec.psu.ifBlank { "—" },
                    )
                )
                if (rec.reason.isNotBlank()) {
                    Text(text = rec.reason, style = BodyMedium, color = colors.textSecondary)
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AI Chat overlay (cosmetic restyle)
// ─────────────────────────────────────────────────────────────

@Composable
private fun AiChatOverlay(
    viewModel: PCBuilderViewModel,
    onDismiss: () -> Unit
) {
    val messages by viewModel.chatMessages.collectAsState()
    val isLoading by viewModel.isChatLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val colors = AppTheme.colors

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase.copy(alpha = 0.96f))
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
                    .padding(AppTheme.spacing.space20),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(colors.cyanWash),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.cyanPrimary, modifier = Modifier.size(18.dp))
                    }
                    Column {
                        Text(text = "Build assistant", style = TitleLarge, color = colors.textPrimary)
                        Text(text = "Powered by Gemini", style = Mono.copy(fontSize = 11.sp), color = colors.cyanPrimary)
                    }
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = AppTheme.spacing.screenHorizontal),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
            ) {
                items(messages) { message -> ChatBubble(message) }
                if (isLoading) {
                    item { AiTypingIndicator() }
                }
            }

            // Input
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surfaceLevel1)
                    .border(width = 1.dp, color = colors.borderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .padding(12.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { inputText = it },
                        placeholder = { Text("Ask about your build…", style = BodyMedium, color = colors.textTertiary) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(RadiusLg),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedTextColor = colors.textPrimary,
                            focusedTextColor = colors.textPrimary,
                            unfocusedContainerColor = colors.surfaceLevel2,
                            focusedContainerColor = colors.surfaceLevel2,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = colors.cyanPrimary.copy(alpha = 0.4f),
                        ),
                        maxLines = 4
                    )
                    val sendActive = inputText.isNotBlank() && !isLoading
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(if (sendActive) colors.cyanPrimary else colors.surfaceLevel2)
                            .clickable(enabled = sendActive) {
                                viewModel.sendMessage(inputText)
                                inputText = ""
                            },
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = if (sendActive) colors.surfaceBase else colors.textTertiary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val colors = AppTheme.colors
    val isUser = message.isUser
    val bg = if (isUser) colors.cyanPrimary else colors.surfaceLevel2
    val textColor = if (isUser) colors.surfaceBase else colors.textPrimary
    val shape = if (isUser) {
        RoundedCornerShape(RadiusLg, RadiusLg, 4.dp, RadiusLg)
    } else {
        RoundedCornerShape(RadiusLg, RadiusLg, RadiusLg, 4.dp)
    }
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bg, shape)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(text = message.text, style = BodyMedium, color = textColor)
        }
    }
}

@Composable
private fun AiTypingIndicator() {
    val colors = AppTheme.colors
    val transition = rememberInfiniteTransition(label = "typing")
    Row(
        modifier = Modifier
            .background(colors.surfaceLevel2, RoundedCornerShape(RadiusLg, RadiusLg, RadiusLg, 4.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        repeat(3) { i ->
            val alpha by transition.animateFloat(
                initialValue = 0.3f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(600, delayMillis = i * 200),
                    repeatMode = RepeatMode.Reverse,
                ),
                label = "dot$i",
            )
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(colors.cyanPrimary.copy(alpha = alpha))
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────
// AI Config form (modal sheet)
// ─────────────────────────────────────────────────────────────

@Composable
private fun AiConfigForm(onGenerate: (String, String, String, String, String) -> Unit) {
    val colors = AppTheme.colors
    var budget by remember { mutableStateOf("1000") }
    var usage by remember { mutableStateOf("Gaming") }
    var resolution by remember { mutableStateOf("1440p") }
    var brand by remember { mutableStateOf("No preference") }
    var upgrade by remember { mutableStateOf("Yes") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.spacing.space24)
            .padding(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16),
    ) {
        Text(text = "AI build configurator", style = TitleLarge, color = colors.textPrimary)
        OutlinedTextField(
            value = budget,
            onValueChange = { budget = it },
            label = { Text("Budget (USD)", style = BodyMedium, color = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(RadiusLg),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedTextColor = colors.textPrimary,
                focusedTextColor = colors.textPrimary,
                unfocusedContainerColor = colors.surfaceLevel2,
                focusedContainerColor = colors.surfaceLevel2,
                unfocusedBorderColor = colors.borderSubtle,
                focusedBorderColor = colors.cyanPrimary.copy(alpha = 0.5f),
            )
        )
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AiDropdownField("Usage", usage, listOf("Gaming", "Work", "Streaming"), { usage = it }, Modifier.weight(1f))
            AiDropdownField("Resolution", resolution, listOf("1080p", "1440p", "4K"), { resolution = it }, Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AiDropdownField("Brand", brand, listOf("Intel", "AMD", "NVIDIA", "No preference"), { brand = it }, Modifier.weight(1f))
            AiDropdownField("Future upgrade", upgrade, listOf("Yes", "No"), { upgrade = it }, Modifier.weight(1f))
        }
        val shape = RoundedCornerShape(RadiusLg)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.cyanPrimary, shape)
                .clickable { onGenerate(budget, usage, resolution, brand, upgrade) }
                .padding(vertical = 18.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Generate AI build", style = TitleMedium, color = colors.surfaceBase)
        }
    }
}

@Composable
private fun AiDropdownField(
    label: String,
    selected: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RadiusLg))
                .background(colors.surfaceLevel2)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(RadiusLg))
                .clickable { expanded = true }
                .padding(12.dp),
        ) {
            Text(text = label.uppercase(), style = LabelUppercase, color = colors.textTertiary)
            Text(text = selected, style = TitleMedium, color = colors.textPrimary)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surfaceLevel2),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, style = BodyMedium, color = colors.textPrimary) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────
// Dialogs
// ─────────────────────────────────────────────────────────────

@Composable
private fun ComponentSelectionDialog(
    title: String,
    items: List<PCComponent>,
    onSelect: (PCComponent) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 500.dp)
                .clip(RoundedCornerShape(RadiusXl))
                .background(colors.surfaceLevel1)
                .border(1.dp, colors.borderDefault, RoundedCornerShape(RadiusXl))
                .padding(AppTheme.spacing.space20),
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = title, style = TitleLarge, color = colors.textPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }
                Spacer(modifier = Modifier.height(AppTheme.spacing.space12))
                if (items.isEmpty()) {
                    Text(
                        text = "No components found. Import data in Admin screen.",
                        style = BodyMedium,
                        color = colors.textTertiary,
                        modifier = Modifier.padding(vertical = 20.dp),
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        items(items) { item ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelect(item) }
                                    .padding(vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = item.name, style = TitleMedium, color = colors.textPrimary, maxLines = 1)
                                    Text(text = item.brand, style = BodyMedium, color = colors.textTertiary, maxLines = 1)
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = colors.textTertiary,
                                )
                            }
                            HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FullBuildReportDialog(state: PCBuildState, onDismiss: () -> Unit) {
    val colors = AppTheme.colors
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(RadiusXl))
                .background(colors.surfaceLevel1)
                .border(1.dp, colors.borderDefault, RoundedCornerShape(RadiusXl))
                .padding(AppTheme.spacing.space24),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space20)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "Build report", style = TitleLarge, color = colors.textPrimary)
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = colors.textSecondary)
                    }
                }
                DataRowGroup(
                    rows = listOf(
                        "Processor" to (state.cpu?.name ?: "—"),
                        "Graphics" to (state.gpu?.name ?: "—"),
                        "Motherboard" to (state.motherboard?.name ?: "—"),
                        "Memory" to (state.ram?.name ?: "—"),
                        "Power supply" to (state.psu?.name ?: "—"),
                    )
                )
                HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CheckRow("CPU socket", state.socketCompatible)
                    CheckRow("Memory type", state.ramCompatible)
                    CheckRow("Power supply", state.psuStatus)
                }
                HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(text = "ESTIMATED BUILD SCORE", style = LabelUppercase, color = colors.textTertiary)
                    MonoNumber(text = state.buildScore.toString(), style = MonoHero, color = colors.textPrimary)
                    Text(
                        text = if (state.buildScore > 75) "Gaming ready" else "Needs balancing",
                        style = BodyLarge,
                        color = if (state.buildScore > 75) colors.success else colors.warning,
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}
