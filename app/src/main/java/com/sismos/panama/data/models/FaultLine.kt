package com.sismos.panama.data.models

/**
 * Representa una falla tectónica activa en Panamá con su régimen y antecedentes
 */
data class FaultLine(
    val id: String,
    val name: String,
    val region: String,
    val mechanism: String,
    val slipRate: String,
    val recurrence: String,
    val description: String,
    val representativeLat: Double,
    val representativeLon: Double,
    val historicalEvents: String
)

object PanamaFaultsCatalog {
    val faults = listOf(
        FaultLine(
            id = "zfp",
            name = "Zona de Fractura de Panamá (ZFP)",
            region = "Golfo de Chiriquí / Burica (Pacífico)",
            mechanism = "Rumbo dextral (transformante oceánica)",
            slipRate = ">40 mm/año",
            recurrence = "5 a 15 años (M≥6.0) / 25 a 40 años (M≥7.0)",
            description = "Límite transformante entre placas Cocos y Nazca. Es el motor principal que concentra más del 65% de los sismos registrados en el país.",
            representativeLat = 8.0,
            representativeLon = -82.8,
            historicalEvents = "1934 (Mw 7.4), 1979 (Mw 6.6), 2002 (Mw 6.4), 2021 (Mw 6.8)"
        ),
        FaultLine(
            id = "burica",
            name = "Sistema de Fallas de Burica / Puerto Armuelles",
            region = "Península de Burica (Chiriquí)",
            mechanism = "Transpresiva y rumbo cortical",
            slipRate = "~20-30 mm/año",
            recurrence = "Constante (enjambres frecuentes)",
            description = "Transfiere los esfuerzos de cizalla de la ZFP hacia la masa continental, provocando continuos microsismos y levantamientos.",
            representativeLat = 8.2,
            representativeLon = -82.9,
            historicalEvents = "Enjambres sísmicos continuos en Puerto Armuelles y Melliza"
        ),
        FaultLine(
            id = "sona_tonosi",
            name = "Sistema de Fallas Soná-Tonosí",
            region = "Veraguas Sur / Península de Azuero",
            mechanism = "Rumbo sinestral / transpresivo cortical",
            slipRate = "1.0 - 2.5 mm/año",
            recurrence = "70 a 150 años (M > 6.5)",
            description = "Cruza diagonalmente desde el Golfo de Montijo hasta Tonosí. Es una de las estructuras corticales más extensas y sismogénicas del Pacífico central.",
            representativeLat = 7.4,
            representativeLon = -80.9,
            historicalEvents = "1913 (Ms 6.9 Tonosí), 2026 (Golfo de Montijo/Azuero Mw 7.2-7.5)"
        ),
        FaultLine(
            id = "pedro_miguel",
            name = "Falla de Pedro Miguel",
            region = "Cuenca del Canal / Panamá Metro",
            mechanism = "Rumbo sinestral cortical (lateral izquierdo)",
            slipRate = "1.6 - 2.1 mm/año",
            recurrence = "300 a 500 años (ruptura superficial)",
            description = "Cruza directamente las obras del Canal de Panamá (Corte Culebra). Considerada de alta importancia por cercanía a la infraestructura metropolitana.",
            representativeLat = 9.03,
            representativeLon = -79.62,
            historicalEvents = "2 de mayo de 1621 (Destrucción de Panamá Viejo, Mw 6.8 - 7.0)"
        ),
        FaultLine(
            id = "limon",
            name = "Falla de Limón",
            region = "Colón / Cuenca Norte del Canal",
            mechanism = "Rumbo con componente transtensivo",
            slipRate = "~1.0 mm/año",
            recurrence = "400 a 700 años",
            description = "Se extiende desde el Lago Gatún hacia la Bahía de Manzanillo en Colón, conectando con el Cinturón Deformado del Norte.",
            representativeLat = 9.25,
            representativeLon = -79.85,
            historicalEvents = "Actividad cuaternaria confirmada por trincheras paleosísmicas"
        ),
        FaultLine(
            id = "miraflores",
            name = "Falla de Miraflores",
            region = "Boca Pacífica del Canal de Panamá",
            mechanism = "Rumbo con componente normal",
            slipRate = "~0.2 - 0.5 mm/año",
            recurrence = "800 a 1.500 años",
            description = "Paralela a Pedro Miguel, discurre en proximidades a las esclusas de Miraflores y la boca del Pacífico.",
            representativeLat = 8.98,
            representativeLon = -79.59,
            historicalEvents = "Falla de baja tasa pero monitoreada continuamente por el Canal"
        ),
        FaultLine(
            id = "cdnp",
            name = "Cinturón Deformado del Norte de Panamá (CDNP)",
            region = "Litoral Caribe (Bocas del Toro hasta San Blas)",
            mechanism = "Inverso (cabalgamiento y sobrecorrimiento)",
            slipRate = "6 a 12 mm/año",
            recurrence = "200 a 450 años (grandes rupturas)",
            description = "Estructura submarina de cabalgamiento en el margen Caribe donde converge el istmo con la Placa del Caribe. Origen de grandes tsunamis históricos.",
            representativeLat = 9.5,
            representativeLon = -81.5,
            historicalEvents = "7 de septiembre de 1882 (San Blas, Mw ~7.9), 22 de abril de 1991 (Bocas del Toro, Mw 7.7)"
        ),
        FaultLine(
            id = "darien_sanson",
            name = "Falla de Sansón y Chucunaque",
            region = "Darién / Panamá Este",
            mechanism = "Cizalla intraplaca / desgarre cortical",
            slipRate = "1.5 - 3.0 mm/año",
            recurrence = "150 a 350 años (M≥7.0)",
            description = "Acomoda la compresión continua por el empuje del Bloque Chocó y la Placa Sudamericana hacia el noroeste del istmo.",
            representativeLat = 8.3,
            representativeLon = -78.2,
            historicalEvents = "1883 (Mw ~7.3), 1976 (Golfo de San Miguel, Mw 7.0)"
        )
    )
}
