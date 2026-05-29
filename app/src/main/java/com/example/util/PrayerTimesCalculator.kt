package com.example.util

import java.lang.Math.*
import java.util.Calendar
import java.util.Date
import java.util.TimeZone

object PrayerTimesCalculator {
    
    enum class Method(val label: String, val fajrAngle: Double, val ishaAngle: Double) {
        MWL("Всемирная Мусульманская Лига (MWL)", 18.0, 17.0),
        ISNA("ИСНА (Северная Америка)", 15.0, 15.0),
        EGYPT("Египет", 19.5, 17.5),
        RUSSIA_DUM("ДУМ РФ (Россия)", 16.0, 15.0),
        KUWAIT("Кувейт", 18.0, 17.5),
        MECCA("Умм аль-Кура (Мекка)", 18.5, 17.0) // typically Fajr 18.5, Isha is sunset + 90 min, simplified here as 17.0
    }

    enum class AsrJuristic(val label: String, val shadowFactor: Double) {
        STANDARD("Стандарт (Шафии, Малики, Ханбали)", 1.0),
        HANAFI("Ханафи", 2.0)
    }

    // Helper math functions in degrees
    private fun dSin(d: Double): Double = sin(toRadians(d))
    private fun dCos(d: Double): Double = cos(toRadians(d))
    private fun dTan(d: Double): Double = tan(toRadians(d))
    private fun dAsin(x: Double): Double = toDegrees(asin(x))
    private fun dAcos(x: Double): Double = toDegrees(acos(x))
    private fun dAtan(x: Double): Double = toDegrees(atan(x))
    private fun dAtan2(y: Double, x: Double): Double = toDegrees(atan2(y, x))

    private fun fixAngle(a: Double): Double {
        var angle = a
        while (angle < 0) angle += 360.0
        while (angle >= 360.0) angle -= 360.0
        return angle
    }

    private fun fixHour(h: Double): Double {
        var hour = h
        while (hour < 0) hour += 24.0
        while (hour >= 24.0) hour -= 24.0
        return hour
    }

    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    fun calculateTimes(
        latitude: Double,
        longitude: Double,
        timezone: Double,
        year: Int,
        month: Int,
        day: Int,
        method: Method = Method.RUSSIA_DUM,
        asrJuristic: AsrJuristic = AsrJuristic.STANDARD
    ): PrayerTimes {
        // 1. Convert month and year for Julian Date calculation
        val m = if (month <= 2) month + 12 else month
        val y = if (month <= 2) year - 1 else year
        val A = floor(y / 100.0)
        val B = 2 - A + floor(A / 4.0)
        val JD = floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + B - 1524.5

        val d = JD - 2451545.0 // Julian days since Jan 1, 2000

        // 2. Solar Anomalies & coordinates
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val L = fixAngle(q + 1.915 * dSin(g) + 0.02 * dSin(2.0 * g))

        val e = 23.439 - 0.00000036 * d
        val declination = dAsin(dSin(e) * dSin(L))
        val RA = fixAngle(dAtan2(dCos(e) * dSin(L), dCos(L))) / 15.0

        // Equation of time in hours
        val EqT = q / 15.0 - RA

        // 3. Solar Noon (Dhuhr)
        val noon = fixHour(12.0 + timezone - longitude / 15.0 - EqT)

        // Hour angle helper for a given solar altitude (angle)
        fun hourAngle(angle: Double, dec: Double, lat: Double): Double {
            val cosH = (dSin(angle) - dSin(dec) * dSin(lat)) / (dCos(dec) * dCos(lat))
            if (cosH < -1.0 || cosH > 1.0) return Double.NaN
            return dAcos(cosH) / 15.0
        }

        // 4. Calculate pray bounds
        val fajrHA = hourAngle(-method.fajrAngle, declination, latitude)
        val fajrTime = if (fajrHA.isNaN()) noon - 1.5 else fixHour(noon - fajrHA)

        val riseHA = hourAngle(-0.833, declination, latitude)
        val sunriseTime = if (riseHA.isNaN()) noon - 1.0 else fixHour(noon - riseHA)

        val asrAlt = dAtan(1.0 / (asrJuristic.shadowFactor + dTan(abs(latitude - declination))))
        val asrHA = hourAngle(asrAlt, declination, latitude)
        val asrTime = if (asrHA.isNaN()) noon + 1.5 else fixHour(noon + asrHA)

        val setHA = hourAngle(-0.833, declination, latitude)
        val maghribTime = if (setHA.isNaN()) noon + 1.0 else fixHour(noon + setHA)

        val ishaHA = hourAngle(-method.ishaAngle, declination, latitude)
        val ishaTime = if (ishaHA.isNaN()) noon + 1.5 else fixHour(noon + ishaHA)

        fun formatTime(t: Double): String {
            val totalMinutes = (t * 60.0 + 0.5).toInt()
            val hrs = (totalMinutes / 60) % 24
            val mins = totalMinutes % 60
            return String.format("%02d:%02d", hrs, mins)
        }

        return PrayerTimes(
            fajr = formatTime(fajrTime),
            sunrise = formatTime(sunriseTime),
            dhuhr = formatTime(noon),
            asr = formatTime(asrTime),
            maghrib = formatTime(maghribTime),
            isha = formatTime(ishaTime)
        )
    }
}
