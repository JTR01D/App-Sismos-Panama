# 🇵🇦 Sismos Panamá - Monitor Sismológico y Tectónico en Vivo

[![Build Android APK](https://github.com/JTR01D/App-Sismos-Panama/actions/workflows/build-apk.yml/badge.svg)](https://github.com/JTR01D/App-Sismos-Panama/actions/workflows/build-apk.yml)
[![Latest Release](https://img.shields.io/github/v/release/JTR01D/App-Sismos-Panama?label=Última%20Versión&color=3186FF)](https://github.com/JTR01D/App-Sismos-Panama/releases/latest)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026--35%29-3DDC84?logo=android&logoColor=white)](https://android.com)

Aplicación nativa para Android diseñada para el monitoreo, alerta temprana y divulgación geocientífica de eventos sísmicos **en tiempo real y con cobertura exclusiva para la República de Panamá**, integrando los datos oficiales de redes sísmicas de referencia (USGS y EMSC) y la cartografía estructural de fallas geológicas del Instituto de Geociencias (IGC-UP).

---

## 🚀 Descarga Directa e Instalación

No requieres tener Android Studio ni compilar el código fuente. Cada actualización del repositorio genera y publica automáticamente el instalador APK en GitHub Releases:

### 📥 Descarga del APK Oficial (Público y Directo)
👉 **[Descargar SismosPanama.apk (Última Versión)](https://github.com/JTR01D/App-Sismos-Panama/releases/download/latest/SismosPanama.apk)**

* Consulta las notas de actualización y el historial de versiones en la sección de **[Releases](../../releases/latest)**.
* Los instaladores son compilados en limpio por GitHub Actions y no requieren cuenta de GitHub para descargarse.

---

## ✨ Características Principales

### ⚡ Monitoreo Sísmico Exclusivo para Panamá
* **Filtro Satelital Estricto**: Conexión directa a los feeds GeoJSON oficiales mundiales delimitados por el *Bounding Box* del territorio y aguas panameñas:
  * Latitud: `[6.5° N - 10.3° N]`
  * Longitud: `[-83.5° O - -77.0° O]`
* **Geolocalización Inmediata**: Cálculo instantáneo de la distancia en kilómetros a las principales ciudades (David, Santiago, Chitré, Las Tablas, Penonomé, Colón, Panamá Metro, Bocas del Toro, Darién, etc.).
* **Clasificación Geodinámica**: Diferenciación por profundidad (Superficial $< 25$ km, Intermedia o Profunda).

### 🔔 Alertas y Notificaciones 24/7 de Alta Disponibilidad
* **Monitoreo Ininterrumpido en Segundo Plano**: Implementado mediante un motor dual híbrido (`AlarmManager` exacto + `WorkManager`) con exención de optimización de batería (`REQUEST_IGNORE_BATTERY_OPTIMIZATIONS`) para evitar que el sistema operativo congele las alarmas.
* **Control Total de Notificaciones**: Modal de configuración con libertad absoluta de selección:
  * 🔊 **Solo Sonido**: Tonos de alarma sísmica sin vibración.
  * 📳 **Solo Vibración**: Alertas táctiles discretas.
  * 💬 **Solo Mensajes**: Notificaciones visuales sin sonido ni vibración.
  * ⚡ **Completo**: Sonido + vibración + notificación visual.
  * 🔕 **Desactivar Todas**: Opción de silenciar completamente el sistema de alertas.
* **Botón de Prueba Integrado**: Permite probar instantáneamente el sonido y la vibración en el dispositivo desde la pantalla de ajustes.

### 🌋 Catálogo Tectónico Estructural e Informativo
* **Enfoque Puramente Científico-Educativo**: Diseñado para ciudadanos y aficionados sin jerga confusa. No lista eventos históricos pasados, sino la arquitectura geológica de las fallas vivas de Panamá.
* **Fichas Técnicas Estructuradas**:
  * **Mecanismo Tectónico**: Rumbo dextral/sinestral (*strike-slip*), cabalgamiento inverso o zonas de subducción.
  * **Tasa de Desplazamiento (*Slip Rate*)**: Velocidad de acumulación elástica en mm/año.
  * **Periodo de Recurrencia Geológico**: Intervalos estimados de acumulación de esfuerzo cortical.
  * **Coordenadas Representativas**: Georreferenciación en latitud y longitud.
* **Filtros por Regiones Geotectónicas**: Selector rápido entre *Pacífico / Chiriquí*, *Azuero / Veraguas*, *Canal / Metro*, *Caribe Norte* y *Darién / Este*.
* **Glosario Interactivo Plegable**: Explicación didáctica de conceptos geológicos clave.

### 📊 Dashboard Estadístico & Detector de Enjambres Sísmicos
* **Métricas en Tiempo Real**: Distribución por rangos de magnitud ($< 3.0$, $3.0 - 4.4$, $4.5 - 5.9$, $\ge 6.0$).
* **Análisis por Región Sísmica**: Gráficos porcentuales de concentración tectónica nacional (Golfo de Chiriquí, Azuero Sur, Bocas del Toro, Cuenca del Canal y Darién).
* **Detector Inteligente de Enjambres Sísmicos**: Identifica agrupamientos anómalos de sismos en una misma falla o región y determina cronológicamente cuál es el **último evento registrado** perteneciente a dicho enjambre.

### 🗺️ Mapa Sismotectónico Interactivo
* **Motor Cartográfico Nativo con Leaflet**: Mapa interactivo táctil fluido sin dependencias de Google Play Services.
* **Capas Tectónicas y Fallas Geológicas**: Representación espacial de la Zona de Fractura de Panamá (ZFP), Falla de Pedro Miguel, Sistema Soná-Tonosí, Cinturón Deformado del Norte y Zona de Fractura de Coiba.
* **Ondas Expansivas Animadas**: Visualización dinámica de ondas sísmicas proporcionales a la energía y magnitud del evento.

### 💎 Diseño de Interfaz Liquid Glass
* Estética visual moderna basada en superficies oscuras (*Obsidian Slate*), desenfoque de cristal líquido (*Glassmorphism*), acentos en azul eléctrico y tipografía de alto contraste sismológico.

---

## 🛠️ Arquitectura del Proyecto

```text
app/src/main/
├── AndroidManifest.xml                  // Permisos, receivers y servicio de arranque
├── assets/
│   ├── leaflet/                         // Librería Leaflet offline embebida
│   └── map.html                         // Lienzo interactivo del mapa sismológico
├── java/com/sismos/panama/
│   ├── MainActivity.kt                  // Actividad principal y navegación Compose
│   ├── SismosPanamaApp.kt               // Inicialización de canales de notificación
│   ├── data/
│   │   ├── api/
│   │   │   ├── SeismicApiService.kt     // Cliente Retrofit con Bounding Box de Panamá
│   │   │   └── RetrofitClient.kt        // OkHttp con timeouts y manejo de red
│   │   ├── background/
│   │   │   ├── SeismicAlarmReceiver.kt  // Despachador de alarmas y notificaciones exactas
│   │   │   ├── SeismicBootReceiver.kt   // Auto-reinicio de monitoreo al encender el teléfono
│   │   │   └── SeismicMonitorWorker.kt  // Tarea de respaldo periódica WorkManager
│   │   ├── models/
│   │   │   ├── AlertSettings.kt         // Preferencias guardadas de sonido/vibración
│   │   │   ├── Earthquake.kt            // Modelo de dominio y cálculos de distancia
│   │   │   ├── FaultLine.kt             // Catálogo estructurado de fallas tectónicas
│   │   │   ├── PanamaRegion.kt          // Polígonos y regiones sismotectónicas
│   │   │   └── UsgsDto.kt               // Deserializadores GeoJSON FDSN
│   │   └── repository/
│   │       └── EarthquakeRepository.kt  // Caché en memoria y filtrado geográfico
│   └── ui/
│       ├── components/
│       │   ├── EarthquakeCard.kt        // Tarjeta interactiva con indicadores de riesgo
│       │   ├── GlassCard.kt             // Contenedor visual translúcido con efecto cristal
│       │   ├── MagnitudeBadge.kt        // Distintivo cromático por magnitud Richter
│       │   ├── NotificationSettingsModal.kt // Modal completo de configuración de alertas
│       │   └── PanamaRegionsBanner.kt   // Chips de filtrado rápido regional
│       ├── screens/
│       │   ├── HomeScreen.kt            // Feed sísmico principal en vivo
│       │   ├── DetailScreen.kt          // Análisis sismotectónico detallado del sismo
│       │   ├── FaultsScreen.kt          // Catálogo técnico de fallas y glosario
│       │   ├── DashboardScreen.kt       // Estadísticas, gráficos y enjambres
│       │   ├── InteractiveMapScreen.kt  // Mapa sismotectónico con Leaflet
│       │   └── AboutScreen.kt           // Acerca de la app, autoría y stack
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Theme.kt
│       │   └── Type.kt
│       └── viewmodel/
│           └── SeismicViewModel.kt      // Estado reactivo y auto-recarga
└── res/
    ├── drawable/
    ├── mipmap-anydpi-v26/
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── themes.xml
```

---

## 📋 Especificaciones Técnicas

| Atributo | Especificación |
| :--- | :--- |
| **Lenguaje** | Kotlin 2.0.21 (JVM Target 17) |
| **Min SDK** | Android 8.0 Oreo (API 26) |
| **Target / Compile SDK** | Android 15 (API 35) |
| **Framework de UI** | Jetpack Compose + Material Design 3 |
| **Arquitectura** | MVVM (Model-View-ViewModel) + StateFlow Reactivo |
| **Red y API** | Retrofit 2 + Gson + OkHttp |
| **Monitoreo en Segundo Plano** | AlarmManager + WorkManager + BroadcastReceiver |
| **Motor Cartográfico** | Leaflet.js embebido en WebView nativo (Offline & Local Assets) |
| **Permisos de Sistema** | `INTERNET`, `POST_NOTIFICATIONS`, `VIBRATE`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` |
| **Seguridad** | 0 Backdoors, sin receptores de depuración exportados en producción |

---

## 💻 Compilación Local en Android Studio

Si deseas compilar la aplicación en tu propio entorno:

1. **Clonar el Repositorio**:
   ```bash
   git clone https://github.com/JTR01D/App-Sismos-Panama.git
   ```
2. **Abrir en Android Studio**:
   * Abre **Android Studio** (Koala / Ladybug o superior con JDK 17).
   * Selecciona **File > Open...** y elige la carpeta raíz del proyecto.
3. **Compilar y Ejecutar**:
   * Conecta tu dispositivo Android con la **Depuración por USB** activada.
   * Presiona **Run** (`Shift + F10`) o compila el APK mediante terminal:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 👨‍💻 Autor y Desarrollador

Desarrollado de forma individual por:

* **JTR01D** ([GitHub: @JTR01D](https://github.com/JTR01D))
* **Repositorio Oficial**: [https://github.com/JTR01D/App-Sismos-Panama](https://github.com/JTR01D/App-Sismos-Panama)

---

## 📄 Licencia

Este proyecto se distribuye bajo los términos de la **Licencia MIT**. Puedes consultar el archivo de licencia para más detalles.
