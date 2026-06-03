package com.example.myphonec.ui.theme

import androidx.compose.ui.graphics.Color

// --- Dark token palette (immutable source) ---
val DarkSurfaceBase     = Color(0xFF07090D)
val DarkSurfaceLevel1   = Color(0xFF0E1218)
val DarkSurfaceLevel2   = Color(0xFF161B23)
val DarkSurfaceLevel3   = Color(0xFF1F2630)

val DarkBorderSubtle    = Color(0xFF1A2029)
val DarkBorderDefault   = Color(0xFF252D38)
val DarkBorderEmphasis  = Color(0xFF3A4452)

val DarkTextPrimary     = Color(0xFFE6EAF0)
val DarkTextSecondary   = Color(0xFF9BA4B0)
val DarkTextTertiary    = Color(0xFF5C6470)
val DarkTextDisabled    = Color(0xFF3A4150)

val DarkCyanPrimary     = Color(0xFF22D3EE)
val DarkCyanGlow        = Color(0xFF06B6D4)
val DarkCyanInk         = Color(0xFF67E8F9)
val DarkCyanWash        = Color(0x1422D3EE)

val DarkAmber           = Color(0xFFFB923C)
val DarkAmberWash       = Color(0x29FB923C)

val DarkSuccess         = Color(0xFF34D399)
val DarkWarning         = Color(0xFFFBBF24)
val DarkDanger          = Color(0xFFF87171)

// --- Light token palette (immutable source) ---
val LightSurfaceBase     = Color(0xFFF7F8FA)
val LightSurfaceLevel1   = Color(0xFFFFFFFF)
val LightSurfaceLevel2   = Color(0xFFF1F3F6)
val LightSurfaceLevel3   = Color(0xFFE6E9EE)

val LightBorderSubtle    = Color(0xFFE2E5EA)
val LightBorderDefault   = Color(0xFFCDD2D9)
val LightBorderEmphasis  = Color(0xFFA3ACB8)

val LightTextPrimary     = Color(0xFF0E1218)
val LightTextSecondary   = Color(0xFF4B5563)
val LightTextTertiary    = Color(0xFF6B7280)
val LightTextDisabled    = Color(0xFFB7BDC7)

val LightCyanPrimary     = Color(0xFF0891B2)
val LightCyanGlow        = Color(0xFF0E7490)
val LightCyanInk         = Color(0xFF155E75)
val LightCyanWash        = Color(0x140891B2)

val LightAmber           = Color(0xFFD97706)
val LightAmberWash       = Color(0x29D97706)

val LightSuccess         = Color(0xFF059669)
val LightWarning         = Color(0xFFD97706)
val LightDanger          = Color(0xFFDC2626)

// --- Live theme tokens (mutable; swapped by ThemePreferences) ---
// Direct screen references read these. When the theme changes, MyPhoneCTheme
// recomposes the entire subtree so screens re-read the new values.
var SurfaceBase     = DarkSurfaceBase
var SurfaceLevel1   = DarkSurfaceLevel1
var SurfaceLevel2   = DarkSurfaceLevel2
var SurfaceLevel3   = DarkSurfaceLevel3

var BorderSubtle    = DarkBorderSubtle
var BorderDefault   = DarkBorderDefault
var BorderEmphasis  = DarkBorderEmphasis

var TextPrimary     = DarkTextPrimary
var TextSecondary   = DarkTextSecondary
var TextTertiary    = DarkTextTertiary
var TextDisabled    = DarkTextDisabled

var CyanPrimary     = DarkCyanPrimary
var CyanGlow        = DarkCyanGlow
var CyanInk         = DarkCyanInk
var CyanWash        = DarkCyanWash

var Amber           = DarkAmber
var AmberWash       = DarkAmberWash

var Success         = DarkSuccess
var Warning         = DarkWarning
var Danger          = DarkDanger
var Info            = DarkCyanPrimary

// Backward-compat aliases (kept for unmigrated screens)
var DarkBackground           = DarkSurfaceBase
var CardBackground           = DarkSurfaceLevel1
var DiagnosticCardBackground = DarkSurfaceLevel2
var NeonCyan                 = DarkCyanPrimary
var NeonCyanSecondary        = DarkCyanGlow
var TextMuted                = DarkTextTertiary
var BorderCyan               = DarkCyanWash

internal fun applyDarkPalette() {
    SurfaceBase     = DarkSurfaceBase
    SurfaceLevel1   = DarkSurfaceLevel1
    SurfaceLevel2   = DarkSurfaceLevel2
    SurfaceLevel3   = DarkSurfaceLevel3
    BorderSubtle    = DarkBorderSubtle
    BorderDefault   = DarkBorderDefault
    BorderEmphasis  = DarkBorderEmphasis
    TextPrimary     = DarkTextPrimary
    TextSecondary   = DarkTextSecondary
    TextTertiary    = DarkTextTertiary
    TextDisabled    = DarkTextDisabled
    CyanPrimary     = DarkCyanPrimary
    CyanGlow        = DarkCyanGlow
    CyanInk         = DarkCyanInk
    CyanWash        = DarkCyanWash
    Amber           = DarkAmber
    AmberWash       = DarkAmberWash
    Success         = DarkSuccess
    Warning         = DarkWarning
    Danger          = DarkDanger
    Info            = DarkCyanPrimary
    DarkBackground           = DarkSurfaceBase
    CardBackground           = DarkSurfaceLevel1
    DiagnosticCardBackground = DarkSurfaceLevel2
    NeonCyan                 = DarkCyanPrimary
    NeonCyanSecondary        = DarkCyanGlow
    TextMuted                = DarkTextTertiary
    BorderCyan               = DarkCyanWash
}

internal fun applyLightPalette() {
    SurfaceBase     = LightSurfaceBase
    SurfaceLevel1   = LightSurfaceLevel1
    SurfaceLevel2   = LightSurfaceLevel2
    SurfaceLevel3   = LightSurfaceLevel3
    BorderSubtle    = LightBorderSubtle
    BorderDefault   = LightBorderDefault
    BorderEmphasis  = LightBorderEmphasis
    TextPrimary     = LightTextPrimary
    TextSecondary   = LightTextSecondary
    TextTertiary    = LightTextTertiary
    TextDisabled    = LightTextDisabled
    CyanPrimary     = LightCyanPrimary
    CyanGlow        = LightCyanGlow
    CyanInk         = LightCyanInk
    CyanWash        = LightCyanWash
    Amber           = LightAmber
    AmberWash       = LightAmberWash
    Success         = LightSuccess
    Warning         = LightWarning
    Danger          = LightDanger
    Info            = LightCyanPrimary
    DarkBackground           = LightSurfaceBase
    CardBackground           = LightSurfaceLevel1
    DiagnosticCardBackground = LightSurfaceLevel2
    NeonCyan                 = LightCyanPrimary
    NeonCyanSecondary        = LightCyanGlow
    TextMuted                = LightTextTertiary
    BorderCyan               = LightCyanWash
}
