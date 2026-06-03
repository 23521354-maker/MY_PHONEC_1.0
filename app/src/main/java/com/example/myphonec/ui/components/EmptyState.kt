package com.example.myphonec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.RadiusLg
import com.example.myphonec.ui.theme.TitleLarge
import com.example.myphonec.ui.theme.TitleMedium

@Composable
fun EmptyState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    val colors = AppTheme.colors
    SurfaceCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space12),
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(colors.surfaceLevel2),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.textTertiary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }
            Text(text = title, style = TitleLarge, color = colors.textPrimary)
            Text(
                text = description,
                style = BodyMedium,
                color = colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            if (!actionLabel.isNullOrBlank() && onActionClick != null) {
                val shape = RoundedCornerShape(RadiusLg)
                Box(
                    modifier = Modifier
                        .clip(shape)
                        .background(colors.cyanPrimary, shape)
                        .clickable(onClick = onActionClick)
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                ) {
                    Text(text = actionLabel, style = TitleMedium, color = colors.surfaceBase)
                }
            }
        }
    }
}
