package com.example.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.SettingsManager
import com.example.ui.components.*
import com.example.util.PrayerTimesCalculator
import java.io.File

@Composable
fun SettingsScreen(
    settingsManager: SettingsManager,
    onThemeChanged: () -> Unit
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // State Variables loaded from settings
    var isDarkTheme by remember { mutableStateOf(settingsManager.isDarkTheme()) }
    var azanSound by remember { mutableStateOf(settingsManager.getAzanSound()) }
    var customAzanPath by remember { mutableStateOf(settingsManager.getCustomAzanPath()) }
    var notifyMinutesBefore by remember { mutableStateOf(settingsManager.isNotifyMinutesBefore()) }
    
    var notifyFajr by remember { mutableStateOf(settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_FAJR)) }
    var notifyDhuhr by remember { mutableStateOf(settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_DHUHR)) }
    var notifyAsr by remember { mutableStateOf(settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_ASR)) }
    var notifyMaghrib by remember { mutableStateOf(settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_MAGHRIB)) }
    var notifyIsha by remember { mutableStateOf(settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_ISHA)) }

    var calcKey by remember { mutableStateOf(settingsManager.getCalculationMethod()) }
    var asrKey by remember { mutableStateOf(settingsManager.getAsrMethod()) }

    var azanDropdownExpanded by remember { mutableStateOf(false) }
    var calcDropdownExpanded by remember { mutableStateOf(false) }
    var asrDropdownExpanded by remember { mutableStateOf(false) }

    // File picker launcher to add local MP3
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val destFile = File(context.filesDir, "custom_azan.mp3")
                inputStream?.use { input ->
                    destFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }
                settingsManager.setCustomAzanPath(destFile.absolutePath)
                settingsManager.setAzanSound("Свой MP3")
                azanSound = "Свой MP3"
                customAzanPath = destFile.absolutePath
                Toast.makeText(context, "Музыкальный файл Azan импортирован!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(context, "Ошибка импорта файла: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .padding(bottom = 72.dp), // offset bottom navigation navbar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        // HEADER: Settings Title
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = "Настройки",
                color = GlowingGold,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Настройка уведомлений, азана и тем приложения",
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 1: Theme selection
        GlassCard(modifier = Modifier.fillMaxWidth(), borderRadius = 24.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Оформление",
                    color = GlowingGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = "Тема",
                            tint = GlowingGold
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = if (isDarkTheme) "Тёмная тема (по умолчанию)" else "Светлая тема",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Switch(
                        checked = isDarkTheme,
                        onCheckedChange = {
                            isDarkTheme = it
                            settingsManager.setDarkTheme(it)
                            onThemeChanged()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GlowingGold,
                            checkedTrackColor = GlowingGold.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.LightGray,
                            uncheckedTrackColor = Color.DarkGray
                        ),
                        modifier = Modifier.testTag("theme_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 2: Notification Toggles for 5 Prayers
        GlassCard(modifier = Modifier.fillMaxWidth(), borderRadius = 24.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Оповещения для намазов",
                    color = GlowingGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                val prayerToggles = listOf(
                    Triple("Фаджр", notifyFajr, SettingsManager.KEY_NOTIFY_FAJR),
                    Triple("Зухр", notifyDhuhr, SettingsManager.KEY_NOTIFY_DHUHR),
                    Triple("Аср", notifyAsr, SettingsManager.KEY_NOTIFY_ASR),
                    Triple("Магриб", notifyMaghrib, SettingsManager.KEY_NOTIFY_MAGHRIB),
                    Triple("Иша", notifyIsha, SettingsManager.KEY_NOTIFY_ISHA)
                )

                prayerToggles.forEachIndexed { i, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.first,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Switch(
                            checked = item.second,
                            onCheckedChange = { checked ->
                                settingsManager.setNotificationEnabled(item.third, checked)
                                when (item.third) {
                                    SettingsManager.KEY_NOTIFY_FAJR -> notifyFajr = checked
                                    SettingsManager.KEY_NOTIFY_DHUHR -> notifyDhuhr = checked
                                    SettingsManager.KEY_NOTIFY_ASR -> notifyAsr = checked
                                    SettingsManager.KEY_NOTIFY_MAGHRIB -> notifyMaghrib = checked
                                    SettingsManager.KEY_NOTIFY_ISHA -> notifyIsha = checked
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = GlowingGold,
                                checkedTrackColor = GlowingGold.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.testTag("switch_${item.third}")
                        )
                    }
                    if (i < prayerToggles.size - 1) {
                        HorizontalDivider(color = Color.White.copy(alpha = 0.08f))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.15f))
                Spacer(modifier = Modifier.height(12.dp))

                // Alert 5 mins before toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Оповещение заранее",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Напомнить за 5 минут до наступления времени",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 12.sp
                        )
                    }

                    Switch(
                        checked = notifyMinutesBefore,
                        onCheckedChange = {
                            notifyMinutesBefore = it
                            settingsManager.setNotifyMinutesBefore(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GlowingGold,
                            checkedTrackColor = GlowingGold.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.testTag("pre_alarm_switch")
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 3: Azan Audio Choice
        GlassCard(modifier = Modifier.fillMaxWidth(), borderRadius = 24.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Звуковой сигнал (Азан)",
                    color = GlowingGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Selector Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { azanDropdownExpanded = !azanDropdownExpanded }
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = "Звук",
                                tint = GlowingGold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Азан: $azanSound",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        Icon(
                            imageVector = if (azanDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                            contentDescription = "Выпадающий список",
                            tint = GlowingGold
                        )
                    }

                    DropdownMenu(
                        expanded = azanDropdownExpanded,
                        onDismissRequest = { azanDropdownExpanded = false },
                        modifier = Modifier
                            .background(DeepSapphire)
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                    ) {
                        listOf("Мекка", "Медина").forEach { sound ->
                            DropdownMenuItem(
                                text = { Text(sound, color = Color.White) },
                                onClick = {
                                    azanSound = sound
                                    settingsManager.setAzanSound(sound)
                                    azanDropdownExpanded = false
                                }
                            )
                        }
                        customAzanPath?.let {
                            DropdownMenuItem(
                                text = { Text("Свой MP3 (Активный)", color = GlowingGold) },
                                onClick = {
                                    azanSound = "Свой MP3"
                                    settingsManager.setAzanSound("Свой MP3")
                                    azanDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Launcher button for file picker
                GlassButton(
                    onClick = { filePickerLauncher.launch("audio/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("import_mp3_btn")
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LibraryMusic,
                            contentDescription = "MP3",
                            tint = GlowingGold
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Добавить свой MP3",
                            color = GlowingGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                customAzanPath?.let { path ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Сохранен локально: ...${path.takeLast(25)}",
                        color = Color.White.copy(alpha = 0.4f),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // SECTION 4: Advanced Calculation Methods
        GlassCard(modifier = Modifier.fillMaxWidth(), borderRadius = 24.dp) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Методы астрономического расчета",
                    color = GlowingGold,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Calculation formulas
                Text(
                    text = "Органы управления методом Фаджр/Иша:",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { calcDropdownExpanded = !calcDropdownExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = PrayerTimesCalculator.Method.valueOf(calcKey).label,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = GlowingGold)
                    }

                    DropdownMenu(
                        expanded = calcDropdownExpanded,
                        onDismissRequest = { calcDropdownExpanded = false },
                        modifier = Modifier.background(DeepSapphire)
                    ) {
                        PrayerTimesCalculator.Method.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.label, color = Color.White) },
                                onClick = {
                                    calcKey = method.name
                                    settingsManager.setCalculationMethod(method.name)
                                    calcDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Asr Juristic (Std vs Hanafi)
                Text(
                    text = "Расчет времени намаза Аср:",
                    color = Color.White.copy(alpha = 0.5f),
                    fontSize = 12.sp
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .clickable { asrDropdownExpanded = !asrDropdownExpanded }
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = PrayerTimesCalculator.AsrJuristic.valueOf(asrKey).label,
                            color = Color.White,
                            fontSize = 14.sp
                        )
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = GlowingGold)
                    }

                    DropdownMenu(
                        expanded = asrDropdownExpanded,
                        onDismissRequest = { asrDropdownExpanded = false },
                        modifier = Modifier.background(DeepSapphire)
                    ) {
                        PrayerTimesCalculator.AsrJuristic.values().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method.label, color = Color.White) },
                                onClick = {
                                    asrKey = method.name
                                    settingsManager.setAsrMethod(method.name)
                                    asrDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // SECTION 5: Hard Reset to Defaults
        Button(
            onClick = {
                settingsManager.resetToDefaults()
                // delete locally copied MP3 if exists
                try {
                    val file = File(context.filesDir, "custom_azan.mp3")
                    if (file.exists()) {
                        file.delete()
                    }
                } catch (e: Exception) {}

                // Reload state
                isDarkTheme = settingsManager.isDarkTheme()
                azanSound = settingsManager.getAzanSound()
                customAzanPath = settingsManager.getCustomAzanPath()
                notifyMinutesBefore = settingsManager.isNotifyMinutesBefore()
                notifyFajr = settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_FAJR)
                notifyDhuhr = settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_DHUHR)
                notifyAsr = settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_ASR)
                notifyMaghrib = settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_MAGHRIB)
                notifyIsha = settingsManager.isNotificationEnabled(SettingsManager.KEY_NOTIFY_ISHA)
                calcKey = settingsManager.getCalculationMethod()
                asrKey = settingsManager.getAsrMethod()

                onThemeChanged()
                Toast.makeText(context, "Настройки сброшены на стандартные", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD62828)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .testTag("reset_settings_btn")
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteForever,
                    contentDescription = "Сброс",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Сбросить настройки",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
