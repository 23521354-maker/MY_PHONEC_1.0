package com.example.myphonec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.Text
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.LabelUppercase
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.MonoLarge
import com.example.myphonec.ui.theme.RadiusFull

/**
 * Instrument-style tile for displaying a primary numeric metric.
 *
 *  ┌──────────────────────────┐
 *  │ MEMORY              [ⓘ] │
 *  │                          │
 *  │  2 GB                    │
 *  │  RAM · LPDDR5            │
 *  │                          │
 *  │  ▓▓▓▓▓▓▓▓░░░░  62%       │
 *  └──────────────────────────┘
 */
@Composable
fun MetricTile(
    label: String,
    value: String,
    unit: String? = null,
    caption: String? = null,
    progress: Float? = null,
    progressColor: androidx.compose.ui.graphics.Color = AppTheme.colors.cyanPrimary,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    SurfaceCard(
        modifier = modifier.fillMaxWidth().heightIn(min = 132.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = label.uppercase(),
                    style = LabelUppercase,
                    color = AppTheme.colors.textTertiary,
                )
                if (trailing != null) trailing()
            }

            Column {
                Text(
                    text = buildAnnotatedString {
                        withStyle(SpanStyle(color = AppTheme.colors.textPrimary)) {
                            append(value)
                        }
                        if (!unit.isNullOrBlank()) {
                            append("  ")
                            withStyle(
                                SpanStyle(
                                    color = AppTheme.colors.textSecondary,
                                    fontSize = 16.sp,
                                )
                            ) {
                                append(unit)
                            }
                        }
                    },
                    style = MonoLarge,
                )
                if (!caption.isNullOrBlank()) {
                    Text(
                        text = caption,
                        style = Mono.copy(color = AppTheme.colors.textSecondary),
                    )
                }
            }

            if (progress != null) {
                Spacer(Modifier.height(0.dp))
                ProgressBar(progress = progress.coerceIn(0f, 1f), color = progressColor)
            }
        }
    }
}

@Composable
private fun ProgressBar(
    progress: Float,
    color: androidx.compose.ui.graphics.Color,
) {
    val shape = RoundedCornerShape(RadiusFull)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(AppTheme.colors.surfaceLevel3, shape),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .height(2.dp)
                .background(color, shape),
        )
    }
}
