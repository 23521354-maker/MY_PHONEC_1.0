package com.example.myphonec.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.RadiusFull
import com.example.myphonec.ui.theme.TitleMedium

@Composable
fun <T> SegmentedControl(
    options: List<T>,
    selected: T,
    onSelect: (T) -> Unit,
    labelFor: (T) -> String,
    modifier: Modifier = Modifier,
) {
    val colors = AppTheme.colors
    val outerShape = RoundedCornerShape(RadiusFull)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(outerShape)
            .background(colors.surfaceLevel2, outerShape)
            .border(1.dp, colors.borderSubtle, outerShape)
            .padding(4.dp),
    ) {
        options.forEach { option ->
            val isSelected = option == selected
            val innerShape = RoundedCornerShape(RadiusFull)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(innerShape)
                    .background(if (isSelected) colors.cyanWash else androidx.compose.ui.graphics.Color.Transparent, innerShape)
                    .clickable { if (!isSelected) onSelect(option) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = labelFor(option),
                    style = TitleMedium,
                    color = if (isSelected) colors.cyanInk else colors.textSecondary,
                )
            }
        }
    }
}
