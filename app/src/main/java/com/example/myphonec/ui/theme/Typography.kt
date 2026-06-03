package com.example.myphonec.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Dùng system fonts — không cần download, không cần cert
val IbmPlexMonoFontFamily = FontFamily.Monospace

val MonoMedium = TextStyle(
    fontFamily = IbmPlexMonoFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize   = 13.sp,
    lineHeight = 18.sp,
)
val MonoSmall = TextStyle(
    fontFamily = IbmPlexMonoFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize   = 11.sp,
    lineHeight = 15.sp,
)
