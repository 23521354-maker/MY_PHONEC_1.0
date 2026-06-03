package com.example.myphonec

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myphonec.ui.components.appearOnEnter
import com.example.myphonec.ui.theme.AppTheme
import com.example.myphonec.ui.theme.BodyLarge
import com.example.myphonec.ui.theme.BodyMedium
import com.example.myphonec.ui.theme.DisplayLarge
import com.example.myphonec.ui.theme.Mono
import com.example.myphonec.ui.theme.RadiusLg

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onSkipLogin: () -> Unit,
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current
    val colors = AppTheme.colors
    val spacing = AppTheme.spacing

    LaunchedEffect(authState.isLoggedIn) {
        if (authState.isLoggedIn) onLoginSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.surfaceBase)
    ) {
        // Ambient light: amber top-left
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.amber.copy(alpha = 0.06f),
                            Color.Transparent,
                        ),
                        center = Offset(0f, 0f),
                        radius = 1400f,
                    )
                )
        )
        // Ambient light: cyan bottom-right
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            colors.cyanPrimary.copy(alpha = 0.08f),
                            Color.Transparent,
                        ),
                        center = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
                        radius = 1400f,
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.Center,
        ) {
            // Tiny brand line
            Row(
                modifier = Modifier.appearOnEnter(delayMillis = 0),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.appicon),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
                Text(
                    text = "PHONEC",
                    style = Mono.copy(letterSpacing = 3.sp),
                    color = colors.textTertiary,
                )
            }

            Spacer(modifier = Modifier.height(spacing.space32))

            // Headline
            Column(
                modifier = Modifier.appearOnEnter(delayMillis = 60),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(text = "Diagnose. Build.", style = DisplayLarge, color = colors.textPrimary)
                Text(text = "Outperform.", style = DisplayLarge, color = colors.cyanPrimary)
            }

            Spacer(modifier = Modifier.height(spacing.space16))

            Text(
                modifier = Modifier.appearOnEnter(delayMillis = 120),
                text = "Sign in to sync your devices and benchmarks across installs.",
                style = BodyLarge,
                color = colors.textSecondary,
            )

            Spacer(modifier = Modifier.height(spacing.space32))

            if (authState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = colors.cyanPrimary)
                }
            } else {
                GoogleSignInButton(
                    modifier = Modifier.appearOnEnter(delayMillis = 180),
                    onClick = { authViewModel.signInWithGoogle(context) },
                )

                Spacer(modifier = Modifier.height(spacing.space12))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .appearOnEnter(delayMillis = 240),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Or continue as guest  →",
                        style = BodyMedium,
                        color = colors.cyanPrimary,
                        modifier = Modifier
                            .clickable(onClick = onSkipLogin)
                            .padding(vertical = 8.dp, horizontal = 12.dp),
                    )
                }
            }

            authState.error?.let { error ->
                Spacer(modifier = Modifier.height(spacing.space16))
                Text(
                    text = error,
                    style = BodyMedium,
                    color = colors.danger,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        // Footer terms
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 32.dp, vertical = 32.dp)
                .appearOnEnter(delayMillis = 300),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(colors.borderSubtle),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "By continuing you agree to the Terms and Privacy Policy.",
                style = BodyMedium,
                color = colors.textTertiary,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun GoogleSignInButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    val colors = AppTheme.colors
    val shape = RoundedCornerShape(RadiusLg)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(shape)
            .background(colors.surfaceLevel2, shape)
            .border(1.dp, colors.borderDefault, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(id = R.drawable.google_icon),
            contentDescription = null,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "Continue with Google",
            style = BodyLarge.copy(color = colors.textPrimary),
        )
    }
}

