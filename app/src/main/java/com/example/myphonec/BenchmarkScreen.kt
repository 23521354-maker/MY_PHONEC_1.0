package com.example.myphonec

import android.opengl.GLSurfaceView
import android.view.Choreographer
import android.view.ViewGroup
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myphonec.ui.components.DataRowGroup
import com.example.myphonec.ui.components.MonoNumber
import com.example.myphonec.ui.components.SectionHeader
import com.example.myphonec.ui.components.Sparkline
import com.example.myphonec.ui.components.SurfaceCard
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyLarge
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.DisplayLarge
import com.example.myphonec.ui.theme.LabelUppercase
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.MonoHero
import com.example.myphonec.ui.theme.MonoLarge
import com.example.myphonec.ui.theme.RadiusLg
import com.example.myphonec.ui.theme.TitleMedium
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

@Composable
fun BenchmarkScreen(
    onBackClick: () -> Unit,
    viewModel: BenchmarkViewModel
) {
    val state by viewModel.benchmarkState.collectAsState()
    val colors = AppTheme.colors

    val context = LocalContext.current
    val renderer = remember { BenchmarkRenderer(context) }

    LaunchedEffect(renderer) {
        viewModel.attachRenderer(renderer)
    }
    DisposableEffect(renderer) {
        onDispose { viewModel.detachRenderer() }
    }

    // Stall watchdog: if no frames for 2s during an active phase, surface an error.
    // Choreographer is only used as a "is the view tree alive" tick — FPS itself
    // is measured from real GL frame deltas in the renderer.
    val currentPhase = rememberUpdatedState(state.phase)
    DisposableEffect(renderer) {
        val choreographer = Choreographer.getInstance()
        var initialized = false
        var lastSeenCount = 0L
        var lastIncrementNs = 0L
        var reported = false
        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(now: Long) {
                if (reported) return
                renderer.lastError?.let {
                    reported = true
                    viewModel.reportError(it)
                    return
                }
                if (!initialized) {
                    initialized = true
                    lastIncrementNs = now
                    lastSeenCount = renderer.frameCounter.get()
                    choreographer.postFrameCallback(this)
                    return
                }
                val phase = currentPhase.value
                val active = phase == BenchmarkPhase.WARMUP || phase == BenchmarkPhase.MEASUREMENT
                if (active) {
                    val cur = renderer.frameCounter.get()
                    if (cur != lastSeenCount) {
                        lastSeenCount = cur
                        lastIncrementNs = now
                    } else if (now - lastIncrementNs > 2_000_000_000L) {
                        reported = true
                        viewModel.reportError("Benchmark stalled — no frames in 2s")
                        return
                    }
                } else {
                    lastIncrementNs = now
                    lastSeenCount = renderer.frameCounter.get()
                }
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
        onDispose { choreographer.removeFrameCallback(callback) }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase)
    ) {
        val glActive = state.phase == BenchmarkPhase.WARMUP ||
                state.phase == BenchmarkPhase.MEASUREMENT ||
                state.phase == BenchmarkPhase.SCORING
        if (glActive) {
            BenchmarkGLView(renderer)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            BenchmarkTopBar(onBackClick = onBackClick, status = topBarStatus(state.phase))

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        horizontal = AppTheme.spacing.screenHorizontal,
                        vertical = AppTheme.spacing.space20,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                when (state.phase) {
                    BenchmarkPhase.IDLE -> StartView(onStart = { viewModel.startBenchmark() })
                    BenchmarkPhase.WARMUP -> WarmupView(state = state)
                    BenchmarkPhase.MEASUREMENT -> MeasurementView(state = state)
                    BenchmarkPhase.SCORING -> ScoringView(progress = state.progress)
                    BenchmarkPhase.RESULT -> ResultView(
                        result = state.result ?: BenchmarkResult(),
                        isUploading = state.isUploading,
                        uploadSuccess = state.uploadSuccess,
                        error = state.error,
                        onRunAgain = { viewModel.reset() },
                        onSave = { state.result?.let(viewModel::uploadResult) },
                    )
                    BenchmarkPhase.ERROR -> ErrorView(
                        message = state.error ?: "Unknown error",
                        onRetry = { viewModel.reset() },
                    )
                }
            }
        }
    }
}

