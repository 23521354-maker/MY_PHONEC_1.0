package com.example.myphonec

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff0a0a0a))
            .statusBarsPadding()
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.05f))
                    .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.back_icon),
                    contentDescription = "Back",
                    tint = Color(0xff22d3ee),
                    modifier = Modifier.size(20.dp)
                )
            }

            Column {
                Text(
                    text = "GLOBAL RANKING",
                    color = Color(0xff22d3ee),
                    style = TextStyle(
                        fontSize = 12.sp, 
                        fontWeight = FontWeight.Black, 
                        letterSpacing = 2.sp
                    )
                )
                Text(
                    text = "Leaderboard",
                    color = Color.White,
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                uiState.isLoading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(10) { ShimmerLeaderboardRow() }
                    }
                }
                uiState.error != null -> {
                    ErrorState(message = uiState.error!!, onRetry = { viewModel.observeLeaderboard() })
                }
                uiState.items.isEmpty() -> {
                    EmptyState()
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.items) { item ->
                            LeaderboardRow(item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderboardRow(item: PhoneRankItem) {
    val isTop3 = item.rank <= 3
    val accentColor = when (item.rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color(0xff22d3ee)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = if (isTop3) {
                        listOf(accentColor.copy(alpha = 0.15f), Color(0xff1a1a1a))
                    } else {
                        listOf(Color(0xff1a1a1a), Color(0xff121212))
                    }
                )
            )
            .border(
                1.dp, 
                if (isTop3) accentColor.copy(alpha = 0.4f) else Color.White.copy(alpha = 0.05f), 
                RoundedCornerShape(20.dp)
            ),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Rank Number
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isTop3) accentColor.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.rank.toString(),
                    color = if (isTop3) accentColor else Color.Gray,
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)
                )
            }

            // Phone Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    color = Color.White,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                    maxLines = 1
                )
                Text(
                    text = "${item.chipset}${item.userName?.let { " • $it" } ?: ""}",
                    color = Color.Gray,
                    style = TextStyle(fontSize = 11.sp),
                    maxLines = 1
                )
            }

            // Score & FPS
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = item.score.toString(),
                    color = if (isTop3) accentColor else Color(0xff22d3ee),
                    style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Black)
                )
                if (item.fps > 0) {
                    Text(
                        text = "${item.fps} FPS",
                        color = Color.Gray,
                        style = TextStyle(fontSize = 10.sp, fontWeight = FontWeight.Medium)
                    )
                }
            }
        }
    }
}

@Composable
fun ShimmerLeaderboardRow() {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = alpha * 0.1f))
            .border(1.dp, Color.White.copy(alpha = 0.05f), RoundedCornerShape(20.dp))
    )
}

@Composable
fun EmptyState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = R.drawable.score_board),
            contentDescription = null,
            tint = Color.Gray.copy(alpha = 0.3f),
            modifier = Modifier.size(80.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "No benchmark results yet",
            color = Color.Gray,
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium)
        )
    }
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Connection Error",
            color = Color.White,
            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            color = Color.Gray,
            style = TextStyle(fontSize = 14.sp),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xff22d3ee)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(text = "Retry", color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}
