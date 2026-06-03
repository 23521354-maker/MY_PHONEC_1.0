package com.example.myphonec.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Until Inter / Space Grotesk / JetBrains Mono ttf files are added under res/font/,
// fall back to system defaults. Swap these aliases when assets are available.
val DisplayFontFamily: FontFamily = FontFamily.SansSerif
val TextFontFamily: FontFamily    = FontFamily.SansSerif
val MonoFontFamily: FontFamily    = FontFamily.Monospace

val DisplayLarge = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 36.sp,
    lineHeight = 40.sp,
    letterSpacing = (-0.5).sp,
)

val DisplayMedium = TextStyle(
    fontFamily = DisplayFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 28.sp,
    lineHeight = 32.sp,
    letterSpacing = (-0.4).sp,
)

val TitleLarge = TextStyle(
    fontFamily = TextFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 18.sp,
    lineHeight = 24.sp,
    letterSpacing = (-0.1).sp,
)

val TitleMedium = TextStyle(
    fontFamily = TextFontFamily,
    fontWeight = FontWeight.SemiBold,
    fontSize = 15.sp,
    lineHeight = 20.sp,
)

val BodyLarge = TextStyle(
    fontFamily = TextFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 15.sp,
    lineHeight = 22.sp,
)

val BodyMedium = TextStyle(
    fontFamily = TextFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 13.sp,
    lineHeight = 18.sp,
)

val LabelUppercase = TextStyle(
    fontFamily = TextFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp,
    lineHeight = 14.sp,
    letterSpacing = 1.2.sp,
)

val Mono = TextStyle(
    fontFamily = MonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 13.sp,
    lineHeight = 18.sp,
)

val MonoLarge = TextStyle(
    fontFamily = MonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 32.sp,
    lineHeight = 36.sp,
    letterSpacing = (-0.5).sp,
)

val MonoHero = TextStyle(
    fontFamily = MonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 56.sp,
    lineHeight = 60.sp,
    letterSpacing = (-1).sp,
)

val Typography = Typography(
    displayLarge   = DisplayLarge,
    displayMedium  = DisplayMedium,
    titleLarge     = TitleLarge,
    titleMedium    = TitleMedium,
    bodyLarge      = BodyLarge,
    bodyMedium     = BodyMedium,
    labelSmall     = LabelUppercase,
    headlineLarge  = DisplayLarge,
)
