package com.example.myphonec.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.Mono

/**
 * Single label/value row. Use inside `DataRowGroup` to get hairline dividers between rows.
 *
 *   Model           sdk_gphone64_x86_64
 *   Manufacturer    Google
 */
@Composable
fun DataRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = BodyMedium,
            color = AppTheme.colors.textSecondary,
        )
        MonoNumber(
            text = value,
            color = AppTheme.colors.textPrimary,
            textAlign = TextAlign.End,
        )
    }
}

/**
 * Wraps a list of `DataRow` and inserts 1px subtle dividers between them.
 */
@Composable
fun DataRowGroup(
    modifier: Modifier = Modifier,
    rows: List<Pair<String, String>>,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, (label, value) ->
            DataRow(label = label, value = value)
            if (index < rows.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = AppTheme.colors.borderSubtle,
                )
            }
        }
    }
}

/**
 * Mono text wrapper. Always applies the mono font and enables tabular numerals
 * so digits align in columns (instrument feel).
 */
@Composable
fun MonoNumber(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = Mono,
    color: androidx.compose.ui.graphics.Color = AppTheme.colors.textPrimary,
    textAlign: TextAlign? = null,
) {
    Text(
        text = text,
        modifier = modifier,
        style = style.copy(
            color = color,
            fontFeatureSettings = "tnum",
        ),
        textAlign = textAlign,
    )
}
