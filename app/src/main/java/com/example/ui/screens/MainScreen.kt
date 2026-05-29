package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.SettingsManager
import com.example.ui.components.*
import com.example.util.PrayerTimesCalculator
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.text.SimpleDateFormat
import java.util.*
import kotlinx.coroutines.delay

@SuppressLint("MissingPermission")
@Composable
fun MainScreen(settingsManager: SettingsManager) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Location State
    var gpsCityName by remember { mutableStateOf<String?>(null) }
    var currentLatitude by remember { mutableStateOf(settingsManager.getLatitude()) }
    var currentLongitude by remember { mutableStateOf(settingsManager.getLongitude()) }
    var activeCity by remember { mutableStateOf(settingsManager.getCity()) }

    // Dropdown list models
    var cityExpanded by remember { mutableStateOf(false) }

    // Timer & Prayer state updates
    var currentTime by remember { mutableStateOf(Calendar.getInstance()) }
    var calculationMethod by remember { mutableStateOf(settingsManager.getCalculationMethod()) }
    var asrMethod by remember { mutableStateOf(settingsManager.getAsrMethod()) }

    // Launcher for Location permission
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                .addOnSuccessListener { loc: Location? ->
                    loc?.let {
                        currentLatitude = it.latitude
                        currentLongitude = it.longitude
                        activeCity = "По геолокации"
                        gpsCityName = "Мое местоположение"
                        settingsManager.setLocation("По геолокации", it.latitude, it.longitude)
                    }
                }
        }
    }

    // Effect to auto-detect GPS upon load if permission exists
    LaunchedEffect(key1 = true) {
        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (fineGranted || coarseGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener { loc: Location? ->
                loc?.let {
                    currentLatitude = it.latitude
                    currentLongitude = it.longitude
                    activeCity = settingsManager.getCity()
                }
            }
        } else {
            // Request permissions on first launcher
            locationPermissionLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
            )
        }
    }

    // Update current time ticks and load settings dynamically
    LaunchedEffect(key1 = true) {
        while (true) {
            currentTime = Calendar.getInstance()
            calculationMethod = settingsManager.getCalculationMethod()
            asrMethod = settingsManager.getAsrMethod()
            delay(1000) // tick every second to keep countdown precise
        }
    }

    // Compute Prayer Times
    val year = currentTime.get(Calendar.YEAR)
    val month = currentTime.get(Calendar.MONTH) + 1
    val day = currentTime.get(Calendar.DAY_OF_MONTH)
    val timeZoneOffset = (currentTime.timeZone.rawOffset + currentTime.timeZone.dstSavings).toDouble() / 3600000.0

    val times = remember(currentLatitude, currentLongitude, year, month, day, calculationMethod, asrMethod) {
        PrayerTimesCalculator.calculateTimes(
            latitude = currentLatitude,
            longitude = currentLongitude,
            timezone = timeZoneOffset,
            year = year,
            month = month,
            day = day,
            method = PrayerTimesCalculator.Method.valueOf(calculationMethod),
            asrJuristic = PrayerTimesCalculator.AsrJuristic.valueOf(asrMethod)
        )
    }

    // Determine currently highlighted and upcoming prayers
    val prayerList = remember(times) {
        listOf(
            "Фаджр" to times.fajr,
            "Зухр" to times.dhuhr,
            "Аср" to times.asr,
            "Магриб" to times.maghrib,
            "Иша" to times.isha
        )
    }

    val parsedTimeNowMinutes = currentTime.get(Calendar.HOUR_OF_DAY) * 60 + currentTime.get(Calendar.MINUTE)

    fun parseStringToMinutes(str: String): Int {
        val parts = str.split(":")
        if (parts.size == 2) {
            return parts[0].toInt() * 60 + parts[1].toInt()
        }
        return 0
    }

    // Map prayer list to its absolute minutes
    val minuteList = remember(prayerList) {
        prayerList.map { it.first to parseStringToMinutes(it.second) }
    }

    // Find the current prayer
    val activePrayerName = remember(parsedTimeNowMinutes, minuteList) {
        var active = "Иша"
        if (parsedTimeNowMinutes < minuteList[0].second) {
            active = "Иша" // past midnight is technically still under Isha's night interval
        } else {
            for (i in 0 until minuteList.size - 1) {
                if (parsedTimeNowMinutes >= minuteList[i].second && parsedTimeNowMinutes < minuteList[i+1].second) {
                    active = minuteList[i].first
                    break
                }
            }
            if (parsedTimeNowMinutes >= minuteList.last().second) {
                active = "Иша"
            }
        }
        active
    }

    // Determine next prayer and time remaining
    val secondsTick = currentTime.get(Calendar.SECOND)
    val countdownText = remember(parsedTimeNowMinutes, secondsTick, minuteList) {
        var nextName = "Фаджр"
        var diffMinutes = 0
        var diffSeconds = 60 - secondsTick

        val fajrTime = minuteList[0].second
        val dhuhrTime = minuteList[1].second
        val asrTime = minuteList[2].second
        val maghribTime = minuteList[3].second
        val ishaTime = minuteList[4].second

        if (parsedTimeNowMinutes < fajrTime) {
            nextName = "Фаджр"
            diffMinutes = fajrTime - parsedTimeNowMinutes - 1
        } else if (parsedTimeNowMinutes < dhuhrTime) {
            nextName = "Зухр"
            diffMinutes = dhuhrTime - parsedTimeNowMinutes - 1
        } else if (parsedTimeNowMinutes < asrTime) {
            nextName = "Аср"
            diffMinutes = asrTime - parsedTimeNowMinutes - 1
        } else if (parsedTimeNowMinutes < maghribTime) {
            nextName = "Магриб"
            diffMinutes = maghribTime - parsedTimeNowMinutes - 1
        } else if (parsedTimeNowMinutes < ishaTime) {
            nextName = "Иша"
            diffMinutes = ishaTime - parsedTimeNowMinutes - 1
        } else {
            nextName = "Фаджр"
            // Isha to next day Fajr is length (1440 - parsedNow) + Fajr
            diffMinutes = (1440 - parsedTimeNowMinutes) + fajrTime - 1
        }

        val h = diffMinutes / 60
        val m = diffMinutes % 60
        "До $nextName: ${if (h > 0) "${h}ч " else ""}${m}мин ${diffSeconds}сек"
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

        // HEADER: App Identity
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "صلاة",
                    color = GlowingGold,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Время намаза",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp
                )
            }

            // Location Quick-Action
            IconButton(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    )
                },
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White.copy(alpha = 0.1f))
                    .testTag("gps_detect_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Определить по GPS",
                    tint = GlowingGold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Region / Manual Settings Glass Card
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderRadius = 24.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { cityExpanded = !cityExpanded }
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Регион",
                            tint = GlowingGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = activeCity,
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Icon(
                        imageVector = if (cityExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "Выбор городов",
                        tint = GlowingGold
                    )
                }

                DropdownMenu(
                    expanded = cityExpanded,
                    onDismissRequest = { cityExpanded = false },
                    modifier = Modifier
                        .background(DeepSapphire)
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                ) {
                    // Option for current GPS Location fallback
                    gpsCityName?.let { gpsName ->
                        DropdownMenuItem(
                            text = { Text("🛰️ Местоположение (GPS)", color = GlowingGold) },
                            onClick = {
                                activeCity = "По геолокации"
                                settingsManager.setLocation("По геолокации", currentLatitude, currentLongitude)
                                cityExpanded = false
                            }
                        )
                    }

                    SettingsManager.DEFAULT_CITIES.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city.name, color = Color.White) },
                            onClick = {
                                activeCity = city.name
                                currentLatitude = city.latitude
                                currentLongitude = city.longitude
                                settingsManager.setLocation(city.name, city.latitude, city.longitude)
                                cityExpanded = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // LIVE COUNTDOWN WIDGET: Apple Glass Glassmorphic style with neon glow
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            borderRadius = 28.dp,
            glowing = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = countdownText,
                    color = GlowingGold,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(4.dp))
                val sdf = SimpleDateFormat("dd MMMM, EEEE (HH:mm)", Locale("ru"))
                Text(
                    text = sdf.format(currentTime.time),
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PRAYER TIMES TABLE: Beautiful Glass cards listing each daily prayer
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            prayerList.forEach { (name, time) ->
                val isActive = name == activePrayerName
                
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderRadius = 20.dp,
                    glowing = isActive,
                    borderWidth = if (isActive) 1.5.dp else 1.dp
                ) {
                    val cardBackground = if (isActive) {
                        Brush.verticalGradient(
                            listOf(
                                GlowingGold.copy(alpha = 0.15f),
                                GlowingGold.copy(alpha = 0.02f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.05f),
                                Color.White.copy(alpha = 0.01f)
                            )
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(cardBackground)
                            .padding(horizontal = 20.dp, vertical = 20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = when (name) {
                                    "Фаджр" -> Icons.Default.WbTwilight
                                    "Зухр" -> Icons.Default.WbSunny
                                    "Аср" -> Icons.Default.WbCloudy
                                    "Магриб" -> Icons.Default.NightlightRound
                                    else -> Icons.Default.Bedtime
                                },
                                contentDescription = name,
                                tint = if (isActive) GlowingGold else Color.White.copy(alpha = 0.5f),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = name,
                                color = if (isActive) GlowingGold else Color.White,
                                fontSize = 18.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isActive) {
                                Text(
                                    text = "ТЕКУЩИЙ",
                                    color = GlowingGold,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    modifier = Modifier
                                        .border(1.dp, GlowingGold, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                            }
                            
                            Text(
                                text = time,
                                color = if (isActive) GlowingGold else Color.White,
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Extra details under prayer list: calculations
        Text(
            text = "Метод расчета: ${PrayerTimesCalculator.Method.valueOf(calculationMethod).label}\nАср: ${PrayerTimesCalculator.AsrJuristic.valueOf(asrMethod).label}",
            color = Color.White.copy(alpha = 0.4f),
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
            lineHeight = 16.sp,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
