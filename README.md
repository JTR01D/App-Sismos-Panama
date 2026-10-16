# 🇵🇦 Sismos Panamá - Monitor Sismológico y Tectónico en Vivo

[![Build Android APK](https://github.com/JTR01D/App-Sismos-Panama/actions/workflows/build-apk.yml/badge.svg)](https://github.com/JTR01D/App-Sismos-Panama/actions/workflows/build-apk.yml)
[![Latest Release](https://img.shields.io/github/v/release/JTR01D/App-Sismos-Panama?label=Última%20Versión&color=3186FF)](https://github.com/JTR01D/App-Sismos-Panama/releases/latest)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B%20%28API%2026--35%29-3DDC84?logo=android&logoColor=white)](https://android.com)

Aplicación nativa para Android diseñada para el monitoreo, alerta temprana y divulgación geocientífica de eventos sísmicos **en tiempo real y con cobertura exclusiva para la República de Panamá**, integrando los datos oficiales del **Instituto de Geociencias de la Universidad de Panamá (IGC-UP)**, el feed satelital de la **Red Sísmica Internacional (USGS)** y la cartografía geológica de fallas tectónicas del istmo.

---

## 🚀 Descarga Directa e Instalación

No requieres tener Android Studio ni compilar el código fuente. Cada actualización del repositorio genera y publica automáticamente el instalador APK en GitHub Releases:

### 📥 Descarga del APK Oficial (Público y Directo)
👉 **[Descargar SismosPanama.apk (Última Versión)](https://github.com/JTR01D/App-Sismos-Panama/releases/download/latest/SismosPanama.apk)**

* Consulta las notas de actualización y el historial de versiones en la sección de **[Releases](../../releases/latest)**.
* Los instaladores son compilados en limpio por GitHub Actions y no requieren cuenta de GitHub para descargarse.

---

## ✨ Características Principales

### ⚡ Monitoreo Sísmico Dual Exclusivo para Panamá
* **Fuentes de Datos Oficiales Integradas**: Conexión primaria directa al portal del **Instituto de Geociencias (IGC-UP)** con fallback satelital automático a la red **USGS** filtrado por el *Bounding Box* del territorio y aguas panameñas:
  * Latitud: `[6.5° N - 10.3° N]`
  * Longitud: `[-83.5° O - -77.0° O]`
* **Geolocalización Inmediata**: Cálculo instantáneo de la distancia en kilómetros a las principales ciudades (David, Puerto Armuelles, Santiago, Chitré, Las Tablas, Penonomé, Colón, Ciudad de Panamá, Bocas del Toro, Darién, etc.).
* **Clasificación Geodinámica**: Diferenciación por profundidad (Superficial $< 25$ km, Intermedia o Profunda).

### 🔔 Sistema de Alertas Inteligente & Coordinador Anti-Duplicados
* **Coordinador Centralizado (`SeismicAlertCoordinator`)**:
  * **Deduplicación Cruzada Espacio-Temporal (IGC-UP ↔ USGS)**: Evita alertas repetidas cuando la red alterna entre ambas fuentes. Reconoce el mismo evento físico si coincide dentro de 80 km, 20 minutos y $\pm 1.2$ de magnitud.
  * **Filtro de Frescura de Emergencia (Máximo 20 minutos)**: Si el teléfono pierde conexión a Internet y la recupera minutos o horas después, **no se disparan sirenas ni alarmas de eventos pasados**.
  * **Historial Persistente Acotado**: Memoria de los últimos 50 identificadores y 20 firmas sísmicas en `SharedPreferences` para evitar alertas redundantes.
  * **Protección de Inicio en Frío**: Sin falsas alarmas retroactivas al instalar o abrir la app por primera vez.
* **Control Continuo del Rango de Magnitud**:
  * **Barra Deslizante (`Slider` Material 3)**: Deslizador continuo de `2.0` a `6.5+` con saltos de `0.1` para definir libremente el umbral mínimo de alertas sonoras y notificaciones.
  * **Insignia Dinámica de Severidad**: Cambio de color y descripción didáctica en vivo según el umbral (🟢 Sin filtro, 🔵 Leves, 🟡 Moderados, 🟠 Fuertes, 🔴 Severos).
  * **Chips de Acceso Rápido**: Atajos de 1 toque (`Todos`, `M ≥ 3.0`, `M ≥ 4.0`, `M ≥ 5.0`, `M ≥ 6.0`).
* **Modalidad de Alertas 24/7**:
  * 🔊 **Solo Sonido**: Tonos de alarma sísmica sin vibración.
  * 📳 **Solo Vibración**: Alertas táctiles discretas.
  * 💬 **Solo Mensajes**: Notificaciones visuales sin sonido ni vibración.
  * ⚡ **Completo**: Sonido + vibración + notificación visual.
  * 🔕 **Desactivar Todas**: Opción de silenciar completamente el sistema de alertas.
* **Botón de Prueba Integrado**: Permite probar instantáneamente el sonido y la vibración en el dispositivo desde la pantalla de ajustes.

### 🌊 Simulador de Ondas Sísmicas con Atenuación Realista
* **Propagación Dinámica de Ondas P y S**: Simulación de ondas longitudinales primarias y de cizalla secundarias en tiempo real.
* **Atenuación Física por Distancia**: Las ondas se van desvaneciendo y reduciendo en intensidad a medida que recorren el territorio nacional alejándose del epicentro.
* **Escalado Proporcional por Magnitud**: La amplitud, velocidad aparente y coloración de severidad varían matemáticamente de acuerdo a la energía liberada.
* **Modo Enfoque Cinemático**: Ocultamiento automático de paneles innecesarios durante la simulación para brindar una vista despejada y envolvente tanto en teléfonos como en tablets.

### 🗺️ Explorador Tectónico & Mapa Interactivo
* **Motor Cartográfico Nativo con Leaflet**: Mapa táctil interactivo de alto rendimiento sin requerir servicios privativos de Google Play.
* **Capas Tectónicas y Fallas Geológicas de Panamá**: Representación cartográfica espacial de la Zona de Fractura de Panamá (ZFP), Falla de Pedro Miguel (Canal), Sistema Soná-Tonosí, Cinturón Deformado del Norte de Panamá (CDNP) y Falla de Burica.
* **Selector y Deselección Rápida de Eventos**: Botón de cierre directo para examinar múltiples sismos consecutivamente sin bloqueos de interfaz.

### 📊 Dashboard Estadístico & Detector de Enjambres Sísmicos
* **Métricas en Tiempo Real**: Distribución porcentual por rangos de magnitud ($< 3.0$, $3.0 - 4.4$, $4.5 - 5.9$, $\ge 6.0$).
* **Análisis por Región Sísmica**: Gráficos de concentración tectónica nacional (Golfo de Chiriquí, Azuero Sur, Bocas del Toro, Cuenca del Canal y Darién).
* **Detector Inteligente de Enjambres Sísmicos**: Identifica agrupamientos anómalos de sismos en una misma falla o región y determina cronológicamente cuál es el **último evento registrado** perteneciente a dicho enjambre.

### 🌋 Catálogo Geológico Estructural de Fallas
* **Enfoque Científico y Educativo**: Arquitectura geológica de las fallas vivas de Panamá con fichas técnicas explicadas de manera comprensible:
  * **Mecanismo Tectónico**: Rumbo dextral/sinestral (*strike-slip*), cabalgamiento inverso o zonas de subducción.
  * **Tasa de Desplazamiento (*Slip Rate*)**: Velocidad de acumulación elástica en mm/año.
  * **Periodo de Recurrencia Geológico**: Intervalos estimados de acumulación de esfuerzo cortical.
  * **Coordenadas Representativas**: Georreferenciación en latitud y longitud.
* **Filtros por Regiones Geotectónicas**: Selector rápido entre *Pacífico / Chiriquí*, *Azuero / Veraguas*, *Canal / Metro*, *Caribe Norte* y *Darién / Este*.
* **Glosario Geológico Interactivo**: Explicaciones didácticas de magnitudes, hipocentros, mecanismos de falla y tsunamis.

### 💎 Diseño de Interfaz Liquid Glass & Simetría Multi-Pantalla
* **Barra de Navegación Flotante (`PanamaBottomNavigationBar`)**: Navegación fluida de 4 secciones con efecto translúcido de cristal esmerilado (*glassmorphism*), bordes curvos y luces indicadoras de sección activa.
* **Optimización Multi-Dispositivo**: Interfaz simétrica y adaptada tanto para teléfonos (ej. Xiaomi Redmi Note 11, pantalla 20:9) como tablets en horizontal y vertical (ej. Lenovo Tab P12, pantalla 16:10 / 2K).
* **Estética Obsidian Slate**: Modo oscuro integral de alto contraste con acentos cromáticos de severidad sísmica internacional.

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
│   ├── SismosPanamaApp.kt               // Inicialización de canales de notificación y WorkManager
│   ├── data/
│   │   ├── api/
│   │   │   ├── SeismicApiService.kt     // Cliente Retrofit con Bounding Box de Panamá
│   │   │   └── RetrofitClient.kt        // OkHttp con timeouts y manejo de red
│   │   ├── background/
│   │   │   ├── SeismicAlertCoordinator.kt // Coordinador centralizado anti-duplicados y frescura
│   │   │   ├── SeismicAlarmReceiver.kt  // Despachador de alarmas y notificaciones exactas
│   │   │   ├── SeismicBootReceiver.kt   // Auto-reinicio de monitoreo al encender el teléfono
│   │   │   └── SeismicMonitorWorker.kt  // Tarea de respaldo periódica WorkManager
│   │   ├── models/
│   │   │   ├── AlertSettings.kt         // Preferencias guardadas de sonido, vibración y umbral
│   │   │   ├── Earthquake.kt            // Modelo de dominio y cálculos geodésicos
│   │   │   ├── FaultLine.kt             // Catálogo estructurado de fallas tectónicas
│   │   │   ├── PanamaRegion.kt          // Polígonos y regiones sismotectónicas
│   │   │   └── UsgsDto.kt               // Deserializadores GeoJSON FDSN
│   │   └── repository/
│   │       └── EarthquakeRepository.kt  // Parser robusto IGC-UP, fallback USGS y distancias
│   └── ui/
│       ├── components/
│       │   ├── EarthquakeCard.kt        // Tarjeta interactiva con indicadores de riesgo
│       │   ├── GlassCard.kt             // Contenedor visual translúcido con efecto cristal
│       │   ├── MagnitudeBadge.kt        // Distintivo cromático por magnitud Richter
│       │   ├── NotificationSettingsModal.kt // Modal con Slider continuo de magnitud y chips
│       │   ├── PanamaBottomNavigationBar.kt // Barra de navegación inferior flotante Liquid Glass
│       │   └── PanamaRegionsBanner.kt   // Chips de filtrado rápido regional
│       ├── screens/
│       │   ├── HomeScreen.kt            // Feed sísmico principal en vivo
│       │   ├── DetailScreen.kt          // Análisis sismotectónico y simulación de ondas
│       │   ├── FaultsScreen.kt          // Catálogo técnico de fallas y glosario
│       │   ├── DashboardScreen.kt       // Estadísticas, gráficos y enjambres
│       │   ├── InteractiveMapScreen.kt  // Explorador tectónico y mapa interactivo
│       │   └── AboutScreen.kt           // Acerca de la app, autoría y stack tecnológico
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Theme.kt
│       │   └── Type.kt
│       └── viewmodel/
│           └── SeismicViewModel.kt      // Estado reactivo y auto-recarga cada 15 segundos
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
| **Red y API** | Retrofit 2 + Gson + OkHttp 4 + Web Scraping IGC-UP |
| **Monitoreo en Segundo Plano** | SeismicAlertCoordinator + AlarmManager + WorkManager + BroadcastReceiver |
| **Motor Cartográfico** | Leaflet.js embebido en WebView nativo (Assets locales offline) |
| **Permisos de Sistema** | `INTERNET`, `POST_NOTIFICATIONS`, `VIBRATE`, `SCHEDULE_EXACT_ALARM`, `RECEIVE_BOOT_COMPLETED`, `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` |
| **Seguridad** | Sin rastreadores de terceros, sin telemetría invasiva, 100% código abierto |

---

## 💻 Compilación Local en Android Studio

Si deseas compilar la aplicación en tu propio entorno:

1. **Clonar el Repositorio**:
   ```bash
   git clone https://github.com/JTR01D/App-Sismos-Panama.git
   cd App-Sismos-Panama
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
