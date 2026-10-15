package com.sismos.panama.ui.theme

import androidx.compose.ui.graphics.Color

val BgDark = Color(0xFF0D121B)
val SurfaceDark = Color(0xFF141C2B)
val SurfaceDarkCard = Color(0xFF1A2436)
val PrimaryBlue = Color(0xFF3186FF)
val PrimaryContainer = Color(0xFF1E3A8A)
val OnSurface = Color(0xFFF1F5F9)
val OnSurfaceVariant = Color(0xFF94A3B8)
val StrokeDark = Color(0xFF2E3D56)

// Colores sismológicos oficiales por potencial destructivo
val HazardInstrumental = Color(0xFF00E676) // M < 3.0: Instrumental / Imperceptible (Verde Esmeralda)
val HazardLight = Color(0xFF00E5FF)        // 3.0 <= M < 4.0: Leve / Sentido débilmente (Cian Eléctrico)
val HazardModerate = Color(0xFFFFD600)     // 4.0 <= M < 5.0: Moderado / Sentido ampliamente (Ámbar Dorado)
val HazardStrong = Color(0xFFFF6D00)       // 5.0 <= M < 6.0: Fuerte / Daño leve-moderado (Naranja Fuego)
val HazardDestructive = Color(0xFFFF1744)  // M >= 6.0: Severo - Destructivo / Alerta General (Rojo Carmesí)

val MagGreen = HazardInstrumental
val MagBlue = HazardLight
val MagYellow = HazardModerate
val MagOrange = HazardStrong
val MagRed = HazardDestructive
val TsunamiWarningColor = Color(0xFFDC2626)