private fun topBarStatus(phase: BenchmarkPhase): String = when (phase) {
    BenchmarkPhase.IDLE -> "STANDBY"
    BenchmarkPhase.WARMUP -> "WARMUP"
    BenchmarkPhase.MEASUREMENT -> "MEASURING"
    BenchmarkPhase.SCORING -> "CALCULATING"
    BenchmarkPhase.RESULT -> "RESULT"
    BenchmarkPhase.ERROR -> "ERROR"
}

@Composable
private fun BenchmarkGLView(renderer: BenchmarkRenderer) {
    AndroidView(
        factory = { ctx ->
            GLSurfaceView(ctx).apply {
                setEGLContextClientVersion(3)
                // Custom chooser so we can request a GLES3-renderable config explicitly.
                // The convenience overload assumes GLES2 by default on some devices.
                setEGLConfigChooser(object : GLSurfaceView.EGLConfigChooser {
                    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
                        val attribs = intArrayOf(
                            EGL10.EGL_RED_SIZE, 8,
                            EGL10.EGL_GREEN_SIZE, 8,
                            EGL10.EGL_BLUE_SIZE, 8,
                            EGL10.EGL_ALPHA_SIZE, 8,
                            EGL10.EGL_DEPTH_SIZE, 16,
                            EGL10.EGL_RENDERABLE_TYPE, 0x40, // EGL_OPENGL_ES3_BIT
                            EGL10.EGL_NONE
                        )
                        val configs = arrayOfNulls<EGLConfig>(1)
                        val num = IntArray(1)
                        egl.eglChooseConfig(display, attribs, configs, 1, num)
                        return configs[0] ?: error("No EGL config")
                    }
                })
                preserveEGLContextOnPause = false
                setRenderer(renderer)
                renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
private fun BenchmarkTopBar(onBackClick: () -> Unit, status: String) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "GPU BENCHMARK", style = LabelUppercase, color = colors.textTertiary)
            Text(text = status, style = Mono.copy(fontSize = 11.sp), color = colors.cyanPrimary)
        }
        Spacer(modifier = Modifier.width(48.dp))
    }
}

// ─── IDLE ─────────────────────────────────────────────────────────────────────

@Composable
private fun StartView(onStart: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space24),
    ) {
        Text(text = "READY", style = LabelUppercase, color = colors.textTertiary)
        Text(text = "Mobile GPU Stress", style = DisplayLarge, color = colors.textPrimary)
        Text(
            text = "15s warmup + 60s measurement.\n" +
                "32-octave FBM + multi-light shading. Sustained-performance score includes thermal throttle.",
            style = BodyLarge,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.space16))
        PrimaryCta(label = "Start benchmark", onClick = onStart)
    }
}

@Composable
private fun PrimaryCta(label: String, onClick: () -> Unit) {
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
        Text(text = label, style = TitleMedium, color = colors.surfaceBase)
    }
}

// ─── WARMUP ───────────────────────────────────────────────────────────────────

@Composable
private fun WarmupView(state: BenchmarkState) {
    val colors = AppTheme.colors
    val tick by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(Spring.DampingRatioLowBouncy, Spring.StiffnessMedium),
        label = "warmup",
    )
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space24)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space20)) {
                Text(text = "WARMING UP — NOT MEASURED", style = LabelUppercase, color = colors.cyanPrimary)
                MonoNumber(
                    text = "${state.remainingSeconds}s",
                    style = MonoHero.copy(fontSize = 88.sp),
                    color = colors.textPrimary,
                    modifier = Modifier.scale(tick),
                )
                Text(
                    text = "GPU is reaching steady clock & thermal state.\nFPS is not being measured yet.",
                    style = BodyMedium,
                    color = colors.textSecondary,
                )
                UsageBars(state.systemUsage)
                HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                ProgressLine(progress = state.progress)
            }
        }
    }
}

