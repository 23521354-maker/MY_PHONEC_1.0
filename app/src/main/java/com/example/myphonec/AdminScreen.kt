package com.example.myphonec

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminScreen(
    onBackClick: () -> Unit,
    viewModel: AdminViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    var jsonInput by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xff0a0a0a))
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        Text(
            text = "HARDWARE IMPORT TOOL",
            color = Color(0xff22d3ee),
            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Black, letterSpacing = 2.sp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        // JSON Input Area
        OutlinedTextField(
            value = jsonInput,
            onValueChange = { jsonInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = { Text("Paste JSON here", color = Color.Gray) },
            textStyle = TextStyle(color = Color.White, fontSize = 14.sp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xff22d3ee),
                unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Status Area
        if (uiState.message.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xff1a1a1a))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = uiState.message, color = Color.White, style = TextStyle(fontSize = 14.sp))
                    if (uiState.progress.isNotEmpty()) {
                        Text(text = uiState.progress, color = Color(0xff22d3ee), style = TextStyle(fontSize = 12.sp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // Action Buttons
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminButton("IMPORT CPUS", Color(0xff22d3ee), Modifier.weight(1f)) {
                    viewModel.importCPUs(jsonInput)
                }
                AdminButton("IMPORT GPUS", Color(0xff22d3ee), Modifier.weight(1f)) {
                    viewModel.importGPUs(jsonInput)
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminButton("CLEAR CPUS", Color(0xfff87171), Modifier.weight(1f)) {
                    viewModel.clearCPUs()
                }
                AdminButton("CLEAR GPUS", Color(0xfff87171), Modifier.weight(1f)) {
                    viewModel.clearGPUs()
                }
            }
            
            Button(
                onClick = {
                    jsonInput = getSampleJson()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.05f))
            ) {
                Text("LOAD SAMPLE JSON")
            }

            Button(
                onClick = onBackClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
            ) {
                Text("BACK TO DASHBOARD", color = Color.Gray)
            }
        }
    }

    if (uiState.isLoading) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {},
            title = { Text("Processing...") },
            text = { 
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xff22d3ee)) 
                }
            },
            containerColor = Color(0xff1a1a1a),
            titleContentColor = Color.White
        )
    }
}

@Composable
fun AdminButton(text: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color.copy(alpha = 0.1f)),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(text = text, color = color, style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold))
    }
}

fun getSampleJson(): String {
    return """
        [
          {
            "id": "ryzen_9_7950x",
            "name": "AMD Ryzen 9 7950X",
            "brand": "AMD",
            "cores": 16,
            "threads": 32,
            "baseClock": 4.5,
            "boostClock": 5.7,
            "score": 63200,
            "socket": "AM5",
            "tdp": 170,
            "process": "5nm",
            "description": "The ultimate gaming and workstation processor."
          },
          {
            "id": "i9_14900k",
            "name": "Intel Core i9-14900K",
            "brand": "Intel",
            "cores": 24,
            "threads": 32,
            "baseClock": 3.2,
            "boostClock": 6.0,
            "score": 65400,
            "socket": "LGA1700",
            "tdp": 125,
            "process": "Intel 7",
            "description": "Intel's fastest desktop processor."
          }
        ]
    """.trimIndent()
}
