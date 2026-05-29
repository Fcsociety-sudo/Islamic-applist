package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HadithsData
import com.example.ui.components.GlassButton
import com.example.ui.components.GlassCard
import com.example.ui.components.GlowingGold

@Composable
fun HadithScreen() {
    val hadiths = remember { HadithsData.list }
    var currentIndex by remember { mutableStateOf(0) }
    var slideDirectionRight by remember { mutableStateOf(true) }

    // Retrieve active Hadith
    val activeHadith = remember(currentIndex) { hadiths[currentIndex] }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 12.dp)
            .padding(bottom = 72.dp), // offset bottom navigation navbar
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // UPPER DECK: Header
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Изречения Пророка",
                color = GlowingGold,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Имамы Аль-Бухари и Муслим",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                textAlign = TextAlign.Center
            )
        }

        // CENTER DECK: Glassmorphic Deck with sliding transition
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = activeHadith,
                transitionSpec = {
                    if (slideDirectionRight) {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> width } + fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300)) { width -> -width } + fadeOut(animationSpec = tween(300)))
                    } else {
                        (slideInHorizontally(animationSpec = tween(300)) { width -> -width } + fadeIn(animationSpec = tween(300))) togetherWith
                                (slideOutHorizontally(animationSpec = tween(300)) { width -> width } + fadeOut(animationSpec = tween(300)))
                    }
                },
                label = "HadithSlide"
            ) { targetHadith ->
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .padding(horizontal = 4.dp),
                    borderRadius = 32.dp,
                    glowing = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Decorative Quote Mark
                        Icon(
                            imageVector = Icons.Default.FormatQuote,
                            contentDescription = "Цитата",
                            tint = GlowingGold.copy(alpha = 0.2f),
                            modifier = Modifier.size(56.dp)
                        )

                        // Dual Language Container
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // LARGE ARABIC calligraphic style
                            Text(
                                text = targetHadith.textArabic,
                                color = GlowingGold,
                                fontSize = if (targetHadith.textArabic.length > 80) 20.sp else 23.sp,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                fontFamily = FontFamily.Serif,
                                lineHeight = 36.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 20.dp)
                            )

                            // SMALL RUSSIAN literal details
                            Text(
                                text = targetHadith.textRussian,
                                color = Color.White,
                                fontSize = if (targetHadith.textRussian.length > 150) 14.sp else 16.sp,
                                fontWeight = FontWeight.Light,
                                fontStyle = FontStyle.Normal,
                                textAlign = TextAlign.Center,
                                lineHeight = 24.sp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp)
                            )
                        }

                        // SOURCE details
                        Text(
                            text = "— ${targetHadith.source}",
                            color = GlowingGold.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            fontStyle = FontStyle.Italic,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }
                }
            }
        }

        // LOWER DECK: Navigation actions & linear progressive track
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // PROGRESS BAR and indicators (e.g. 1 / 50)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Прогресс чтений",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Text(
                    text = "${currentIndex + 1} из ${hadiths.size}",
                    color = GlowingGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("hadith_progress_txt")
                )
            }

            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / hadiths.size },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .padding(horizontal = 12.dp),
                color = GlowingGold,
                trackColor = Color.White.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // PREV / NEXT Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                GlassButton(
                    onClick = {
                        if (currentIndex > 0) {
                            slideDirectionRight = false
                            currentIndex--
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("prev_hadith_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад",
                            tint = if (currentIndex > 0) GlowingGold else GlowingGold.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Назад",
                            color = if (currentIndex > 0) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                GlassButton(
                    onClick = {
                        if (currentIndex < hadiths.size - 1) {
                            slideDirectionRight = true
                            currentIndex++
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("next_hadith_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Вперед",
                            color = if (currentIndex < hadiths.size - 1) Color.White else Color.White.copy(alpha = 0.3f),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Вперед",
                            tint = if (currentIndex < hadiths.size - 1) GlowingGold else GlowingGold.copy(alpha = 0.3f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
