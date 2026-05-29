package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.components.*
import com.example.ui.screens.HadithScreen
import com.example.ui.screens.MainScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.MyApplicationTheme

enum class SalatScreen {
    SPLASH, PRAYERS, HADITHS, SETTINGS
}

class MainActivity : ComponentActivity() {
    private lateinit var settingsManager: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        settingsManager = SettingsManager(this)
        enableEdgeToEdge()

        setContent {
            var isDarkTheme by remember { mutableStateOf(settingsManager.isDarkTheme()) }
            var currentScreen by remember { mutableStateOf(SalatScreen.SPLASH) }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                // Wrapper taking full-bleed content matching edge-to-edge guidelines
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    if (currentScreen == SalatScreen.SPLASH) {
                        SplashScreen(onTimeout = {
                            currentScreen = SalatScreen.PRAYERS
                        })
                    } else {
                        // Drawing the procedural geometric Islamic Star/Arabesque latticed background
                        if (isDarkTheme) {
                            IslamicPatternBackground(modifier = Modifier.fillMaxSize())
                        } else {
                            // light theme background
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.background)
                            )
                        }

                        // Scaffold to safely overlay screens and custom glass floats
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            containerColor = Color.Transparent, // let the geometric lattice shine through
                            contentWindowInsets = WindowInsets.safeDrawing,
                            bottomBar = {
                                // FLOATING APPLE GLASS NAVIGATION CAPSULE
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 12.dp)
                                        .windowInsetsPadding(WindowInsets.navigationBars)
                                        .testTag("glass_bottom_nav"),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    GlassCard(
                                        modifier = Modifier.wrapContentHeight(),
                                        borderRadius = 24.dp,
                                        borderWidth = 1.dp,
                                        glowing = false
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            horizontalArrangement = Arrangement.SpaceAround,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            // TAB 1: Prayers (Намаз)
                                            val isPrayersSelected = currentScreen == SalatScreen.PRAYERS
                                            IconButton(
                                                onClick = { currentScreen = SalatScreen.PRAYERS },
                                                modifier = Modifier
                                                    .testTag("nav_prayers_tab")
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.AccessTime,
                                                        contentDescription = "Намаз",
                                                        tint = if (isPrayersSelected) GlowingGold else Color.White.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        text = "Намаз",
                                                        fontSize = 10.sp,
                                                        color = if (isPrayersSelected) GlowingGold else Color.White.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }

                                            // TAB 2: Hadiths (Хадисы)
                                            val isHadithsSelected = currentScreen == SalatScreen.HADITHS
                                            IconButton(
                                                onClick = { currentScreen = SalatScreen.HADITHS },
                                                modifier = Modifier
                                                    .testTag("nav_hadiths_tab")
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.MenuBook,
                                                        contentDescription = "Хадисы",
                                                        tint = if (isHadithsSelected) GlowingGold else Color.White.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        text = "Хадисы",
                                                        fontSize = 10.sp,
                                                        color = if (isHadithsSelected) GlowingGold else Color.White.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }

                                            // TAB 3: Settings (Настройки)
                                            val isSettingsSelected = currentScreen == SalatScreen.SETTINGS
                                            IconButton(
                                                onClick = { currentScreen = SalatScreen.SETTINGS },
                                                modifier = Modifier
                                                    .testTag("nav_settings_tab")
                                            ) {
                                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                                    Icon(
                                                        imageVector = Icons.Default.Settings,
                                                        contentDescription = "Настройки",
                                                        tint = if (isSettingsSelected) GlowingGold else Color.White.copy(alpha = 0.5f),
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                    Text(
                                                        text = "Опции",
                                                        fontSize = 10.sp,
                                                        color = if (isSettingsSelected) GlowingGold else Color.White.copy(alpha = 0.5f)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        ) { paddingValue ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(paddingValue)
                            ) {
                                when (currentScreen) {
                                    SalatScreen.PRAYERS -> MainScreen(settingsManager = settingsManager)
                                    SalatScreen.HADITHS -> HadithScreen()
                                    SalatScreen.SETTINGS -> SettingsScreen(
                                        settingsManager = settingsManager,
                                        onThemeChanged = {
                                            isDarkTheme = settingsManager.isDarkTheme()
                                        }
                                    )
                                    else -> Unit
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
