package com.example.myphonec.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lightweight sparkline. Renders a smoothed line with an optional gradient fill below.
 *
 *  - `values` should be the most recent N samples (left → right ordering).
 *  - When fewer than 2 samples are provided nothing is drawn.
 */
@Composable
fun Sparkline(
    values: List<Float>,
    strokeColor: Color,
    modifier: Modifier = Modifier,
    fillColor: Color = strokeColor.copy(alpha = 0.16f),
    strokeWidth: Dp = 1.5.dp,
    minRange: Float = 1f,
) {
    Canvas(modifier = modifier) {
        if (values.size < 2) return@Canvas
        val w = size.width
        val h = size.height
        val minV = values.min()
        val maxV = values.max()
        val range = (maxV - minV).takeIf { it > 0f } ?: minRange

        val points = values.mapIndexed { i, v ->
            val x = i.toFloat() / (values.size - 1) * w
            val y = h - ((v - minV) / range) * h * 0.9f - h * 0.05f
            Offset(x, y)
        }

        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                val prev = points[i - 1]
                val cur = points[i]
                val midX = (prev.x + cur.x) / 2f
                cubicTo(midX, prev.y, midX, cur.y, cur.x, cur.y)
            }
        }

        val fill = Path().apply {
            addPath(line)
            lineTo(points.last().x, h)
            lineTo(points.first().x, h)
            close()
        }

        drawPath(fill, color = fillColor)
        drawPath(
            line,
            color = strokeColor,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round),
        )
    }
}
