package com.example.myphonec

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ScreenTestScreen(onBackClick: () -> Unit) {
    val colors = listOf(
        Color.Black,
        Color.White,
        Color.Red,
        Color.Green,
        Color.Blue,
        Color.Yellow,
        Color.Cyan
    )
    
    var colorIndex by remember { mutableIntStateOf(0) }
    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors[colorIndex])
            .clickable(
                interactionSource = interactionSource,
                indication = null // No ripple effect for color test
            ) {
                if (colorIndex < colors.size - 1) {
                    colorIndex++
                } else {
                    onBackClick()
                }
            }
    )
}

@Preview
@Composable
fun ScreenTestScreenPreview() {
    ScreenTestScreen(onBackClick = {})
}
