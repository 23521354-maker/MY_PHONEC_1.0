package com.example.myphonec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myphonec.ui.theme.MyPhoneCTheme

@Composable
fun PCToolsScreen(
    modifier: Modifier = Modifier,
    onNavigateToCompare: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xff0a0a0a))
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = "PC Tools",
                color = Color(0xffe2e2e2),
                style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Build, compare and analyze PC performance",
                color = Color(0xffbac9cc),
                style = TextStyle(fontSize = 18.sp)
            )
        }

        // Cards Section
        PCToolsCard(
            title = "Compare Components",
            subtitle = "CPU vs CPU, GPU vs GPU",
            iconResId = R.drawable.container,
            onClick = onNavigateToCompare
        )
        
        PCToolsCard(
            title = "Build PC",
            subtitle = "Custom configuration",
            iconResId = R.drawable.container,
            onClick = { /* Future */ }
        )
        
        PCToolsCard(
            title = "Bottleneck",
            subtitle = "Analyze CPU & GPU balance",
            iconResId = R.drawable.container,
            onClick = { /* Future */ }
        )

        // PC Image Banner
        Image(
            painter = painterResource(id = R.drawable.internalhighendpchardwarewithliquidcoolingandneonlights),
            contentDescription = "PC Hardware",
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(24.dp)),
            contentScale = ContentScale.Crop
        )
        
        Spacer(modifier = Modifier.height(80.dp))
    }
}

@Composable
fun PCToolsCard(title: String, subtitle: String, iconResId: Int, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xff1f1f1f).copy(alpha = 0.3f))
            .border(BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.2f)), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xff22d3ee).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconResId),
                        contentDescription = null,
                        tint = Color(0xff22d3ee),
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = title,
                    color = Color(0xffe2e2e2),
                    style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )
                Text(
                    text = subtitle,
                    color = Color(0xffbac9cc),
                    style = TextStyle(fontSize = 14.sp)
                )
            }
            
            // Arrow Icon
            Icon(
                painter = painterResource(id = R.drawable.icon),
                contentDescription = null,
                tint = Color(0xff52525b),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF0A0A0A)
@Composable
fun PCToolsScreenPreview() {
    MyPhoneCTheme {
        PCToolsScreen(onNavigateToCompare = {})
    }
}
