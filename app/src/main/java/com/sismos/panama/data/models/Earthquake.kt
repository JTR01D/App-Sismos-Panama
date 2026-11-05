package com.sismos.panama.data.models

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Modelo de dominio para representar un evento sísmico en Panamá
 */
data class Earthquake(
    val id: String,
    val magnitude: Double,
    val place: String,
    val timeMillis: Long,
    val latitude: Double,
    val longitude: Double,
    val depthKm: Double,
    val nearestCity: String,
    val distanceToNearestCityKm: Int,
    val nearestFault: String,
    val faultMechanism: String,
    val panamaRegion: String,
    val isTsunamiWarning: Boolean,
    val feltReports: Int,
    val officialUrl: String,
    val sourceName: String = "Red Sísmica Panamá (USGS / IGC-UP)"
) {
    val alertLevel: AlertLevel
        get() = when {
            magnitude >= 6.0 -> AlertLevel.CRITICAL
            magnitude >= 5.0 -> AlertLevel.HIGH
            magnitude >= 4.0 -> AlertLevel.MODERATE
            magnitude >= 3.0 -> AlertLevel.LIGHT
            else -> AlertLevel.LOW
        }

    fun getFormattedTime(): String {
        val panamaTz = TimeZone.getTimeZone("America/Panama")
        val localTz = TimeZone.getDefault()

        val sdfPanama = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale("es", "PA")).apply {
            timeZone = panamaTz
        }
        val panamaTime = sdfPanama.format(Date(timeMillis))

        val date = Date(timeMillis)
        val isDifferentTz = localTz.rawOffset != panamaTz.rawOffset ||
                localTz.inDaylightTime(date) != panamaTz.inDaylightTime(date)

        if (!isDifferentTz) {
            return panamaTime
        }

        val sdfLocal = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).apply {
            timeZone = localTz
        }
        val localTime = sdfLocal.format(date)
        val localTzName = localTz.getDisplayName(localTz.inDaylightTime(date), TimeZone.SHORT, Locale.getDefault())

        return "$localTime ($localTzName) • $panamaTime (PA)"
    }

    fun getFormattedPanamaTimeOnly(): String {
        val sdfPanama = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale("es", "PA")).apply {
            timeZone = TimeZone.getTimeZone("America/Panama")
        }
        return sdfPanama.format(Date(timeMillis))
    }

    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timeMillis
        if (diff < 0) return "Hace unos instantes"

        val seconds = diff / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            minutes < 1 -> "Hace unos instantes"
            minutes < 60 -> "Hace $minutes min"
            hours < 24 -> "Hace $hours ${if (hours == 1L) "hora" else "horas"}"
            else -> "Hace $days ${if (days == 1L) "día" else "días"}"
        }
    }
}

enum class AlertLevel(val label: String, val hazardDescription: String) {
    LOW("Instrumental (M < 3.0)", "Imperceptible / Sin peligro"),
    LIGHT("Leve (M 3.0 - 3.9)", "Sentido débilmente en reposo"),
    MODERATE("Moderado (M 4.0 - 4.9)", "Sentido ampliamente / Sin daño"),
    HIGH("Fuerte (M 5.0 - 5.9)", "Daño leve-moderado en estructuras"),
    CRITICAL("Severo / Destructivo (M ≥ 6.0)", "Peligro severo / Alerta general")
}
