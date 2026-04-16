package com.example.myphonec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun BottleneckCalculatorScreen(
    onBackClick: () -> Unit,
    viewModel: BottleneckViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color(0xff131313))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            // Header with Back Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 24.dp, top = 48.dp, bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xff1f1f1f))
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.back_icon),
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BOTTLENECK",
                        color = Color(0xffe2e2e2),
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                    )
                    Text(
                        text = "CALCULATOR",
                        color = Color(0xff00e5ff),
                        style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Medium, letterSpacing = 4.sp)
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Selector: CPU
                SelectorItem(
                    label = "CPU SELECTOR",
                    value = uiState.selectedCpu?.name ?: "Select CPU",
                    iconColor = Color(0xff00e5ff),
                    options = ComparisonData.cpus,
                    onOptionSelected = { viewModel.onCpuSelected(it) },
                    optionLabel = { it.name }
                )

                // Selector: GPU
                SelectorItem(
                    label = "GPU SELECTOR",
                    value = uiState.selectedGpu?.name ?: "Select GPU",
                    iconColor = Color(0xff2ff801),
                    options = ComparisonData.gpus,
                    onOptionSelected = { viewModel.onGpuSelected(it) },
                    optionLabel = { it.name }
                )

                // Selector: Resolution
                SelectorItem(
                    label = "RESOLUTION",
                    value = uiState.selectedResolution.label,
                    iconColor = Color(0xff849396),
                    options = Resolution.entries.toList(),
                    onOptionSelected = { viewModel.onResolutionSelected(it) },
                    optionLabel = { it.label }
                )

                // Calculate Button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(brush = Brush.linearGradient(
                            colors = listOf(Color(0xff00e5ff), Color(0xff2ff801))
                        ))
                        .clickable { viewModel.calculateBottleneck() }
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(32.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "CALCULATE BOTTLENECK",
                        color = Color(0xff001f24),
                        style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.6.sp)
                    )
                }

                // Results Section
                if (uiState.result != null) {
                    val result = uiState.result!!
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = Color(0xff1f1f1f).copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, Color(0xff3b494c).copy(alpha = 0.2f)),
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            Text(
                                text = "ANALYSIS RESULT",
                                color = Color(0xffbac9cc),
                                style = TextStyle(fontSize = 12.sp, letterSpacing = 2.sp)
                            )

                            // Percentage Circle (Simplified visualization)
                            Box(contentAlignment = Alignment.Center) {
                                Box(
                                    modifier = Modifier
                                        .size(160.dp)
                                        .clip(RoundedCornerShape(80.dp))
                                        .border(4.dp, Color(result.statusColor), RoundedCornerShape(80.dp))
                                )
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${result.percentage}%",
                                        color = Color.White,
                                        style = TextStyle(fontSize = 40.sp, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = "IMBALANCE",
                                        color = Color(0xffbac9cc),
                                        style = TextStyle(fontSize = 10.sp)
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = result.status,
                                    color = Color(result.statusColor),
                                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp)
                                )
                                Text(
                                    text = result.description,
                                    color = Color(0xffbac9cc),
                                    textAlign = TextAlign.Center,
                                    style = TextStyle(fontSize = 14.sp)
                                )
                            }

                            // Load Stats
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                LoadStatCard(label = "CPU LOAD", value = "${result.cpuLoad}%", modifier = Modifier.weight(1f))
                                LoadStatCard(label = "GPU LOAD", value = "${result.gpuLoad}%", modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun <T> SelectorItem(
    label: String,
    value: String,
    iconColor: Color,
    options: List<T>,
    onOptionSelected: (T) -> Unit,
    optionLabel: (T) -> String
) {
    var expanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            color = Color(0xffbac9cc),
            style = TextStyle(fontSize = 10.sp, letterSpacing = 2.sp)
        )
        
        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xff1f1f1f))
                    .clickable { expanded = true }
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconColor.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.container),
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    text = value,
                    color = Color.White,
                    style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Medium),
                    modifier = Modifier.weight(1f)
                )
                
                Icon(
                    painter = painterResource(id = R.drawable.icon),
                    contentDescription = null,
                    tint = Color(0xff52525b),
                    modifier = Modifier.size(16.dp).rotate(90f)
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(Color(0xff1f1f1f))
                    .fillMaxWidth(0.85f)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(optionLabel(option), color = Color.White) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LoadStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xff1b1b1b))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(text = label, color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp))
        Text(text = value, color = Color.White, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
    }
}
