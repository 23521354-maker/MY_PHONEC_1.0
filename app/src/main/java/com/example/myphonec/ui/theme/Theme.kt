package com.example.myphonec.ui.theme

import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf

@Immutable
data class AppColors(
    val surfaceBase: androidx.compose.ui.graphics.Color,
    val surfaceLevel1: androidx.compose.ui.graphics.Color,
    val surfaceLevel2: androidx.compose.ui.graphics.Color,
    val surfaceLevel3: androidx.compose.ui.graphics.Color,
    val borderSubtle: androidx.compose.ui.graphics.Color,
    val borderDefault: androidx.compose.ui.graphics.Color,
    val borderEmphasis: androidx.compose.ui.graphics.Color,
    val textPrimary: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val textTertiary: androidx.compose.ui.graphics.Color,
    val textDisabled: androidx.compose.ui.graphics.Color,
    val cyanPrimary: androidx.compose.ui.graphics.Color,
    val cyanGlow: androidx.compose.ui.graphics.Color,
    val cyanInk: androidx.compose.ui.graphics.Color,
    val cyanWash: androidx.compose.ui.graphics.Color,
    val amber: androidx.compose.ui.graphics.Color,
    val amberWash: androidx.compose.ui.graphics.Color,
    val success: androidx.compose.ui.graphics.Color,
    val warning: androidx.compose.ui.graphics.Color,
    val danger: androidx.compose.ui.graphics.Color,
)

private fun darkAppColors() = AppColors(
    surfaceBase = DarkSurfaceBase,
    surfaceLevel1 = DarkSurfaceLevel1,
    surfaceLevel2 = DarkSurfaceLevel2,
    surfaceLevel3 = DarkSurfaceLevel3,
    borderSubtle = DarkBorderSubtle,
    borderDefault = DarkBorderDefault,
    borderEmphasis = DarkBorderEmphasis,
    textPrimary = DarkTextPrimary,
    textSecondary = DarkTextSecondary,
    textTertiary = DarkTextTertiary,
    textDisabled = DarkTextDisabled,
    cyanPrimary = DarkCyanPrimary,
    cyanGlow = DarkCyanGlow,
    cyanInk = DarkCyanInk,
    cyanWash = DarkCyanWash,
    amber = DarkAmber,
    amberWash = DarkAmberWash,
    success = DarkSuccess,
    warning = DarkWarning,
    danger = DarkDanger,
)

private fun lightAppColors() = AppColors(
    surfaceBase = LightSurfaceBase,
    surfaceLevel1 = LightSurfaceLevel1,
    surfaceLevel2 = LightSurfaceLevel2,
    surfaceLevel3 = LightSurfaceLevel3,
    borderSubtle = LightBorderSubtle,
    borderDefault = LightBorderDefault,
    borderEmphasis = LightBorderEmphasis,
    textPrimary = LightTextPrimary,
    textSecondary = LightTextSecondary,
    textTertiary = LightTextTertiary,
    textDisabled = LightTextDisabled,
    cyanPrimary = LightCyanPrimary,
    cyanGlow = LightCyanGlow,
    cyanInk = LightCyanInk,
    cyanWash = LightCyanWash,
    amber = LightAmber,
    amberWash = LightAmberWash,
    success = LightSuccess,
    warning = LightWarning,
    danger = LightDanger,
)

val LocalAppColors = staticCompositionLocalOf { darkAppColors() }
val LocalAppSpacing = staticCompositionLocalOf { Spacing }

object AppTheme {
    val colors: AppColors
        @Composable get() = LocalAppColors.current
    val spacing: AppSpacing
        @Composable get() = LocalAppSpacing.current
}

private val DarkColorScheme = darkColorScheme(
    primary       = DarkCyanPrimary,
    onPrimary     = DarkSurfaceBase,
    secondary     = DarkCyanGlow,
    onSecondary   = DarkSurfaceBase,
    background    = DarkSurfaceBase,
    onBackground  = DarkTextPrimary,
    surface       = DarkSurfaceLevel1,
    onSurface     = DarkTextPrimary,
    surfaceVariant = DarkSurfaceLevel2,
    onSurfaceVariant = DarkTextSecondary,
    outline       = DarkBorderDefault,
    outlineVariant = DarkBorderSubtle,
    error         = DarkDanger,
    onError       = DarkSurfaceBase,
)

private val LightColorScheme = lightColorScheme(
    primary       = LightCyanPrimary,
    onPrimary     = LightSurfaceLevel1,
    secondary     = LightCyanGlow,
    onSecondary   = LightSurfaceLevel1,
    background    = LightSurfaceBase,
    onBackground  = LightTextPrimary,
    surface       = LightSurfaceLevel1,
    onSurface     = LightTextPrimary,
    surfaceVariant = LightSurfaceLevel2,
    onSurfaceVariant = LightTextSecondary,
    outline       = LightBorderDefault,
    outlineVariant = LightBorderSubtle,
    error         = LightDanger,
    onError       = LightSurfaceLevel1,
)

object ThemePreferences {
    private const val PREFS = "myphonec_theme"
    private const val KEY_DARK = "is_dark"

    private val _isDark = mutableStateOf(true)
    val isDark: State<Boolean> = _isDark

    fun init(context: Context) {
        val prefs = context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val dark = prefs.getBoolean(KEY_DARK, true)
        _isDark.value = dark
        if (dark) applyDarkPalette() else applyLightPalette()
    }

    fun setDark(context: Context, dark: Boolean) {
        if (dark) applyDarkPalette() else applyLightPalette()
        _isDark.value = dark
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DARK, dark)
            .apply()
    }

    fun toggle(context: Context) {
        setDark(context, !_isDark.value)
    }
}

@Composable
fun MyPhoneCTheme(
    content: @Composable () -> Unit
) {
    val reduceMotion = rememberReduceMotionFromSystem()
    val isDark = ThemePreferences.isDark.value
    val appColors = if (isDark) darkAppColors() else lightAppColors()
    val materialScheme = if (isDark) DarkColorScheme else LightColorScheme
    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppColors provides appColors,
        LocalAppSpacing provides Spacing,
        LocalReduceMotion provides reduceMotion,
    ) {
        MaterialTheme(
            colorScheme = materialScheme,
            typography = Typography,
            content = content
        )
    }
}
