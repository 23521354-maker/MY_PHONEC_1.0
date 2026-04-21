package com.example.myphonec

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    onBackClick: () -> Unit,
    viewModel: CompareViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Compare Components", color = Color.White, style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold))
                        Text("CPU VS CPU, GPU VS GPU", color = Color.Gray, style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp))
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color(0xff00e5ff))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        },
        containerColor = Color.Black
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Section Tabs
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.Black,
                contentColor = Color(0xff00e5ff),
                indicator = { tabPositions ->
                    if (selectedTab < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = Color(0xff00e5ff)
                        )
                    }
                },
                divider = {}
            ) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                    Text("CPU COMPARISON", modifier = Modifier.padding(16.dp), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = if (selectedTab == 0) Color(0xff00e5ff) else Color.Gray)
                }
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                    Text("GPU COMPARISON", modifier = Modifier.padding(16.dp), style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp), color = if (selectedTab == 1) Color(0xff00e5ff) else Color.Gray)
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (uiState.isLoading) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = Color(0xff00e5ff))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Synchronizing Hardware Database...", color = Color.Gray, fontSize = 12.sp)
                    }
                } else if (uiState.error != null) {
                    Column(
                        modifier = Modifier.fillMaxSize().padding(32.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = Color.Red, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(uiState.error ?: "Error", color = Color.White, textAlign = TextAlign.Center)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(top = 24.dp, bottom = 40.dp)
                    ) {
                        if (selectedTab == 0) {
                            item {
                                ComparisonSelectors(
                                    itemA = uiState.selectedCpuA?.name ?: "Select CPU",
                                    itemB = uiState.selectedCpuB?.name ?: "Select CPU",
                                    options = uiState.cpus.map { it.name },
                                    onSelectA = { viewModel.selectCpuA(it) },
                                    onSelectB = { viewModel.selectCpuB(it) }
                                )
                            }
                            if (uiState.selectedCpuA != null && uiState.selectedCpuB != null) {
                                val cpuA = uiState.selectedCpuA!!
                                val cpuB = uiState.selectedCpuB!!
                                item {
                                    PerformanceReport(
                                        nameA = cpuA.name,
                                        scoreA = cpuA.score,
                                        subA = "${cpuA.cores} cores / ${cpuA.threads} threads",
                                        nameB = cpuB.name,
                                        scoreB = cpuB.score,
                                        subB = "${cpuB.cores} cores / ${cpuB.threads} threads",
                                        analysisText = generateCpuAnalysis(cpuA, cpuB)
                                    )
                                }
                                item {
                                    TechnicalSpecs(
                                        specs = listOf(
                                            SpecRow("CORES", cpuA.cores.toString(), cpuB.cores.toString(), cpuA.cores > cpuB.cores, cpuB.cores > cpuA.cores),
                                            SpecRow("THREADS", cpuA.threads.toString(), cpuB.threads.toString(), cpuA.threads > cpuB.threads, cpuB.threads > cpuA.threads),
                                            SpecRow("BASE CLOCK", "${cpuA.baseClock} GHz", "${cpuB.baseClock} GHz", cpuA.baseClock > cpuB.baseClock, cpuB.baseClock > cpuA.baseClock),
                                            SpecRow("BOOST CLOCK", "${cpuA.boostClock} GHz", "${cpuB.boostClock} GHz", cpuA.boostClock > cpuB.boostClock, cpuB.boostClock > cpuA.boostClock),
                                            SpecRow("TDP", "${cpuA.tdp}W", "${cpuB.tdp}W", cpuA.tdp < cpuB.tdp, cpuB.tdp < cpuA.tdp),
                                            SpecRow("PROCESS", cpuA.process, cpuB.process, false, false),
                                            SpecRow("SOCKET", cpuA.socket.toString(), cpuB.socket.toString(), false, false)
                                        )
                                    )
                                }
                            }
                        } else {
                            item {
                                ComparisonSelectors(
                                    itemA = uiState.selectedGpuA?.name ?: "Select GPU",
                                    itemB = uiState.selectedGpuB?.name ?: "Select GPU",
                                    options = uiState.gpus.map { it.name },
                                    onSelectA = { viewModel.selectGpuA(it) },
                                    onSelectB = { viewModel.selectGpuB(it) }
                                )
                            }
                            if (uiState.selectedGpuA != null && uiState.selectedGpuB != null) {
                                val gpuA = uiState.selectedGpuA!!
                                val gpuB = uiState.selectedGpuB!!
                                item {
                                    PerformanceReport(
                                        nameA = gpuA.name,
                                        scoreA = gpuA.score,
                                        subA = "${gpuA.vram}GB VRAM",
                                        nameB = gpuB.name,
                                        scoreB = gpuB.score,
                                        subB = "${gpuB.vram}GB VRAM",
                                        analysisText = generateGpuAnalysis(gpuA, gpuB)
                                    )
                                }
                                item {
                                    TechnicalSpecs(
                                        specs = listOf(
                                            SpecRow("VRAM", "${gpuA.vram} GB", "${gpuB.vram} GB", gpuA.vram > gpuB.vram, gpuB.vram > gpuA.vram),
                                            SpecRow("BOOST CLOCK", "${gpuA.boostClock} MHz", "${gpuB.boostClock} MHz", gpuA.boostClock > gpuB.boostClock, gpuB.boostClock > gpuA.boostClock),
                                            SpecRow("TDP", "${gpuA.tdp}W", "${gpuB.tdp}W", gpuA.tdp < gpuB.tdp, gpuB.tdp < gpuA.tdp),
                                            SpecRow("ARCHITECTURE", gpuA.architecture, gpuB.architecture, false, false)
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ComparisonSelectors(
    itemA: String,
    itemB: String,
    options: List<String>,
    onSelectA: (String) -> Unit,
    onSelectB: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min), 
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DropdownSelector(label = "COMPONENT A", selected = itemA, options = options, onSelect = onSelectA, modifier = Modifier.weight(1f))
        DropdownSelector(label = "COMPONENT B", selected = itemB, options = options, onSelect = onSelectB, modifier = Modifier.weight(1f))
    }
}

@Composable
fun DropdownSelector(label: String, selected: String, options: List<String>, onSelect: (String) -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        Surface(
            onClick = { expanded = true },
            shape = RoundedCornerShape(12.dp),
            color = Color(0xff1f1f1f).copy(alpha = 0.6f),
            border = BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.2f)),
            modifier = Modifier.fillMaxHeight().defaultMinSize(minHeight = 84.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth().fillMaxHeight()) {
                Text(label, color = Color(0xffbac9cc), style = TextStyle(fontSize = 10.sp, letterSpacing = 1.sp, fontWeight = FontWeight.Bold))
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = selected, 
                        color = Color.White, 
                        fontSize = 14.sp, 
                        fontWeight = FontWeight.Bold, 
                        modifier = Modifier.weight(1f),
                        minLines = 2
                    )
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color(0xff00e5ff))
                }
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }, modifier = Modifier.background(Color(0xff1f1f1f))) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = Color.White) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Composable
fun PerformanceReport(nameA: String, scoreA: Double, subA: String, nameB: String, scoreB: Double, subB: String, analysisText: String) {
    // We force maxScore to 100.0 as requested
    val maxScore = 100.0
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xff1f1f1f).copy(alpha = 0.6f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Text("ANALYSIS REPORT", color = Color(0xff00e5ff), style = TextStyle(fontSize = 10.sp, letterSpacing = 3.sp, fontWeight = FontWeight.Bold))
                    Text("Total\nPerformance\nScore", color = Color.White, style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.Bold, letterSpacing = (-1).sp))
                }
                Icon(painter = painterResource(id = R.drawable.container), contentDescription = null, tint = Color(0xff00e5ff).copy(alpha = 0.2f), modifier = Modifier.size(48.dp))
            }

            ScoreBar(name = nameA, sub = subA, score = scoreA, maxScore = maxScore, isWinner = scoreA > scoreB)
            
            val diffText = if (scoreB > scoreA && scoreA > 0.0) {
                "+${((scoreB/scoreA - 1) * 100).toInt()}%"
            } else if (scoreA > scoreB && scoreB > 0.0) {
                "+${((scoreA/scoreB - 1) * 100).toInt()}%"
            } else null

            ScoreBar(name = nameB, sub = subB, score = scoreB, maxScore = maxScore, isWinner = scoreB > scoreA, diff = if (scoreB > scoreA) diffText else null)

            Text(text = analysisText, color = Color(0xffbac9cc), fontSize = 14.sp, lineHeight = 22.sp)
        }
    }
}

