package com.example.myphonec.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.LocalReduceMotion
import com.example.myphonec.ui.theme.RadiusLg

/**
 * Base surface card. Replaces Material `Card` defaults.
 *
 * Depth via layered surface + 1dp border, NOT shadow.
 * Press feedback: subtle scale 0.98 spring (no ripple).
 */
@Composable
fun SurfaceCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    background: Color = AppTheme.colors.surfaceLevel1,
    borderColor: Color = AppTheme.colors.borderDefault,
    borderWidth: Dp = 1.dp,
    cornerRadius: Dp = RadiusLg,
    contentPadding: PaddingValues = PaddingValues(AppTheme.spacing.cardInner),
    content: @Composable () -> Unit,
) {
    val shape = RoundedCornerShape(cornerRadius)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val interactive = onClick != null
    val reduceMotion = LocalReduceMotion.current

    val scale by animateFloatAsState(
        targetValue = if (interactive && pressed && !reduceMotion) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "surfaceCardScale"
    )

    val baseModifier = modifier
        .scale(scale)
        .clip(shape)
        .background(background, shape)
        .border(borderWidth, borderColor, shape)

    val clickableModifier = if (onClick != null) {
        baseModifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    } else baseModifier

    Box(modifier = clickableModifier.padding(contentPadding)) {
        content()
    }
}
