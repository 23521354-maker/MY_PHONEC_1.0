package com.example.myphonec

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphonec.ui.theme.MyPhoneCTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerformanceScreen(
    onBackClick: () -> Unit,
    viewModel: PerformanceViewModel = viewModel()
) {
    val state by viewModel.performanceState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Performance", color = Color.White, style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xff00e5ff))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xff00e5ff))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp),
                contentPadding = PaddingValues(top = 20.dp, bottom = 40.dp)
            ) {
                item {
                    PerformanceCircle(state.performanceScore, state.isOptimizing)
                }

                item {
                    AnimatedVisibility(
                        visible = state.optimizationMessage != null && !state.isOptimizing,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        OptimizationResultCard(state.ramFreed)
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "SYSTEM USAGE",
                            color = Color(0xffbac9cc).copy(alpha = 0.5f),
                            style = TextStyle(fontSize = 12.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Bold)
                        )
                        
                        UsageCard(label = "RAM Usage", value = "${(state.ramUsage * 100).toInt()}%", progress = state.ramUsage)
                        UsageCard(label = "CPU Load", value = "${(state.cpuLoad * 100).toInt()}%", progress = state.cpuLoad)
                        UsageCard(label = "Storage", value = "${(state.storageUsed * 100).toInt()}%", progress = state.storageUsed)
                    }
                }

                item {
                    Button(
                        onClick = { viewModel.optimize() },
                        enabled = !state.isOptimizing,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (state.performanceScore == "OPTIMAL") Color(0xff2ff801).copy(alpha = 0.1f) else Color(0xff00e5ff),
                            contentColor = if (state.performanceScore == "OPTIMAL") Color(0xff2ff801) else Color.Black,
                            disabledContainerColor = Color.Gray.copy(alpha = 0.3f),
                            disabledContentColor = Color.White.copy(alpha = 0.5f)
                        ),
                        border = if (state.performanceScore == "OPTIMAL") BorderStroke(1.dp, Color(0xff2ff801)) else null
                    ) {
                        if (state.isOptimizing) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("OPTIMIZING...", fontWeight = FontWeight.Bold)
                        } else {
                            Text(if (state.performanceScore == "OPTIMAL") "RE-OPTIMIZE" else "OPTIMIZE PERFORMANCE", fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (state.performanceScore == "OPTIMAL" && !state.isOptimizing) {
                    item {
                        Text(
                            text = "System is running at peak performance",
                            color = Color(0xff2ff801).copy(alpha = 0.8f),
                            style = TextStyle(fontSize = 13.sp, fontWeight = FontWeight.Medium),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OptimizationResultCard(ramFreed: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xff2ff801).copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, Color(0xff2ff801).copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xff2ff801))
            Column {
                Text("Optimization Complete", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("Freed up $ramFreed of RAM and cleared cache.", color = Color(0xffbac9cc), fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun PerformanceCircle(score: String, isOptimizing: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val color = when {
        isOptimizing -> Color(0xffFFB300)
        score == "OPTIMAL" -> Color(0xff2ff801)
        score == "POOR" -> Color(0xFFFF5252)
        else -> Color(0xff00e5ff)
    }

    Box(
        modifier = Modifier
            .size(200.dp)
            .drawBehind {
                drawCircle(
                    color = color.copy(alpha = if (isOptimizing) alpha else 0.05f),
                    radius = size.minDimension / 2,
                )
                drawArc(
                    color = color.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = if (isOptimizing) alpha * 360f else 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = score,
                color = color,
                style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
            )
            Text(
                text = if (isOptimizing) "SCANNING..." else "STATUS",
                color = Color(0xffbac9cc).copy(alpha = 0.6f),
                style = TextStyle(fontSize = 12.sp, letterSpacing = 1.sp)
            )
        }
    }
}

@Composable
fun UsageCard(label: String, value: String, progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White.copy(alpha = 0.05f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = label, color = Color(0xffbac9cc), fontSize = 14.sp)
            Text(text = value, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(CircleShape),
            color = if (progress > 0.8f) Color(0xFFFF5252) else Color(0xff00e5ff),
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}

@Preview
@Composable
fun PerformanceScreenPreview() {
    MyPhoneCTheme {
        PerformanceScreen(onBackClick = {})
    }
}
