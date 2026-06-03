package com.example.myphonec

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Speed
import com.example.myphonec.ui.components.Avatar
import com.example.myphonec.ui.components.DataRowGroup
import com.example.myphonec.ui.components.EmptyState
import com.example.myphonec.ui.components.MetricTile
import com.example.myphonec.ui.components.MonoNumber
import com.example.myphonec.ui.components.SectionHeader
import com.example.myphonec.ui.components.Sparkline
import com.example.myphonec.ui.components.SurfaceCard
import com.example.myphonec.ui.components.appearOnEnter
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.DisplayMedium
import com.example.myphonec.ui.theme.LabelUppercase
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.RadiusFull
import com.example.myphonec.ui.theme.RadiusLg
import com.example.myphonec.ui.theme.ThemePreferences
import com.example.myphonec.ui.theme.TitleMedium
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    userProfileViewModel: UserProfileViewModel,
    onBack: () -> Unit,
    onSignOut: () -> Unit,
) {
    val authState by authViewModel.authState.collectAsState()
    val profile by userProfileViewModel.uiState.collectAsState()
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
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.sectionGap),
        ) {
            ProfileTopBar(onBack = onBack)
            Box(modifier = Modifier.appearOnEnter(delayMillis = 0)) {
                ProfileHero(
                    photoUrl = authState.photoUrl,
                    displayName = authState.userName,
                    email = authState.userEmail,
                    isGuest = authState.isGuest,
                )
            }
            Box(modifier = Modifier.appearOnEnter(delayMillis = 60)) {
                ProfileMetrics(profile = profile)
            }
            Box(modifier = Modifier.appearOnEnter(delayMillis = 120)) {
                PerformanceTrajectory(devices = profile.benchmarkedDevices)
            }
            Box(modifier = Modifier.appearOnEnter(delayMillis = 180)) {
                DevicesSection(devices = profile.benchmarkedDevices)
            }
            Box(modifier = Modifier.appearOnEnter(delayMillis = 240)) {
                AccountSection(
                    authState = authState,
                    onSignOut = onSignOut,
                )
            }
            Spacer(modifier = Modifier.height(spacing.space48))
        }
    }
}

@Composable
private fun ProfileTopBar(onBack: () -> Unit) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppTheme.spacing.space12),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IconButton(onClick = onBack) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = colors.textPrimary,
            )
        }
        Text(
            text = "PROFILE",
            style = LabelUppercase,
            color = colors.textTertiary,
        )
        Box(modifier = Modifier.size(48.dp)) // balance
    }
}

@Composable
private fun ProfileHero(
    photoUrl: String?,
    displayName: String?,
    email: String?,
    isGuest: Boolean,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Halo
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .blur(28.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                colors.amber.copy(alpha = 0.32f),
                                Color.Transparent,
                            )
                        )
                    )
            )
            Avatar(
                photoUrl = photoUrl,
                displayName = displayName,
                size = 96.dp,
            )
        }
        Spacer(modifier = Modifier.height(AppTheme.spacing.space4))
        Text(
            text = displayName ?: if (isGuest) "Guest" else "Unknown",
            style = DisplayMedium,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        if (!email.isNullOrBlank()) {
            Text(
                text = email,
                style = BodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
        }
        StatusLine(isGuest = isGuest)
    }
}

@Composable
private fun StatusLine(isGuest: Boolean) {
    val colors = AppTheme.colors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.space8),
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isGuest) colors.warning else colors.success)
        )
        Text(
            text = if (isGuest) "Guest session" else "Active session",
            style = Mono,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun ProfileMetrics(profile: UserProfileUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
    ) {
        MetricTile(
            label = "Devices",
            value = profile.totalDevices.toString(),
            caption = "tested",
            modifier = Modifier.weight(1f),
        )
        MetricTile(
            label = "Highest",
            value = profile.highestScore.toString(),
            caption = "score",
            modifier = Modifier.weight(1f),
        )
        MetricTile(
            label = "Total",
            value = profile.benchmarkedDevices.size.toString(),
            caption = "runs",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PerformanceTrajectory(devices: List<BenchmarkedDevice>) {
    val colors = AppTheme.colors
    val scores = devices
        .sortedBy { it.testedAt?.time ?: 0L }
        .map { it.score.toFloat() }

    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(
            title = "Performance trajectory",
            caption = if (scores.isEmpty()) "Run a benchmark to start tracking"
                      else "${scores.size} run${if (scores.size == 1) "" else "s"} · trending"
        )
        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space20)) {
            if (scores.size < 2) {
                Text(
                    text = "Not enough data yet.",
                    style = BodyMedium,
                    color = colors.textTertiary,
                )
            } else {
                Sparkline(
                    values = scores,
                    strokeColor = colors.cyanPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(96.dp),
                )
            }
        }
    }
}

