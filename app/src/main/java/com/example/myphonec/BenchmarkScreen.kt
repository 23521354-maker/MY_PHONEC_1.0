package com.example.myphonec

import android.view.ViewGroup
import android.opengl.GLSurfaceView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

@Composable
fun BenchmarkScreen(
    onBackClick: () -> Unit,
    viewModel: BenchmarkViewModel
) {
    val state by viewModel.benchmarkState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff0a0a0a))
    ) {
        // GPU Content Layer (The actual OpenGL Rendering)
        if (state.isRunning) {
            BenchmarkView(onFpsUpdate = { viewModel.updateFps(it) })
        }

        // UI Layer
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            BenchmarkTopBar(onBackClick = onBackClick)

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                when {
                    state.result != null -> {
                        ResultView(
                            result = state.result!!, 
                            onRunAgain = { viewModel.reset() },
                            onSaveResult = { viewModel.uploadResult(state.result!!) },
                            isUploading = state.isUploading,
                            uploadSuccess = state.uploadSuccess,
                            error = state.error
                        )
                    }
                    state.isCountingDown -> {
                        CountdownView(value = state.countdownValue)
                    }
                    state.isRunning -> {
                        InBenchmarkUI(state = state)
                    }
                    else -> {
                        StartBenchmarkUI(onStart = { viewModel.startBenchmark() })
                    }
                }
            }
        }
    }
}

@Composable
fun BenchmarkView(onFpsUpdate: (Int) -> Unit) {
    val context = LocalContext.current
    AndroidView(
        factory = { ctx ->
            GLSurfaceView(ctx).apply {
                setEGLContextClientVersion(3)
                val renderer = BenchmarkRenderer(ctx)
                renderer.onFpsUpdate = onFpsUpdate
                setRenderer(renderer)
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun BenchmarkTopBar(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.05f))
        ) {
            Icon(
                painter = painterResource(id = R.drawable.back_icon),
                contentDescription = "Back",
                tint = Color(0xff22d3ee),
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(
                text = "GPU BENCHMARK",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
            Text(
                text = "STRESS TEST V1.0",
                color = Color(0xff22d3ee),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun StartBenchmarkUI(onStart: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 40.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xff22d3ee).copy(alpha = 0.1f))
                .border(2.dp, Color(0xff22d3ee).copy(alpha = 0.3f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.score_board),
                contentDescription = null,
                tint = Color(0xff22d3ee),
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "READY FOR STRESS TEST?",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = "This will push your GPU to the limit for 30 seconds. Close background apps for best results.",
            color = Color(0xffa1a1aa),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d3ee)),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = "START BENCHMARK",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun CountdownView(value: Int) {
    Box(contentAlignment = Alignment.Center) {
        val scale by animateFloatAsState(
            targetValue = if (value > 0) 1.5f else 1f,
            animationSpec = tween(500),
            label = "countdown_scale"
        )
        Text(
            text = if (value > 0) value.toString() else "START",
            color = Color(0xff22d3ee),
            fontSize = 120.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier.animateContentSize()
        )
    }
}

@Composable
fun InBenchmarkUI(state: BenchmarkState) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Bottom
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text("CURRENT FPS", color = Color(0xff22d3ee), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = state.currentFps.toString(),
                    color = Color.White,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Black
                )
            }
            
            Text(
                text = "${(state.progress * 100).toInt()}%",
                color = Color(0xff22d3ee),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LinearProgressIndicator(
            progress = state.progress,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Color(0xff22d3ee),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Composable
fun ResultView(
    result: BenchmarkResult, 
    onRunAgain: () -> Unit,
    onSaveResult: () -> Unit,
    isUploading: Boolean,
    uploadSuccess: Boolean,
    error: String?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        // Score Circle
        Box(
            modifier = Modifier
                .size(200.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        listOf(Color(0xff22d3ee).copy(alpha = 0.15f), Color.Transparent)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("TOTAL SCORE", color = Color(0xff22d3ee), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = result.score.toString(),
                    color = Color.White,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = result.tier.uppercase(),
                    color = Color(0xff22d3ee),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        if (error != null) {
            Text(text = error, color = Color.Red, fontSize = 12.sp, modifier = Modifier.padding(bottom = 8.dp))
        }

        // Device Info Card
        ResultCard(title = "DEVICE INFORMATION") {
            ResultRow("Manufacturer", result.deviceName.split(" ").firstOrNull() ?: "-")
            ResultRow("Model", result.deviceName.split(" ").getOrNull(1) ?: "-")
            ResultRow("GPU Engine", result.gpuName)
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Stats Card
        ResultCard(title = "PERFORMANCE STATS") {
            ResultRow("Average FPS", "${result.averageFps} FPS")
            ResultRow("Stability Score", "${result.stability}%")
            ResultRow("Min / Max FPS", "${result.minFps} / ${result.maxFps}")
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                text = "Run Again",
                icon = Icons.Default.Refresh,
                containerColor = Color.White.copy(alpha = 0.05f),
                contentColor = Color.White,
                onClick = onRunAgain,
                modifier = Modifier.weight(1f)
            )
            
            val buttonColor = if (uploadSuccess) Color(0xff10b981) else Color(0xff22d3ee)
            val buttonText = if (isUploading) "Saving..." else if (uploadSuccess) "Saved" else "Save Result"
            val buttonIcon = if (uploadSuccess) Icons.Default.Check else Icons.Default.Save

            ActionButton(
                text = buttonText,
                icon = buttonIcon,
                containerColor = buttonColor,
                contentColor = Color.Black,
                onClick = onSaveResult,
                enabled = !isUploading && !uploadSuccess,
                modifier = Modifier.weight(1f)
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun ResultCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF111111))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
            .padding(20.dp)
    ) {
        Text(
            text = title,
            color = Color(0xff71717a),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(16.dp))
        content()
    }
}

@Composable
fun ResultRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xffa1a1aa), fontSize = 14.sp)
        Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActionButton(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.height(56.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.5f),
            disabledContentColor = contentColor.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        if (text == "Saving...") {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                color = contentColor,
                strokeWidth = 2.dp
            )
        } else {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = text, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
