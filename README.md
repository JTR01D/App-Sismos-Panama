# 🇵🇦 Sismos Panamá - Aplicación Nativa para Android (Kotlin + Jetpack Compose)

Aplicación nativa para Android diseñada para monitorear y visualizar sismos **en tiempo real de manera exclusiva para la República de Panamá**, integrando los datos geodinámicos y fallas tectónicas de los estudios oficiales y redes sismológicas de referencia.

---

## 📱 Características Principales

* ⚡ **Monitoreo en Tiempo Real**: Conexión directa a los feeds satelitales FDSN / GeoJSON (USGS / EMSC) configurados con un *Bounding Box* geográfico estricto para Panamá:
  * Latitud: `[6.5° N - 10.3° N]`
  * Longitud: `[-83.5° O - -77.0° O]`
* 🇵🇦 **Filtro Exclusivo Panamá**: Descarta automáticamente sismos de otros países que no tengan incidencia directa en territorio o aguas territoriales panameñas.
* 📍 **Geolocalización Inmediata**:
  * Identificación de la **ciudad panameña más cercana** y distancia estimada en kilómetros (David, Santiago, Chitré, Las Tablas, Tonosí, Penonomé, Colón, Panamá Metro, Changuinola, Puerto Armuelles, Darién).
  * Clasificación por profundidad (Superficial $< 25$ km vs Intermedia/Profunda).
* 🌋 **Identificación de Fallas Tectónicas Activas**:
  * Vinculación automática con las fallas documentadas en los reportes técnicos del **Instituto de Geociencias (IGC-UP)**:
    1. **Zona de Fractura de Panamá (ZFP)** y Sistema Burica (Chiriquí).
    2. **Sistema Soná-Tonosí** y Cinturón Deformado del Sur (Azuero y Veraguas).
    3. **Falla de Pedro Miguel**, **Miraflores** y **Limón** (Cuenca del Canal y Panamá Metro).
    4. **Cinturón Deformado del Norte (CDNP)** (Bocas del Toro y Costa Caribe).
    5. **Fallas de Sansón y Chucunaque** (Darién).
* 📊 **Zonas de Concentración Sísmica**: Selector rápido de las 5 regiones sismotectónicas analizadas en el informe geológico:
  * Golfo de Chiriquí / Burica (68% de sismicidad nacional)
  * Azuero Sur / Coiba (18%)
  * Bocas del Toro / Caribe Occ. (7%)
  * Cuenca del Canal / Panamá Metro (4%)
  * Darién / Golfo de San Miguel (3%)
* 🔔 **Alertas en Segundo Plano (WorkManager)**: Tarea programada que evalúa eventos cada 15 minutos e interactúa con el sistema de notificaciones de Android ante sismos $M \ge 3.5$ o alertas preventivas.
* 🎨 **Diseño Moderno Material 3**: Tema oscuro de alto contraste sismológico (Obsidian Slate + Electric Blue + distintivos por magnitud en verde, amarillo, naranja y rojo).

---

## 🛠️ Estructura del Proyecto

```text
app/src/main/
├── AndroidManifest.xml
├── java/com/sismos/panama/
│   ├── MainActivity.kt                  // Navegación Compose y permisos de notificación
│   ├── SismosPanamaApp.kt               // Inicializador del canal y WorkManager
│   ├── data/
│   │   ├── api/
│   │   │   ├── SeismicApiService.kt     // Interfaz Retrofit con bounding box de Panamá
│   │   │   └── RetrofitClient.kt        // Cliente HTTP con OkHttp y timeouts
│   │   ├── background/
│   │   │   └── SeismicMonitorWorker.kt  // Notificaciones y monitoreo en segundo plano
│   │   ├── models/
│   │   │   ├── Earthquake.kt            // Modelo de dominio y niveles de alerta
│   │   │   ├── FaultLine.kt             // Catálogo de fallas activas de Panamá
│   │   │   ├── PanamaRegion.kt          // Catálogo de las 5 zonas del reporte
│   │   │   └── UsgsDto.kt               // DTOs para deserializar GeoJSON
│   │   └── repository/
│   │       └── EarthquakeRepository.kt  // Filtrado Panamá, cálculo de distancia y fallas
│   └── ui/
│       ├── components/
│       │   ├── EarthquakeCard.kt        // Tarjeta interactiva del sismo
│       │   ├── MagnitudeBadge.kt        // Badge de magnitud con código de color
│       │   └── PanamaRegionsBanner.kt   // Barra horizontal con las 5 zonas
│       ├── screens/
│       │   ├── HomeScreen.kt            // Pantalla principal con feed y filtros
│       │   ├── DetailScreen.kt          // Detalle físico, geodinámico y enlace oficial
│       │   └── FaultsScreen.kt          // Catálogo educativo de fallas de Panamá
│       ├── theme/
│       │   ├── Color.kt
│       │   ├── Theme.kt
│       │   └── Type.kt
│       └── viewmodel/
│           └── SeismicViewModel.kt      // Estado reactivo y auto-refresco
└── res/
    ├── drawable/
    ├── mipmap-anydpi-v26/
    └── values/
        ├── colors.xml
        ├── strings.xml
        └── themes.xml
```

---

## 🚀 Cómo abrir y compilar en Android Studio

1. **Abrir el Proyecto**:
   * Inicia **Android Studio**.
   * Selecciona **File > Open...** (o *Open* en la pantalla de bienvenida).
   * Navega hasta la carpeta del proyecto:
   * Abre la carpeta raiz del proyecto.
   * Haz clic en **OK**.

2. **Sincronizar Gradle**:
   * Android Studio descargará automáticamente las dependencias (Jetpack Compose, Retrofit, WorkManager, Material 3).
   * Cuando termine la sincronización, verás el mensaje `"Gradle sync finished"`.

3. **Ejecutar en tu Teléfono o Emulador**:
   * Conecta tu celular Android por cable USB y activa la **Depuración por USB** (USB Debugging) en *Opciones de Desarrollador*.
   * En la barra superior de Android Studio, selecciona tu dispositivo en el menú desplegable.
   * Presiona el botón verde **Run** (o presiona `Shift + F10`).

4. **Generar el archivo APK para instalar directamente en cualquier celular**:
   * En el menú superior de Android Studio ve a:
     `Build > Build Bundle(s) / APK(s) > Build APK(s)`.
   * En pocos segundos Android Studio compilará el instalador y mostrará una notificación emergente con el enlace directo al archivo:
     `app/build/outputs/apk/debug/app-debug.apk`.
   * Pasa ese archivo `.apk` a tu celular (por WhatsApp, Google Drive o cable USB) y tócalo para instalarlo.