// ─── MEASUREMENT ──────────────────────────────────────────────────────────────

@Composable
private fun MeasurementView(state: BenchmarkState) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
    ) {
        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space24)) {
            Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space20)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "BENCHMARKING", style = LabelUppercase, color = colors.cyanPrimary)
                    Text(
                        text = "${state.remainingSeconds}s left",
                        style = Mono.copy(fontSize = 12.sp),
                        color = colors.textTertiary,
                    )
                }
                MonoNumber(
                    text = state.currentFps.toString(),
                    style = MonoHero,
                    color = colors.textPrimary,
                )
                Text(text = "FPS", style = LabelUppercase, color = colors.textTertiary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.space24),
                ) {
                    StatBlock("AVG", String.format("%.1f", state.averageFps), Modifier.weight(1f))
                    StatBlock("MIN", state.minFps.toString(), Modifier.weight(1f))
                    StatBlock("MAX", state.maxFps.toString(), Modifier.weight(1f))
                }
                Sparkline(
                    values = state.fpsHistory.map { it.toFloat() },
                    strokeColor = colors.cyanPrimary,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                )
                UsageBars(state.systemUsage)
                HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
                ProgressLine(progress = state.progress)
            }
        }
    }
}

@Composable
private fun UsageBars(usage: SystemUsage) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space8)) {
        UsageRow("CPU", usage.cpuPercent)
        UsageRow("GPU", usage.gpuPercent)
        UsageRow("RAM", usage.ramPercent)
    }
}

@Composable
private fun UsageRow(label: String, percent: Int) {
    val colors = AppTheme.colors
    val available = percent >= 0
    val p = percent.coerceIn(0, 100)
    val barColor = when {
        p >= 85 -> colors.danger
        p >= 60 -> colors.cyanPrimary
        else -> colors.cyanPrimary.copy(alpha = 0.7f)
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = label,
            style = LabelUppercase,
            color = colors.textTertiary,
            modifier = Modifier.width(36.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.surfaceLevel3),
        ) {
            if (available) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(p / 100f)
                        .height(6.dp)
                        .background(barColor),
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = if (available) "$p%" else "N/A",
            style = Mono.copy(fontSize = 11.sp),
            color = if (available) colors.textSecondary else colors.textTertiary,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = AppTheme.colors
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space4)) {
        Text(text = label, style = LabelUppercase, color = colors.textTertiary)
        MonoNumber(text = value, style = MonoLarge.copy(fontSize = 20.sp), color = colors.textPrimary)
    }
}

@Composable
private fun ProgressLine(progress: Float) {
    val colors = AppTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(colors.surfaceLevel3),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .height(2.dp)
                .background(colors.cyanPrimary),
        )
    }
}

// ─── SCORING ──────────────────────────────────────────────────────────────────

@Composable
private fun ScoringView(progress: Float) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16),
    ) {
        CircularProgressIndicator(color = colors.cyanPrimary)
        Text(text = "CALCULATING", style = LabelUppercase, color = colors.textTertiary)
        Text(
            text = "Aggregating FPS samples & thermal segments…",
            style = BodyMedium,
            color = colors.textSecondary,
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.space8))
        ProgressLine(progress = progress)
    }
}

// ─── RESULT ───────────────────────────────────────────────────────────────────

