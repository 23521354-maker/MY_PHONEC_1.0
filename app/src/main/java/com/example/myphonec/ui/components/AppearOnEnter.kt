package com.example.myphonec.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.graphicsLayer
import com.example.myphonec.ui.theme.DurationSlow
import com.example.myphonec.ui.theme.EasingStandard
import com.example.myphonec.ui.theme.LocalReduceMotion

/**
 * Fades in + translates Y when first composed.
 *
 *  - `delayMillis` lets a list of cards stagger their reveal.
 *  - `from` controls the starting Y offset.
 */
@Composable
fun Modifier.appearOnEnter(
    delayMillis: Int = 0,
    from: Dp = 12.dp,
    durationMillis: Int = DurationSlow,
): Modifier = composed {
    val reduceMotion = LocalReduceMotion.current
    if (reduceMotion) return@composed this

    val density = LocalDensity.current
    val fromPx = with(density) { from.toPx() }
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }

    val progress by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(
            durationMillis = durationMillis,
            delayMillis = delayMillis,
            easing = EasingStandard,
        ),
        label = "appearOnEnter",
    )

    this
        .alpha(progress)
        .graphicsLayer { translationY = (1f - progress) * fromPx }
}
