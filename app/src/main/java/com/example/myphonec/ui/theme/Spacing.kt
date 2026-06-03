package com.example.myphonec.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Immutable
data class AppSpacing(
    val space2: Dp = 2.dp,
    val space4: Dp = 4.dp,
    val space8: Dp = 8.dp,
    val space12: Dp = 12.dp,
    val space16: Dp = 16.dp,
    val space20: Dp = 20.dp,
    val space24: Dp = 24.dp,
    val space32: Dp = 32.dp,
    val space48: Dp = 48.dp,
    val screenHorizontal: Dp = 20.dp,
    val sectionGap: Dp = 32.dp,
    val cardInner: Dp = 20.dp,
)

val Spacing = AppSpacing()
