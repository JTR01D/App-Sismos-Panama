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
        val sdf = SimpleDateFormat("dd/MM/yyyy hh:mm:ss a", Locale("es", "PA"))
        sdf.timeZone = TimeZone.getTimeZone("America/Panama")
        return sdf.format(Date(timeMillis))
    }

    fun getTimeAgo(): String {
        val now = System.currentTimeMillis()
        val diff = now - timeMillis
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
