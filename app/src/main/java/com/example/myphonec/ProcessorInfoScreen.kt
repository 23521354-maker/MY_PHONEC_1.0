package com.example.myphonec

import android.opengl.GLES20
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myphonec.ui.theme.MyPhoneCTheme
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProcessorInfoScreen(
    viewModel: ProcessorViewModel = viewModel(),
    onBackClick: () -> Unit
) {
    val state by viewModel.processorState.collectAsState()

    LaunchedEffect(Unit) {
        val gpuInfo = getGpuInfo()
        viewModel.updateGpuInfo(gpuInfo.first, gpuInfo.second)
    }

    // Manage lifecycle of live updates
    DisposableEffect(viewModel) {
        viewModel.startLiveUpdates()
        onDispose {
            viewModel.stopLiveUpdates()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(end = 48.dp)) {
                        Text(
                            text = "PROCESSOR",
                            style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = 2.sp)
                        )
                        if (!state.isLoading) {
                            Text(
                                text = state.model,
                                style = TextStyle(fontSize = 10.sp, color = Color(0xffa3a3a3))
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            painter = painterResource(id = R.drawable.back_icon),
                            contentDescription = "Back",
                            tint = Color(0xff22d3ee)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.statusBarsPadding()
            )
        },
        containerColor = Color(0xff131313)
    ) { paddingValues ->
        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xff00e5ff))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                item {
                    ProcessorHeaderCard(state.model, state.maxFreq)
                }

                item {
                    SectionWrapper(title = "CPU OVERVIEW") {
                        InfoRow("Model", state.model, valueColor = Color(0xff00daf3))
                        InfoRow("Cores", state.cores.toString())
                        InfoRow("Hardware", state.hardware)
                        InfoRow("Revision", state.revision)
                        
                        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(text = "Supported ABIs", color = Color(0xffbac9cc), fontSize = 14.sp)
                            ProcessorArchitectureItem("Architecture", state.architecture)
                        }
                    }
                }

                item {
                    SectionWrapper(title = "PERFORMANCE") {
                        InfoRow("Clock Speed Range", "${state.minFreq} - ${state.maxFreq}")
                        
                        Column(modifier = Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text(text = "CPU FREQUENCY DATA", color = Color(0xffbac9cc), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = "Live monitoring active for all available cores", color = Color(0xff2ae500), fontSize = 10.sp)
                        }
                    }
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = "LIVE CORES", color = Color(0xffbac9cc), style = TextStyle(fontSize = 12.sp, letterSpacing = 2.4.sp, fontWeight = FontWeight.Bold))
                        
                        val cores = state.coreDetails
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            for (i in 0 until cores.size step 2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    CoreGridItem(cores[i], Modifier.weight(1f))
                                    if (i + 1 < cores.size) {
                                        CoreGridItem(cores[i+1], Modifier.weight(1f))
                                    } else {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    SectionWrapper(title = "GPU") {
                        InfoRow("GPU Vendor", state.gpuVendor)
                        InfoRow("GPU Renderer", state.gpuRenderer, valueColor = Color(0xff00e5ff))
                    }
                }

                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.technicalschematiclineart),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            alpha = 0.2f,
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
fun ProcessorArchitectureItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xff0e0e0e).copy(alpha = 0.3f))
            .border(BorderStroke(1.dp, Color(0xff3b494c).copy(alpha = 0.05f)), RoundedCornerShape(6.dp))
            .padding(8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = Color(0xffbac9cc), fontSize = 12.sp)
        Text(text = value, color = Color(0xffc3f5ff), fontSize = 12.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun ProcessorHeaderCard(model: String, maxFreq: String) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xff1f1f1f).copy(alpha = 0.7f),
        border = BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.15f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(modifier = Modifier.padding(32.dp)) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xff353535))
                        .border(BorderStroke(1.dp, Color(0xffc3f5ff).copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                        .shadow(6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.container),
                        contentDescription = null,
                        tint = Color(0xff00e5ff),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = model,
                        color = Color(0xffc3f5ff),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "$maxFreq MAX CLOCK",
                        color = Color(0xff2ae500),
                        fontSize = 12.sp,
                        letterSpacing = 1.2.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun CoreGridItem(data: CpuCoreInfo, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xff1f1f1f).copy(alpha = 0.7f))
            .border(BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.15f)), RoundedCornerShape(14.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = "CORE ${data.id}", color = Color(0xffbac9cc), fontSize = 10.sp)
            Text(text = "${(data.usage * 100).toInt()}%", color = Color(0xff2ff801), fontSize = 10.sp)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = data.currentFreq, color = Color(0xffc3f5ff), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            LinearProgressIndicator(
                progress = { data.usage },
                modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                color = Color(0xff2ff801),
                trackColor = Color(0xff353535)
            )
        }
    }
}

fun getGpuInfo(): Pair<String, String> {
    val egl = EGLContext.getEGL() as EGL10
    val display = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
    egl.eglInitialize(display, IntArray(2))
    
    val configSpec = intArrayOf(EGL10.EGL_NONE)
    val configs = arrayOfNulls<EGLConfig>(1)
    val numConfig = IntArray(1)
    egl.eglChooseConfig(display, configSpec, configs, 1, numConfig)
    val config = configs[0] ?: return Pair("Unknown", "Unknown")
    
    val context = egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, null)
    val surface = egl.eglCreatePbufferSurface(display, config, intArrayOf(EGL10.EGL_WIDTH, 1, EGL10.EGL_HEIGHT, 1, EGL10.EGL_NONE))
    
    egl.eglMakeCurrent(display, surface, surface, context)
    
    val renderer = GLES20.glGetString(GLES20.GL_RENDERER) ?: "Unknown"
    val vendor = GLES20.glGetString(GLES20.GL_VENDOR) ?: "Unknown"
    
    egl.eglMakeCurrent(display, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
    egl.eglDestroySurface(display, surface)
    egl.eglDestroyContext(display, context)
    egl.eglTerminate(display)
    
    return Pair(vendor, renderer)
}

@Preview(showBackground = true)
@Composable
fun ProcessorInfoScreenPreview() {
    MyPhoneCTheme {
        ProcessorInfoScreen(onBackClick = {})
    }
}
