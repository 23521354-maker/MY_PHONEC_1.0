package com.example.myphonec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SectionWrapper(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = title,
            color = Color(0xffbac9cc),
            style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(start = 4.dp)
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xff1f1f1f).copy(alpha = 0.2f))
                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DetailItem(label: String, value: String, isHighlighted: Boolean = false, isCaps: Boolean = false) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.1f)), RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically) {
        Text(text = label, color = Color(0xffbac9cc), fontSize = 12.sp)
        Text(
            text = value,
            color = if (isHighlighted) Color(0xff00e5ff) else Color(0xffe2e2e2),
            fontSize = if (isCaps) 10.sp else 16.sp,
            letterSpacing = if (isCaps) 1.sp else 0.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoRow(label: String, value: String, valueColor: Color = Color(0xffe2e2e2)) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = Color(0xffbac9cc), fontSize = 14.sp)
        Text(
            text = value,
            color = valueColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f).padding(start = 16.dp)
        )
    }
}

@Composable
fun InfoCardSmall(title: String, value: String, modifier: Modifier = Modifier, hasBorder: Boolean = false, valueColor: Color = Color(0xffe2e2e2)) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .then(if (hasBorder) Modifier.border(BorderStroke(1.dp, Color(0xff00daf3).copy(alpha = 0.1f)), RoundedCornerShape(10.dp)) else Modifier)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = title, color = Color(0xffbac9cc), fontSize = 9.sp, letterSpacing = 0.9.sp)
        Text(text = value, color = valueColor, fontSize = 20.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun StorageProgressCard(title: String, subtitle: String, used: String, total: String, percentage: Float, usedLabel: String, freeLabel: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            Column {
                Text(text = title, color = Color(0xffe2e2e2), fontSize = 12.sp)
                Text(text = subtitle, color = Color(0xffbac9cc), fontSize = 9.sp, letterSpacing = 0.9.sp)
            }
            Row(verticalAlignment = Alignment.Bottom) {
                Text(text = used, color = Color(0xffe2e2e2), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = " / $total GB", color = Color(0xffbac9cc), fontSize = 10.sp, modifier = Modifier.padding(bottom = 2.dp))
            }
        }
        
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xff353535))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(percentage.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(2.dp))
                        .background(
                            androidx.compose.ui.graphics.Brush.linearGradient(
                                colors = listOf(Color(0xff00e5ff), Color(0xff9cf0ff))
                            )
                        )
                )
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = usedLabel, color = Color(0xffbac9cc), fontSize = 9.sp, letterSpacing = 0.9.sp)
                Text(text = freeLabel, color = Color(0xffbac9cc), fontSize = 9.sp, letterSpacing = 0.9.sp)
            }
        }
    }
}
