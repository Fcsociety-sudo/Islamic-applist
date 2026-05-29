package com.example.data

import android.content.Context
import android.content.SharedPreferences

class SettingsManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("salat_settings", Context.MODE_PRIVATE)

    companion object {
        const val KEY_THEME = "theme_mode_dark"
        const val KEY_AZAN_SOUND = "azan_sound_key"
        const val KEY_CUSTOM_AZAN_PATH = "custom_azan_path"
        const val KEY_NOTIFY_MINUTES_BEFORE = "notify_minutes_before"
        
        const val KEY_NOTIFY_FAJR = "notify_fajr"
        const val KEY_NOTIFY_DHUHR = "notify_dhuhr"
        const val KEY_NOTIFY_ASR = "notify_asr"
        const val KEY_NOTIFY_MAGHRIB = "notify_maghrib"
        const val KEY_NOTIFY_ISHA = "notify_isha"
        
        const val KEY_CITY = "manual_city"
        const val KEY_LAT = "manual_lat"
        const val KEY_LON = "manual_lon"
        const val KEY_CALC_METHOD = "calc_method"
        const val KEY_ASR_METHOD = "asr_method"

        val DEFAULT_CITIES = listOf(
            City("Москва", 55.7558, 37.6173, "Europe/Moscow"),
            City("Казань", 55.7887, 49.1221, "Europe/Moscow"),
            City("Грозный", 43.3183, 45.6946, "Europe/Moscow"),
            City("Махачкала", 42.9849, 47.5046, "Europe/Moscow"),
            City("Уфа", 54.7388, 55.9721, "Asia/Yekaterinburg"),
            City("Санкт-Петербург", 59.9343, 30.3351, "Europe/Moscow"),
            City("Мекка", 21.3891, 39.8579, "Asia/Riyadh"),
            City("Медина", 24.5247, 39.5692, "Asia/Riyadh")
        )
    }

    data class City(val name: String, val latitude: Double, val longitude: Double, val timezoneId: String)

    fun isDarkTheme(): Boolean = prefs.getBoolean(KEY_THEME, true)
    fun setDarkTheme(value: Boolean) = prefs.edit().putBoolean(KEY_THEME, value).apply()

    fun getAzanSound(): String = prefs.getString(KEY_AZAN_SOUND, "Мекка") ?: "Мекка"
    fun setAzanSound(value: String) = prefs.edit().putString(KEY_AZAN_SOUND, value).apply()

    fun getCustomAzanPath(): String? = prefs.getString(KEY_CUSTOM_AZAN_PATH, null)
    fun setCustomAzanPath(value: String?) = prefs.edit().putString(KEY_CUSTOM_AZAN_PATH, value).apply()

    fun isNotifyMinutesBefore(): Boolean = prefs.getBoolean(KEY_NOTIFY_MINUTES_BEFORE, false)
    fun setNotifyMinutesBefore(value: Boolean) = prefs.edit().putBoolean(KEY_NOTIFY_MINUTES_BEFORE, value).apply()

    fun isNotificationEnabled(prayerKey: String): Boolean {
        return prefs.getBoolean(prayerKey, true)
    }
    fun setNotificationEnabled(prayerKey: String, value: Boolean) {
        prefs.edit().putBoolean(prayerKey, value).apply()
    }

    fun getCity(): String = prefs.getString(KEY_CITY, "Москва") ?: "Москва"
    fun getLatitude(): Double = prefs.getFloat(KEY_LAT, 55.7558f).toDouble()
    fun getLongitude(): Double = prefs.getFloat(KEY_LON, 37.6173f).toDouble()

    fun setLocation(cityName: String, latitude: Double, longitude: Double) {
        prefs.edit()
            .putString(KEY_CITY, cityName)
            .putFloat(KEY_LAT, latitude.toFloat())
            .putFloat(KEY_LON, longitude.toFloat())
            .apply()
    }

    fun getCalculationMethod(): String = prefs.getString(KEY_CALC_METHOD, "RUSSIA_DUM") ?: "RUSSIA_DUM"
    fun setCalculationMethod(value: String) = prefs.edit().putString(KEY_CALC_METHOD, value).apply()

    fun getAsrMethod(): String = prefs.getString(KEY_ASR_METHOD, "STANDARD") ?: "STANDARD"
    fun setAsrMethod(value: String) = prefs.edit().putString(KEY_ASR_METHOD, value).apply()

    fun resetToDefaults() {
        prefs.edit().clear().apply()
    }
}
