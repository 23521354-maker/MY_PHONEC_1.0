package com.example.myphonec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.DisplayFontFamily

@Composable
fun Avatar(
    photoUrl: String?,
    displayName: String?,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
    onClick: (() -> Unit)? = null,
) {
    val initial = (displayName?.trim()?.firstOrNull() ?: '?').uppercaseChar().toString()
    val borderColor = AppTheme.colors.borderDefault
    val fallbackBg = AppTheme.colors.amberWash
    val fallbackInk = AppTheme.colors.amber

    val base = modifier
        .size(size)
        .clip(CircleShape)
        .background(if (photoUrl == null) fallbackBg else AppTheme.colors.surfaceLevel2)
        .border(1.dp, borderColor, CircleShape)

    val full = if (onClick != null) base.clickable(onClick = onClick) else base

    Box(modifier = full, contentAlignment = Alignment.Center) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = displayName?.let { "Profile of $it" } ?: "Profile",
                modifier = Modifier.size(size).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = initial,
                color = fallbackInk,
                style = androidx.compose.ui.text.TextStyle(
                    fontFamily = DisplayFontFamily,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    fontSize = (size.value * 0.42f).sp,
                ),
            )
        }
    }
}