@Composable
private fun ResultView(
    result: BenchmarkResult,
    isUploading: Boolean,
    uploadSuccess: Boolean,
    error: String?,
    onRunAgain: () -> Unit,
    onSave: () -> Unit,
) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space24),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SurfaceCard(contentPadding = PaddingValues(AppTheme.spacing.space24)) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
            ) {
                Text(text = "FINAL SCORE", style = LabelUppercase, color = colors.textTertiary)
                MonoNumber(
                    text = result.score.toString(),
                    style = MonoHero,
                    color = colors.textPrimary,
                )
            }
        }

        if (error != null) {
            Text(
                text = error,
                style = BodyMedium,
                color = colors.danger,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SectionHeader(title = "Device", caption = result.deviceName)
            SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20)) {
                DataRowGroup(
                    rows = listOf(
                        "Manufacturer" to (result.deviceName.split(" ").firstOrNull() ?: "—"),
                        "Model" to (result.deviceName.split(" ").drop(1).joinToString(" ").ifBlank { "—" }),
                        "GPU engine" to result.gpuName.ifBlank { "Unknown" },
                    )
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SectionHeader(title = "Performance stats")
            SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20)) {
                DataRowGroup(
                    rows = listOf(
                        "Average FPS" to "%.1f".format(result.averageFps),
                        "Min / Max FPS" to "%.1f / %.1f".format(result.minFps, result.maxFps),
                        "1% Low FPS" to "%.1f".format(result.p1LowFps),
                        "Stability" to "%.0f%%".format(result.stability * 100f),
                        "Total frames" to result.totalFrames.toString(),
                    )
                )
            }
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
            modifier = Modifier.fillMaxWidth(),
        ) {
            SectionHeader(
                title = "Thermal stability",
                caption = thermalCaption(result.sustainedRatio),
            )
            SurfaceCard(contentPadding = PaddingValues(horizontal = AppTheme.spacing.space20)) {
                DataRowGroup(
                    rows = listOf(
                        "Sustained" to "%.0f%%".format(result.sustainedRatio * 100f),
                        "First 10s avg" to "%.1f fps".format(result.firstSegFps),
                        "Last 10s avg" to "%.1f fps".format(result.lastSegFps),
                    )
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
        ) {
            GhostAction(
                text = "Run again",
                icon = Icons.Default.Refresh,
                onClick = onRunAgain,
                modifier = Modifier.weight(1f),
            )
            PrimaryAction(
                text = when {
                    isUploading -> "Saving…"
                    uploadSuccess -> "Saved"
                    else -> "Save result"
                },
                icon = if (uploadSuccess) Icons.Default.Check else Icons.Default.Save,
                isLoading = isUploading,
                enabled = !isUploading && !uploadSuccess,
                onClick = onSave,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(AppTheme.spacing.space24))
    }
}

private fun thermalCaption(sustainedRatio: Float): String = when {
    sustainedRatio <= 0f -> "Insufficient data"
    sustainedRatio >= 0.9f -> "Excellent — minimal throttle"
    sustainedRatio >= 0.7f -> "Normal — moderate throttle"
    else -> "Severe throttle"
}

@Composable
private fun GhostAction(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    Row(
        modifier = modifier
            .clip(shape)
            .border(1.dp, colors.borderDefault, shape)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, style = TitleMedium, color = colors.textSecondary)
    }
}

@Composable
private fun PrimaryAction(
    text: String,
    icon: ImageVector,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    val bg = if (enabled) colors.cyanPrimary else colors.cyanPrimary.copy(alpha = 0.4f)
    Row(
        modifier = modifier
            .clip(shape)
            .background(bg, shape)
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(vertical = 16.dp, horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = colors.surfaceBase,
                strokeWidth = 2.dp,
            )
        } else {
            Icon(imageVector = icon, contentDescription = null, tint = colors.surfaceBase, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = colors.surfaceBase)) { append(text) }
            },
            style = TitleMedium,
        )
    }
}

// ─── ERROR ────────────────────────────────────────────────────────────────────

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space16),
    ) {
        Text(text = "BENCHMARK ERROR", style = LabelUppercase, color = colors.danger)
        Text(
            text = message,
            style = BodyLarge,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "GL context may have been lost or the device reported a render failure.",
            style = BodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.space12))
        PrimaryCta(label = "Try again", onClick = onRetry)
    }
}
