package com.example.myphonec.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.LabelUppercase

/**
 *   DIAGNOSTIC CLUSTER                        See all →
 *   8 tests available · 3 recommended
 */
@Composable
fun SectionHeader(
    title: String,
    caption: String? = null,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.space4)) {
            Text(
                text = title.uppercase(),
                style = LabelUppercase,
                color = AppTheme.colors.textTertiary,
            )
            if (!caption.isNullOrBlank()) {
                Text(
                    text = caption,
                    style = BodyMedium,
                    color = AppTheme.colors.textSecondary,
                )
            }
        }
        if (!actionLabel.isNullOrBlank() && onActionClick != null) {
            Text(
                text = actionLabel,
                style = BodyMedium,
                color = AppTheme.colors.cyanPrimary,
                modifier = Modifier.clickable(onClick = onActionClick),
            )
        }
    }
}