@Composable
private fun DevicesSection(devices: List<BenchmarkedDevice>) {
    val colors = AppTheme.colors
    val dateFmt = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(
            title = "Devices benchmarked",
            caption = "${devices.size} device${if (devices.size == 1) "" else "s"}",
        )
        if (devices.isEmpty()) {
            EmptyState(
                icon = Icons.Default.Speed,
                title = "Nothing benchmarked yet",
                description = "Run your first benchmark to compare\nagainst the global leaderboard.",
            )
        } else {
            SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20)) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    devices.forEachIndexed { idx, device ->
                        DeviceRow(
                            device = device,
                            subtitle = "${device.chipset} · ${device.testedAt?.let { dateFmt.format(it) } ?: "—"}",
                        )
                        if (idx < devices.lastIndex) {
                            HorizontalDivider(
                                thickness = 1.dp,
                                color = colors.borderSubtle,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DeviceRow(device: BenchmarkedDevice, subtitle: String) {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.spacing.space16),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(RadiusFull))
                .background(colors.surfaceLevel2)
                .border(1.dp, colors.borderSubtle, RoundedCornerShape(RadiusFull)),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "PH", style = Mono.copy(fontSize = 11.sp), color = colors.textSecondary)
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = device.deviceModel,
                style = TitleMedium,
                color = colors.textPrimary,
                maxLines = 1,
            )
            Text(
                text = subtitle,
                style = BodyMedium,
                color = colors.textTertiary,
                maxLines = 1,
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            MonoNumber(text = device.score.toString(), color = colors.textPrimary)
            Text(text = "${device.fps} FPS", style = Mono, color = colors.textTertiary)
        }
    }
}

@Composable
private fun AccountSection(
    authState: AuthState,
    onSignOut: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "Account")
        SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20)) {
            DataRowGroup(
                rows = listOfNotNull(
                    "Linked Google".takeIf { !authState.isGuest }?.let { it to (authState.userEmail ?: "—") },
                    "Mode" to if (authState.isGuest) "Guest" else "Signed in",
                    "Data sync" to if (authState.isLoggedIn) "On" else "Off",
                )
            )
        }
        AppearanceToggle()
        GhostButton(label = "Sign out", onClick = onSignOut)
        DangerLink(label = "Delete account", onClick = { /* TODO */ })
        Text(
            text = "Deleting your account removes all benchmark history\nand cannot be undone.",
            style = BodyMedium,
            color = colors.textTertiary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun AppearanceToggle() {
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val context = LocalContext.current
    val isDark = ThemePreferences.isDark.value
    val shape = RoundedCornerShape(RadiusLg)

    Column(verticalArrangement = Arrangement.spacedBy(spacing.space8)) {
        Text(
            text = "APPEARANCE",
            style = LabelUppercase,
            color = colors.textTertiary,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(colors.surfaceLevel1)
                .border(1.dp, colors.borderSubtle, shape)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            AppearanceOption(
                label = "Dark",
                icon = Icons.Default.DarkMode,
                selected = isDark,
                onClick = { if (!isDark) ThemePreferences.setDark(context, true) },
                modifier = Modifier.weight(1f),
            )
            AppearanceOption(
                label = "Light",
                icon = Icons.Default.LightMode,
                selected = !isDark,
                onClick = { if (isDark) ThemePreferences.setDark(context, false) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun AppearanceOption(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    val bg = if (selected) colors.cyanWash else Color.Transparent
    val fg = if (selected) colors.cyanPrimary else colors.textSecondary
    Row(
        modifier = modifier
            .clip(shape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = fg,
            modifier = Modifier.size(18.dp),
        )
        Spacer(modifier = Modifier.width(AppTheme.spacing.space8))
        Text(text = label, style = TitleMedium, color = fg)
    }
}

@Composable
private fun GhostButton(label: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(1.dp, colors.borderDefault, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = TitleMedium,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun DangerLink(label: String, onClick: () -> Unit) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = BodyMedium,
            color = colors.danger,
        )
    }
}

