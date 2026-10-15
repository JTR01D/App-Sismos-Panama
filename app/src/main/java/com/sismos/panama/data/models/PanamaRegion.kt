package com.sismos.panama.data.models

/**
 * Zonas de concentración sísmica en Panamá extraídas del análisis regional
 */
data class PanamaRegionInfo(
    val name: String,
    val shortName: String,
    val percentage: Int,
    val frequency: String,
    val mainFault: String,
    val dominantMechanism: String,
    val centroidLat: Double,
    val centroidLon: Double
)

object PanamaRegionsCatalog {
    val regions = listOf(
        PanamaRegionInfo(
            name = "Golfo de Chiriquí / Burica",
            shortName = "Chiriquí / Burica",
            percentage = 68,
            frequency = ">1200 eventos/año (M≥2.0)",
            mainFault = "Zona de Fractura de Panamá (ZFP) + Burica",
            dominantMechanism = "Rumbo dextral rápido",
            centroidLat = 8.0,
            centroidLon = -82.8
        ),
        PanamaRegionInfo(
            name = "Azuero Sur / Coiba",
            shortName = "Azuero / Coiba",
            percentage = 18,
            frequency = "~300 eventos/año (M≥2.0)",
            mainFault = "Falla Soná-Tonosí + CDSP",
            dominantMechanism = "Transpresión",
            centroidLat = 7.2,
            centroidLon = -80.8
        ),
        PanamaRegionInfo(
            name = "Bocas del Toro / Caribe Occ.",
            shortName = "Bocas del Toro",
            percentage = 7,
            frequency = "~110 eventos/año (M≥2.0)",
            mainFault = "Cinturón Deformado Norte (CDNP)",
            dominantMechanism = "Cabalgamiento inverso",
            centroidLat = 9.4,
            centroidLon = -82.5
        ),
        PanamaRegionInfo(
            name = "Cuenca del Canal / Panamá Metro",
            shortName = "Canal / Metro",
            percentage = 4,
            frequency = "~50 eventos/año (M≥2.0)",
            mainFault = "Falla Pedro Miguel + Limón",
            dominantMechanism = "Rumbo intraplaca lento",
            centroidLat = 9.1,
            centroidLon = -79.7
        ),
        PanamaRegionInfo(
            name = "Darién / Golfo de San Miguel",
            shortName = "Darién / S. Miguel",
            percentage = 3,
            frequency = "~40 eventos/año (M≥2.0)",
            mainFault = "Falla Sansón + Chucunaque",
            dominantMechanism = "Cizalla intraplaca",
            centroidLat = 8.2,
            centroidLon = -78.1
        )
    )
}
