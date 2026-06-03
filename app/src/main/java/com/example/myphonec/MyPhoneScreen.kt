package com.example.myphonec

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphonec.ui.components.Avatar
import com.example.myphonec.ui.theme.*

// ─────────────────────────────────────────────────────────────────────────────
// MyPhoneScreen — Redesigned UI, 100% same params as original
// ─────────────────────────────────────────────────────────────────────────────

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
    onNavigateToAdmin: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val deviceInfo by viewModel.deviceInfo.collectAsState()
    val authState  by authViewModel.authState.collectAsState()

    LaunchedEffect(authState.isLoggedIn, authState.isGuest) {
        if (!authState.isLoggedIn && !authState.isGuest && !authState.isLoading) {
            onNavigateToLogin()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SurfaceBase),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 120.dp),
            verticalArrangement = Arrangement.spacedBy(0.dp),
        ) {

            // ── TOP BAR ──────────────────────────────────────────────────
            AnimatedEntry(0) {
                PhoneTopBar(
                    authState          = authState,
                    onLeaderboard      = onNavigateToLeaderboard,
                    onAdmin            = onNavigateToAdmin,
                    onProfile          = onNavigateToProfile,
                )
            }

            Spacer(Modifier.height(20.dp))

            // ── GREETING ─────────────────────────────────────────────────
            AnimatedEntry(1) {
                GreetingSection(userName = authState.userName)
            }

            Spacer(Modifier.height(20.dp))

            // ── HERO CARD ─────────────────────────────────────────────────
            AnimatedEntry(2) {
                DeviceHeroCard(
                    deviceInfo         = deviceInfo,
                    onSystemModelClick = onNavigateToDetails,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── CORE SPECS ────────────────────────────────────────────────
            AnimatedEntry(3) {
                SectionHeaderRow(title = "Core Specs")
            }
            Spacer(Modifier.height(10.dp))
            AnimatedEntry(4) {
                CoreSpecsGrid(
                    deviceInfo       = deviceInfo,
                    onProcessorClick = onNavigateToProcessor,
                    onOsClick        = onNavigateToSystemDetails,
                )
            }

            Spacer(Modifier.height(24.dp))

            // ── DIAGNOSTIC CLUSTER ────────────────────────────────────────
            AnimatedEntry(5) {
                SectionHeaderRow(title = "Diagnostic Cluster", badge = "4 tests")
            }
            Spacer(Modifier.height(10.dp))
            AnimatedEntry(6) {
                DiagnosticGrid(
                    onScreen      = onNavigateToScreenTest,
                    onSensors     = onNavigateToSensors,
                    onBattery     = onNavigateToBattery,
                    onPerformance = onNavigateToPerformance,
                )
            }

            Spacer(Modifier.height(12.dp))

            // ── BENCHMARK CARD ────────────────────────────────────────────
            AnimatedEntry(7) {
                BenchmarkCard(onClick = onNavigateToBenchmark)
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TOP BAR
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun PhoneTopBar(
    authState: AuthState,
    onLeaderboard: () -> Unit,
    onAdmin: () -> Unit,
    onProfile: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Brand — nhỏ hơn, tinh tế hơn
        Text(
            text  = "PHONEC",
            color = CyanPrimary,
            style = LabelUppercase.copy(fontSize = 13.sp, letterSpacing = 2.2.sp),
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onAdmin() },
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Leaderboard icon button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(SurfaceLevel2)
                    .border(1.dp, BorderDefault, RoundedCornerShape(10.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                    ) { onLeaderboard() },
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    painter           = painterResource(id = R.drawable.score_board),
                    contentDescription = "Leaderboard",
                    tint              = TextSecondary,
                    modifier          = Modifier.size(18.dp),
                )
            }

            // Avatar — giữ component Avatar gốc, chỉ thêm border mới
            Box(
                modifier = Modifier
                    .border(1.dp, BorderDefault, CircleShape)
                    .padding(1.dp),
            ) {
                Avatar(
                    photoUrl    = authState.photoUrl,
                    displayName = authState.userName,
                    size        = 34.dp,
                    onClick     = onProfile,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// GREETING
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun GreetingSection(userName: String?) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val timeLabel = when {
        hour < 12 -> "Good morning"
        hour < 17 -> "Good afternoon"
        else      -> "Good evening"
    }
    val firstName = userName?.trim()?.split(" ")?.firstOrNull() ?: "there"

    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text  = "$timeLabel, $firstName",
            color = TextTertiary,
            style = LabelUppercase.copy(letterSpacing = 0.6.sp, fontSize = 11.sp),
        )
        Text(
            text  = "My Phone",
            color = TextPrimary,
            style = DisplayLarge,
        )
        Text(
            text  = "ACTIVE SESSION: ${userName?.uppercase() ?: "LOCAL DEVICE"}",
            color = TextTertiary,
            style = LabelUppercase.copy(fontSize = 9.sp, letterSpacing = 1.8.sp),
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DeviceHeroCard(
    deviceInfo: DeviceInfo,
    onSystemModelClick: () -> Unit,
) {
    val shape = RoundedCornerShape(28.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.985f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "hero_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(SurfaceLevel1)
            .border(1.dp, BorderDefault, shape)
            .drawBehind { drawHeroGlow() }
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onSystemModelClick,
            )
            .padding(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {

            Text(
                text  = "SYSTEM MODEL",
                color = TextTertiary,
                style = LabelUppercase,
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text     = deviceInfo.model,
                color    = TextPrimary,
                style    = DisplayLarge,
                maxLines = 2,
                overflow = TextOverflow.Clip,
            )

            Spacer(Modifier.height(12.dp))

            // Tags
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                HeroTag("Android ${deviceInfo.osVersion}")
                HeroTag(deviceInfo.processor.take(10))
            }

            Spacer(Modifier.height(20.dp))

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(BorderSubtle),
            )

            Spacer(Modifier.height(14.dp))

            // Memory + Storage row
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                HeroStat(label = "MEMORY",  value = deviceInfo.totalRam,     suffix = "RAM")
                HeroStat(label = "STORAGE", value = deviceInfo.totalStorage,  suffix = "TOTAL")
            }
        }
    }
}

private fun DrawScope.drawHeroGlow() {
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(CyanPrimary.copy(alpha = 0.09f), Color.Transparent),
            center = Offset.Zero,
            radius = size.width * 0.65f,
        ),
        radius = size.width * 0.65f,
        center = Offset.Zero,
    )
    drawCircle(
        brush = Brush.radialGradient(
            colors = listOf(Amber.copy(alpha = 0.04f), Color.Transparent),
            center = Offset(size.width, size.height),
            radius = size.width * 0.45f,
        ),
        radius = size.width * 0.45f,
        center = Offset(size.width, size.height),
    )
}

@Composable
private fun HeroTag(text: String) {
    Text(
        text     = text,
        style    = MonoSmall,
        color    = TextTertiary,
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(SurfaceLevel2)
            .border(1.dp, BorderSubtle, RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp),
    )
}

@Composable
private fun HeroStat(label: String, value: String, suffix: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(text = label,  color = TextTertiary,  style = LabelUppercase.copy(fontSize = 9.sp))
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(text = value,  color = TextPrimary,   style = MonoLarge.copy(fontSize = 18.sp))
            Text(text = suffix, color = TextSecondary, style = MonoSmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CORE SPECS GRID
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun CoreSpecsGrid(
    deviceInfo: DeviceInfo,
    onProcessorClick: () -> Unit,
    onOsClick: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SpecTile(
                label    = "Processor",
                value    = deviceInfo.processor,
                isMono   = false,
                onClick  = onProcessorClick,
                modifier = Modifier.weight(1f),
            )
            SpecTile(
                label      = "OS Version",
                value      = "Android ${deviceInfo.osVersion}",
                isMono     = false,
                valueColor = CyanInk,
                onClick    = onOsClick,
                modifier   = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SpecTile(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    isMono: Boolean = true,
    valueColor: Color = TextPrimary,
    onClick: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "spec_tile",
    )

    Column(
        modifier = modifier
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(SurfaceLevel1)
            .border(1.dp, BorderDefault, shape)
            .then(
                if (onClick != null) Modifier.clickable(
                    interactionSource = interactionSource,
                    indication        = null,
                ) { onClick() } else Modifier
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text  = label.uppercase(),
            color = TextTertiary,
            style = LabelUppercase,
        )
        Spacer(Modifier.height(2.dp))
        if (isMono) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = value, color = valueColor, style = MonoLarge)
                if (unit != null) {
                    Spacer(Modifier.width(3.dp))
                    Text(text = unit, color = TextSecondary, style = MonoSmall)
                }
            }
        } else {
            Text(
                text     = value,
                color    = valueColor,
                style    = MonoMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onClick != null) {
            Spacer(Modifier.height(2.dp))
            Text(text = "Tap to view →", color = TextDisabled, style = MonoSmall.copy(fontSize = 10.sp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// DIAGNOSTIC GRID — giữ nguyên iconId gốc (R.drawable.*)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DiagnosticGrid(
    onScreen: () -> Unit,
    onSensors: () -> Unit,
    onBattery: () -> Unit,
    onPerformance: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DiagnosticTile(
                title    = "Screen test",
                subtitle = "OLED INTEGRITY",
                iconId   = R.drawable.screen,
                onClick  = onScreen,
                modifier = Modifier.weight(1f),
            )
            DiagnosticTile(
                title    = "Sensors test",
                subtitle = "IMU CALIBRATION",
                iconId   = R.drawable.sensor,
                onClick  = onSensors,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DiagnosticTile(
                title    = "Battery",
                subtitle = "HEALTH & CYCLE",
                iconId   = R.drawable.battery,
                onClick  = onBattery,
                modifier = Modifier.weight(1f),
            )
            DiagnosticTile(
                title    = "Performance",
                subtitle = "FPS & RAM LIVE",
                iconId   = R.drawable.bottleneck,
                onClick  = onPerformance,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun DiagnosticTile(
    title: String,
    subtitle: String,
    iconId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(20.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.96f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "diag_tile",
    )

    Column(
        modifier = modifier
            .height(130.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(SurfaceLevel1)
            .border(1.dp, BorderDefault, shape)
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Icon trong box
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceLevel2)
                .border(1.dp, BorderSubtle, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                painter           = painterResource(id = iconId),
                contentDescription = title,
                tint              = TextSecondary,
                modifier          = Modifier.size(17.dp),
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text  = title,
                color = TextPrimary,
                style = TitleMedium.copy(fontSize = 13.sp),
            )
            Text(
                text  = subtitle,
                color = TextTertiary,
                style = MonoSmall.copy(fontSize = 10.sp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// BENCHMARK CARD — giữ nguyên icon gốc R.drawable.container
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BenchmarkCard(onClick: () -> Unit) {
    val shape = RoundedCornerShape(24.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue   = if (isPressed) 0.97f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label         = "bench_scale",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(shape)
            .background(SurfaceLevel1)
            .border(
                width = 1.dp,
                color = if (isPressed) CyanPrimary else BorderCyan,
                shape = shape,
            )
            .drawBehind {
                // Cyan glow mạnh hơn khi pressed
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF22D3EE).copy(alpha = if (isPressed) 0.14f else 0.06f),
                            Color.Transparent,
                        ),
                        center = Offset.Zero,
                        radius = size.width * 0.8f,
                    ),
                    radius = size.width * 0.8f,
                    center = Offset.Zero,
                )
            }
            .clickable(
                interactionSource = interactionSource,
                indication        = null,
                onClick           = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 20.dp),
    ) {
        Row(
            modifier              = Modifier.fillMaxSize(),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // "NEW" badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(CyanPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp),
                ) {
                    Text(
                        text  = "NEW",
                        color = SurfaceBase,
                        style = MonoSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                    )
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text  = "Benchmark Test",
                    color = TextPrimary,
                    style = TitleLarge.copy(fontSize = 20.sp),
                )
                Text(
                    text  = "GPU STRESS + FPS SCORE",
                    color = TextTertiary,
                    style = LabelUppercase.copy(letterSpacing = 1.sp),
                )
            }

            // Icon gốc giữ nguyên
            Icon(
                painter           = painterResource(id = R.drawable.container),
                contentDescription = null,
                tint              = CyanPrimary,
                modifier          = Modifier.size(44.dp),
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// SECTION HEADER ROW
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SectionHeaderRow(
    title: String,
    badge: String? = null,
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text     = title.uppercase(),
            color    = TextTertiary,
            style    = LabelUppercase,
            modifier = Modifier.weight(1f),
        )
        if (badge != null) {
            Text(text = badge, color = CyanPrimary, style = MonoSmall)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATED ENTRY — stagger fade + slide
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun AnimatedEntry(
    index: Int,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }
    val alpha by animateFloatAsState(
        targetValue   = if (visible) 1f else 0f,
        animationSpec = tween(260, easing = EaseOut),
        label         = "a$index",
    )
    val ty by animateFloatAsState(
        targetValue   = if (visible) 0f else 12f,
        animationSpec = tween(260, easing = EaseOut),
        label         = "t$index",
    )
    Box(modifier = Modifier.graphicsLayer { this.alpha = alpha; translationY = ty }) {
        content()
    }
}
