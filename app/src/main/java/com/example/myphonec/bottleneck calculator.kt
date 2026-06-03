package com.example.myphonec

import androidx.compose.foundation.background
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myphonec.ui.components.MonoNumber
import com.example.myphonec.ui.components.SectionHeader
import com.example.myphonec.ui.components.SegmentedControl
import com.example.myphonec.ui.components.SurfaceCard
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyLarge
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.DisplayMedium
import com.example.myphonec.ui.theme.LabelUppercase
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.MonoHero
import com.example.myphonec.ui.theme.MonoLarge
import com.example.myphonec.ui.theme.RadiusLg
import com.example.myphonec.ui.theme.TitleMedium

@Composable
fun BottleneckCalculatorScreen(
    onBackClick: () -> Unit,
    viewModel: BottleneckViewModel
) {
    val ui by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase)
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
            Header()
            ResolutionSelector(
                selected = ui.selectedResolution,
                onSelect = { viewModel.onResolutionSelected(it) },
            )
            PartSelectors(
                ui = ui,
                onCpu = { viewModel.onCpuSelected(it) },
                onGpu = { viewModel.onGpuSelected(it) },
            )
            CalculateButton(onClick = { viewModel.calculateBottleneck() })
            ui.result?.let { ResultBlock(it) }
            Spacer(modifier = Modifier.height(spacing.space48))
        }
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
        Text(
            text = "BOTTLENECK",
            style = LabelUppercase,
            color = colors.textTertiary,
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun Header() {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space8)) {
        Text(
            text = "Bottleneck",
            style = DisplayMedium,
            color = colors.textPrimary,
        )
        Text(
            text = "Estimate the balance between your CPU and GPU at a given resolution.",
            style = BodyLarge,
            color = colors.textSecondary,
        )
    }
}

@Composable
private fun ResolutionSelector(selected: Resolution, onSelect: (Resolution) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "Resolution")
        SegmentedControl(
            options = Resolution.entries.toList(),
            selected = selected,
            onSelect = onSelect,
            labelFor = { it.label },
        )
    }
}

@Composable
private fun PartSelectors(
    ui: BottleneckUIState,
    onCpu: (String) -> Unit,
    onGpu: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "Components")
        SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20)) {
            Column(modifier = Modifier.fillMaxWidth()) {
                PartDropdown(
                    label = "CPU",
                    value = ui.selectedCpu?.name ?: "Select processor",
                    options = ui.cpus,
                    optionLabel = { it.name },
                    onSelect = { onCpu(it.name) },
                )
                HorizontalDivider(thickness = 1.dp, color = AppTheme.colors.borderSubtle)
                PartDropdown(
                    label = "GPU",
                    value = ui.selectedGpu?.name ?: "Select graphics",
                    options = ui.gpus,
                    optionLabel = { it.name },
                    onSelect = { onGpu(it.name) },
                )
            }
        }
    }
}

@Composable
private fun <T> PartDropdown(
    label: String,
    value: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelect: (T) -> Unit,
) {
    val colors = AppTheme.colors
    var expanded by remember { mutableStateOf(false) }
    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = BodyMedium, color = colors.textSecondary)
            Text(
                text = value,
                style = Mono,
                color = colors.textPrimary,
                textAlign = TextAlign.End,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(colors.surfaceLevel2),
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option), style = BodyMedium, color = colors.textPrimary) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CalculateButton(onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.cyanPrimary, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 18.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(text = "Calculate", style = TitleMedium, color = colors.surfaceBase)
    }
}

@Composable
private fun ResultBlock(result: BottleneckResult) {
    val colors = AppTheme.colors
    val statusColor = Color(result.statusColor)

    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16)) {
        SectionHeader(title = "Analysis result", caption = result.direction)

        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space24)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16),
            ) {
                Text(text = "IMBALANCE", style = LabelUppercase, color = colors.textTertiary)
                MonoNumber(
                    text = "${result.percentage}%",
                    style = MonoHero,
                    color = colors.textPrimary,
                )
                Text(
                    text = result.status,
                    style = TitleMedium,
                    color = statusColor,
                )
                SeverityDots(percentage = result.percentage, activeColor = statusColor)
                Text(
                    text = result.description,
                    style = BodyMedium,
                    color = colors.textSecondary,
                    textAlign = TextAlign.Center,
                )
            }
        }

        LoadBars(cpuLoad = result.cpuLoad, gpuLoad = result.gpuLoad)
    }
}

@Composable
private fun SeverityDots(percentage: Int, activeColor: Color) {
    val colors = AppTheme.colors
    val level = when {
        percentage <= 5 -> 1
        percentage <= 12 -> 2
        percentage <= 20 -> 3
        percentage <= 30 -> 4
        else -> 5
    }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        for (i in 1..5) {
            val active = i <= level
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (active) activeColor else colors.surfaceLevel3)
            )
        }
    }
    Spacer(modifier = Modifier.height(2.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = "BALANCED", style = LabelUppercase, color = colors.textTertiary)
        Text(text = "SEVERE", style = LabelUppercase, color = colors.textTertiary)
    }
}

@Composable
private fun LoadBars(cpuLoad: Int, gpuLoad: Int) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
        SectionHeader(title = "Load")
        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space20)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16)) {
                LoadRow(label = "CPU", percent = cpuLoad)
                LoadRow(label = "GPU", percent = gpuLoad)
            }
        }
    }
}

@Composable
private fun LoadRow(label: String, percent: Int) {
    val colors = AppTheme.colors
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(text = label, style = LabelUppercase, color = colors.textTertiary)
            MonoNumber(
                text = "$percent%",
                style = MonoLarge.copy(fontSize = 18.sp),
                color = colors.textPrimary,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(colors.surfaceLevel3),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth((percent / 100f).coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(colors.cyanPrimary)
            )
        }
    }
}

