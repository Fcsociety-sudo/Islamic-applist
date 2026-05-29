package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

// Sophisticated custom colors
val CosmicDarkBlue = Color(0xFF03071E)
val DeepSapphire = Color(0xFF0D1B2A)
val GlassWhite = Color(0x15FFFFFF)
val GlowingGold = Color(0xFFE0A96D)
val BurnedGold = Color(0xFF9C7A4F)
val NeonCyan = Color(0xFF00F5D4)

@Composable
fun IslamicPatternBackground(
    modifier: Modifier = Modifier,
    patternColor: Color = Color(0x18E0A96D), // very subtle translucent gold
    backgroundColor: Color = CosmicDarkBlue
) {
    Box(
        modifier = modifier
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        DeepSapphire,
                        backgroundColor
                    ),
                    center = Offset.Zero
                )
            )
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val width = size.width
            val height = size.height
            val stepX = 140.dp.toPx()
            val stepY = 140.dp.toPx()

            // Draw a grid of Islamic 8-pointed star geometric patterns
            val cols = (width / stepX).toInt() + 2
            val rows = (height / stepY).toInt() + 2

            for (c in -1..cols) {
                for (r in -1..rows) {
                    val cx = c * stepX + (if (r % 2 == 0) stepX / 2 else 0f)
                    val cy = r * stepY
                    val radius = 45.dp.toPx()

                    // Draw 8-pointed star
                    val starPath = Path()
                    val numPoints = 8
                    for (i in 0 until numPoints * 2) {
                        val angle = i * Math.PI / numPoints
                        val rLen = if (i % 2 == 0) radius else radius * 0.54f
                        val x = cx + (rLen * cos(angle)).toFloat()
                        val y = cy + (rLen * sin(angle)).toFloat()
                        if (i == 0) {
                            starPath.moveTo(x, y)
                        } else {
                            starPath.lineTo(x, y)
                        }
                    }
                    starPath.close()
                    drawPath(
                        path = starPath,
                        color = patternColor,
                        style = Stroke(width = 1.dp.toPx())
                    )

                    // Connecting lattice lines
                    drawCircle(
                        color = patternColor.copy(alpha = patternColor.alpha * 1.5f),
                        radius = 2.dp.toPx(),
                        center = Offset(cx, cy)
                    )

                    // Draw octagonal links
                    val octPath = Path()
                    for (i in 0 until 8) {
                        val angle = i * Math.PI / 4 + Math.PI / 8
                        val x = cx + (radius * 0.85f * cos(angle)).toFloat()
                        val y = cy + (radius * 0.85f * sin(angle)).toFloat()
                        if (i == 0) octPath.moveTo(x, y) else octPath.lineTo(x, y)
                    }
                    octPath.close()
                    drawPath(
                        path = octPath,
                        color = patternColor.copy(alpha = patternColor.alpha * 0.5f),
                        style = Stroke(width = 0.5.dp.toPx())
                    )
                }
            }
        }
    }
}

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderRadius: Dp = 32.dp,
    borderWidth: Dp = 1.5.dp,
    glowing: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val gradientBorder = Brush.linearGradient(
        colors = if (glowing) {
            listOf(GlowingGold.copy(alpha = 0.8f), GlowingGold.copy(alpha = 0.1f), GlowingGold.copy(alpha = 0.5f))
        } else {
            listOf(Color.White.copy(alpha = 0.25f), Color.White.copy(alpha = 0.03f), BurnedGold.copy(alpha = 0.15f))
        }
    )

    val shadowColor = if (glowing) GlowingGold.copy(alpha = 0.18f) else Color.Transparent

    Box(
        modifier = modifier
            // Simple premium layout styling with thin double borders and soft shadows
            .clip(RoundedCornerShape(borderRadius))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.08f),
                        Color.White.copy(alpha = 0.02f)
                    )
                )
            )
            .border(
                width = borderWidth,
                brush = gradientBorder,
                shape = RoundedCornerShape(borderRadius)
            ),
        content = content
    )
}

@Composable
fun GlassButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    active: Boolean = false,
    glowing: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val borderBrush = if (active || glowing) {
        Brush.linearGradient(listOf(GlowingGold, BurnedGold))
    } else {
        Brush.linearGradient(listOf(Color.White.copy(alpha = 0.2f), Color.White.copy(alpha = 0.05f)))
    }

    val backgroundBrush = if (active) {
        Brush.verticalGradient(listOf(GlowingGold.copy(alpha = 0.25f), GlowingGold.copy(alpha = 0.05f)))
    } else {
        Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.07f), Color.White.copy(alpha = 0.01f)))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundBrush)
            .border(1.dp, borderBrush, RoundedCornerShape(24.dp))
            .clickable(
                onClick = onClick,
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(color = GlowingGold)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center,
        content = content
    )
}
