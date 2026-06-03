package com.example.myphonec.ui.theme

import android.provider.Settings
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

val EasingStandard: Easing = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
val EasingEmphasis: Easing = CubicBezierEasing(0.3f, 0.0f, 0.0f, 1.0f)

const val DurationFast   = 150
const val DurationMedium = 250
const val DurationSlow   = 400

/** Stagger between sequential card reveals. */
const val StaggerStep    = 60

/**
 * True when the user has asked the OS to reduce motion (animator scale 0,
 * or accessibility tools that disable animations).
 */
val LocalReduceMotion = compositionLocalOf { false }

/**
 * Reads the system animator scale to determine whether the user has disabled
 * animations. Call this inside a Composable and forward to `LocalReduceMotion`.
 */
@Composable
fun rememberReduceMotionFromSystem(): Boolean {
    val context = LocalContext.current
    return remember(context) {
        runCatching {
            Settings.Global.getFloat(
                context.contentResolver,
                Settings.Global.ANIMATOR_DURATION_SCALE,
                1f,
            ) == 0f
        }.getOrDefault(false)
    }
}