@Composable
fun ScoreBar(name: String, sub: String, score: Double, maxScore: Double, isWinner: Boolean, diff: String? = null) {
    // Ensure we don't exceed 100%
    val progress = (score / maxScore).coerceIn(0.0, 1.0).toFloat()
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress, 
        animationSpec = tween(1000, easing = FastOutSlowInEasing), 
        label = "progress"
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(name, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    if (diff != null && isWinner) {
                        Surface(color = Color(0xff2ff801).copy(alpha = 0.2f), shape = RoundedCornerShape(99.dp)) {
                            Text(diff, color = Color(0xff2ff801), fontSize = 10.sp, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(sub, color = Color.Gray, fontSize = 10.sp)
            }
            Text(
                text = String.format(Locale.getDefault(), "%.2f", score), 
                color = Color.White, 
                fontSize = 22.sp, 
                fontWeight = FontWeight.Bold
            )
        }
        Box(modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)).background(Color(0xff353535))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(Brush.horizontalGradient(listOf(Color(0xff00e5ff), Color(0xff2ff801))))
            )
        }
    }
}

@Composable
fun TechnicalSpecs(specs: List<SpecRow>) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xff111111),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Text("TECHNICAL SPECIFICATIONS", color = Color.Gray, style = TextStyle(fontSize = 11.sp, letterSpacing = 1.2.sp, fontWeight = FontWeight.Bold))
            specs.forEach { spec ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text(spec.valA, color = if (spec.winA) Color(0xff2ff801) else Color.White, fontSize = 13.sp, fontWeight = if (spec.winA) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                    Text(spec.label, color = Color(0xffbac9cc), fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), letterSpacing = 1.sp)
                    Text(spec.valB, color = if (spec.winB) Color(0xff2ff801) else Color.White, textAlign = TextAlign.End, fontSize = 13.sp, fontWeight = if (spec.winB) FontWeight.Bold else FontWeight.Normal, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

data class SpecRow(val label: String, val valA: String, val valB: String, val winA: Boolean, val winB: Boolean)

fun generateCpuAnalysis(a: CPU, b: CPU): String {
    val winner = if (a.score > b.score) a else b
    val loser = if (winner == a) b else a
    
    val diff = if (winner.score > loser.score && loser.score > 0.0) {
        ((winner.score / loser.score - 1) * 100).toInt()
    } else 0
    
    return buildAnnotatedString {
        append("${winner.name} is approximately ")
        withStyle(SpanStyle(color = Color(0xff00e5ff), fontWeight = FontWeight.Bold)) { append("$diff% faster") }
        append(" than ${loser.name} in overall performance scenarios, particularly in multi-threaded workloads.")
    }.text
}

fun generateGpuAnalysis(a: GPU, b: GPU): String {
    val winner = if (a.score > b.score) a else b
    val loser = if (winner == a) b else a
    
    val diff = if (winner.score > loser.score && loser.score > 0.0) {
        ((winner.score / loser.score - 1) * 100).toInt()
    } else 0

    return buildAnnotatedString {
        append("${winner.name} provides ")
        withStyle(SpanStyle(color = Color(0xff00e5ff), fontWeight = FontWeight.Bold)) { append("$diff% better frame rates") }
        append(" in modern titles and ${if (winner.vram > loser.vram) "handles higher resolution textures better due to ${winner.vram}GB VRAM." else "offers superior power efficiency."}")
    }.text
}
