package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGold
import com.example.ui.components.IslamicPatternBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onTimeout: () -> Unit) {
    val scale = remember { Animatable(0.7f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        // Run animations concurrently
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    LaunchedEffect(key1 = true) {
        alpha.animateTo(
            targetValue = 1f,
            animationSpec = tween(1200, easing = LinearEasing)
        )
        // Wait for 2 seconds total before transitioning
        delay(2000)
        onTimeout()
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Procedural Gold Arabesque lattices
        IslamicPatternBackground(modifier = Modifier.fillMaxSize())

        GlassCard(
            modifier = Modifier
                .padding(24.dp)
                .scale(scale.value)
                .alpha(alpha.value),
            borderRadius = 32.dp,
            glowing = true
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 32.dp, vertical = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic stylized calligraphic subtitle
                Text(
                    text = "صلاة",
                    color = GlowingGold,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Light,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.Serif
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                // Request requirement text: "Хупри Джамаат" in customized typography
                Text(
                    text = "Хупри Джамаат",
                    color = GlowingGold,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp,
                    textAlign = TextAlign.Center,
                    fontFamily = FontFamily.SansSerif
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "SALAT APP",
                    color = GlowingGold.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 4.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
