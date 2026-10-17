import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.sismos.panama"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.sismos.panama"
        minSdk = 26
        targetSdk = 35
        versionCode = 33
        versionName = "2.5.8"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            val keystoreFile = rootProject.file("app/release.keystore")
            if (keystoreFile.exists()) {
                val localProps = Properties().apply {
                    val propFile = rootProject.file("local.properties")
                    if (propFile.exists()) {
                        FileInputStream(propFile).use { load(it) }
                    }
                }

                val envPass = System.getenv("KEYSTORE_PASSWORD")?.trim()?.takeIf { it.isNotBlank() }
                    ?: localProps.getProperty("KEYSTORE_PASSWORD")?.trim()
                    ?: "sismospanama2026"
                val envAlias = System.getenv("KEY_ALIAS")?.trim()?.takeIf { it.isNotBlank() }
                    ?: localProps.getProperty("KEY_ALIAS")?.trim()
                    ?: "sismospanama"
                val envKeyPass = System.getenv("KEY_PASSWORD")?.trim()?.takeIf { it.isNotBlank() }
                    ?: localProps.getProperty("KEY_PASSWORD")?.trim()
                    ?: envPass

                storeFile = keystoreFile
                storePassword = envPass
                keyAlias = envAlias
                keyPassword = envKeyPass
            }
        }
    }

    buildTypes {
        debug {
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
        }
        release {
            isMinifyEnabled = false
            val releaseSigning = signingConfigs.findByName("release")
            if (releaseSigning?.storeFile?.exists() == true) {
                signingConfig = releaseSigning
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.work.runtime.ktx)

    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.gson)
    implementation(libs.okhttp.logging)
    implementation(libs.gson)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.ui.tooling)
}
