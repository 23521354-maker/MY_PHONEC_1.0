package com.example.myphonec

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onSkipLogin: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = Color.Black)
    ) {
        // Background Glows
        Box(
            modifier = Modifier
                .align(alignment = Alignment.TopStart)
                .offset(x = (-39).dp, y = (-100).dp)
                .requiredWidth(width = 234.dp)
                .requiredHeight(height = 530.dp)
                .clip(shape = RoundedCornerShape(9999.dp))
                .blur(radius = 120.dp)
                .background(color = Color(0xff00e5ff).copy(alpha = 0.1f))
        )
        Box(
            modifier = Modifier
                .align(alignment = Alignment.BottomEnd)
                .offset(x = 39.dp, y = 100.dp)
                .requiredWidth(width = 195.dp)
                .requiredHeight(height = 442.dp)
                .clip(shape = RoundedCornerShape(9999.dp))
                .blur(radius = 100.dp)
                .background(color = Color(0xff00e5ff).copy(alpha = 0.05f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Title
            Text(
                text = "PHONEC",
                color = Color(0xff00e5ff),
                fontStyle = FontStyle.Normal,
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 60.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-3).sp
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Subtitle
            Text(
                text = "Connect your device and PC tools",
                color = Color(0xffbac9cc),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.45.sp
                ),
                modifier = Modifier.padding(bottom = 80.dp)
            )

            // Sign in with Google Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .clickable { 
                        authViewModel.onSignInSuccess("User", "user@example.com")
                        onLoginSuccess() 
                    },
                color = Color.White,
                shadowElevation = 8.dp
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google_icon),
                        contentDescription = "Google",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Sign in with Google",
                        color = Color(0xff18181b),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Continue without login
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(9999.dp))
                    .border(
                        BorderStroke(1.dp, Color(0xff00e5ff).copy(alpha = 0.3f)),
                        RoundedCornerShape(9999.dp)
                    )
                    .clickable { 
                        authViewModel.onContinueAsGuest()
                        onSkipLogin() 
                    },
                color = Color.Transparent
            ) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text(
                        text = "CONTINUE WITHOUT LOGIN",
                        color = Color(0xff00e5ff),
                        style = TextStyle(
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 1.4.sp
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        painter = painterResource(id = R.drawable.container),
                        contentDescription = null,
                        tint = Color(0xff00e5ff),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    LoginScreen(onLoginSuccess = {}, onSkipLogin = {})
}
