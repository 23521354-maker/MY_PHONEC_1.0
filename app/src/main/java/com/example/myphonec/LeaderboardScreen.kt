package com.example.myphonec

import android.os.Build
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material.icons.filled.EmojiEvents
import com.example.myphonec.ui.components.EmptyState
import com.example.myphonec.ui.components.MonoNumber
import com.example.myphonec.ui.components.SectionHeader
import com.example.myphonec.ui.components.SkeletonBox
import com.example.myphonec.ui.components.SurfaceCard
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.DisplayMedium
import com.example.myphonec.ui.theme.LabelUppercase
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.TitleMedium

@Composable
fun LeaderboardScreen(
    onBackClick: () -> Unit,
    viewModel: LeaderboardViewModel = viewModel(),
) {
    val ui by viewModel.uiState.collectAsState()
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing
    val myDevice = remember { Build.MODEL }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase)
            .statusBarsPadding()
    ) {
        TopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = spacing.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(spacing.space12),
        ) {
            Text(
                text = "Global leaderboard",
                style = DisplayMedium,
                color = colors.textPrimary,
            )
            SectionHeader(
                title = "Top ${ui.items.size.coerceAtLeast(0)}",
                caption = if (ui.items.isEmpty()) "Waiting for results" else "Live · across all devices",
            )
        }

        Spacer(modifier = Modifier.height(spacing.space16))

        Box(modifier = Modifier.fillMaxSize()) {
            when {
                ui.isLoading -> LoadingList()
                ui.error != null -> ErrorState(
                    message = ui.error!!,
                    onRetry = { viewModel.observeLeaderboard() }
                )
                ui.items.isEmpty() -> EmptyBoard()
                else -> RankList(items = ui.items, myDeviceModel = myDevice)
            }
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
            text = "LEADERBOARD",
            style = LabelUppercase,
            color = colors.textTertiary,
        )
        Spacer(modifier = Modifier.width(48.dp))
    }
}

@Composable
private fun RankList(items: List<PhoneRankItem>, myDeviceModel: String) {
    val spacing = AppTheme.spacing
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = spacing.screenHorizontal,
            end = spacing.screenHorizontal,
            top = spacing.space4,
            bottom = spacing.space48,
        ),
    ) {
        item { RankHeaderRow() }
        items(items) { item ->
            RankRow(
                item = item,
                isMine = item.name.equals(myDeviceModel, ignoreCase = true),
            )
        }
    }
}

@Composable
private fun RankHeaderRow() {
    val colors = AppTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "#",
            style = LabelUppercase,
            color = colors.textTertiary,
            modifier = Modifier.width(40.dp),
        )
        Text(
            text = "DEVICE",
            style = LabelUppercase,
            color = colors.textTertiary,
            modifier = Modifier.weight(1.4f),
        )
        Text(
            text = "CHIPSET",
            style = LabelUppercase,
            color = colors.textTertiary,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = "SCORE",
            style = LabelUppercase,
            color = colors.textTertiary,
            textAlign = TextAlign.End,
            modifier = Modifier.width(80.dp),
        )
    }
    HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
}

@Composable
private fun RankRow(item: PhoneRankItem, isMine: Boolean) {
    val colors = AppTheme.colors
    val isTop3 = item.rank in 1..3

    val rowBg = if (isMine) colors.cyanWash else androidx.compose.ui.graphics.Color.Transparent
    val borderColor = when {
        isTop3 -> colors.cyanPrimary
        isMine -> colors.cyanGlow
        else -> androidx.compose.ui.graphics.Color.Transparent
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(rowBg)
                .padding(vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Left accent: 2dp colored bar for top3 / mine
            Box(
                modifier = Modifier
                    .width(2.dp)
                    .height(28.dp)
                    .background(borderColor)
            )
            Spacer(modifier = Modifier.width(8.dp))

            MonoNumber(
                text = item.rank.toString().padStart(2, '0'),
                color = if (isTop3) colors.cyanPrimary else colors.textSecondary,
                modifier = Modifier.width(30.dp),
            )

            Column(modifier = Modifier.weight(1.4f)) {
                Text(
                    text = item.name,
                    style = TitleMedium,
                    color = if (isMine) colors.cyanInk else colors.textPrimary,
                    maxLines = 1,
                )
                if (!item.userName.isNullOrBlank()) {
                    Text(
                        text = item.userName,
                        style = BodyMedium,
                        color = colors.textTertiary,
                        maxLines = 1,
                    )
                }
            }

            Text(
                text = item.chipset,
                style = Mono.copy(fontSize = 12.sp),
                color = colors.textSecondary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
            )

            Column(
                modifier = Modifier.width(80.dp),
                horizontalAlignment = Alignment.End,
            ) {
                MonoNumber(
                    text = item.score.toString(),
                    color = colors.textPrimary,
                )
                if (item.fps > 0) {
                    Text(
                        text = "${item.fps} FPS",
                        style = Mono.copy(fontSize = 10.sp),
                        color = colors.textTertiary,
                    )
                }
            }
        }
        HorizontalDivider(thickness = 1.dp, color = colors.borderSubtle)
    }
}

@Composable
private fun LoadingList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = AppTheme.spacing.screenHorizontal),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(10) { ShimmerRow() }
    }
}

@Composable
private fun ShimmerRow() {
    SkeletonBox(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    )
}

@Composable
private fun EmptyBoard() {
    Box(
        modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.space24),
        contentAlignment = Alignment.Center,
    ) {
        EmptyState(
            icon = Icons.Default.EmojiEvents,
            title = "Nothing benchmarked yet",
            description = "Be the first to publish a result.\nRun a benchmark to appear on the board.",
        )
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(AppTheme.spacing.space12)
    Column(
        modifier = Modifier.fillMaxSize().padding(AppTheme.spacing.space24),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Connection error",
            style = TitleMedium,
            color = colors.textPrimary,
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.space8))
        Text(
            text = message,
            style = BodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(AppTheme.spacing.space24))
        Box(
            modifier = Modifier
                .clip(shape)
                .background(colors.cyanPrimary, shape)
                .clickable(onClick = onRetry)
                .padding(horizontal = 24.dp, vertical = 12.dp),
        ) {
            Text(
                text = "Retry",
                style = TitleMedium,
                color = colors.surfaceBase,
            )
        }
    }
}
