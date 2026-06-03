package com.example.myphonec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.RadiusFull

@Composable
fun StatusPill(
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
    showDot: Boolean = true,
) {
    val shape = RoundedCornerShape(RadiusFull)
    Row(
        modifier = modifier
            .clip(shape)
            .background(color.copy(alpha = 0.12f), shape)
            .border(1.dp, color.copy(alpha = 0.24f), shape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (showDot) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(color)
            )
        }
        Text(
            text = label,
            style = Mono,
            color = color,
        )
    }
}

object StatusPillTone {
    val active   @Composable get() = AppTheme.colors.success
    val warning  @Composable get() = AppTheme.colors.warning
    val danger   @Composable get() = AppTheme.colors.danger
    val info     @Composable get() = AppTheme.colors.cyanPrimary
    val muted    @Composable get() = AppTheme.colors.textTertiary
}
