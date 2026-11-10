import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
}

android {
    namespace = "com.example.techsmart"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.techsmart"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // URL del Asesor IA (endpoint /api/chat de la web).
        // Se cambia en local.properties con: chat.api.url=http://192.168.1.25:3000/api/chat
        // Por defecto 10.0.2.2 = la computadora anfitriona vista desde el emulador.
        val propiedadesLocales = Properties().apply {
            val archivo = rootProject.file("local.properties")
            if (archivo.exists()) archivo.inputStream().use { load(it) }
        }
        val urlChat = propiedadesLocales.getProperty("chat.api.url", "http://10.0.2.2:3000/api/chat")
        buildConfigField("String", "CHAT_API_URL", "\"$urlChat\"")
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)

    // Firebase BoM (Bill of Materials) para gestionar versiones
    implementation(platform("com.google.firebase:firebase-bom:33.9.0"))

    // Cloud Firestore para Kotlin
    implementation("com.google.firebase:firebase-firestore-ktx")

    // Corrutinas para llamadas asíncronas con Firebase
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.0")

    // Jetpack Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Iconos extendidos de Jetpack Compose
    implementation("androidx.compose.material:material-icons-extended")

    // When using the BoM, don't specify versions in Firebase dependencies
    implementation("com.google.firebase:firebase-analytics")

    // Firebase Storage para subida de adjuntos (RFQ y RMA)
    implementation("com.google.firebase:firebase-storage-ktx")

    // Firebase Auth (la versión la gestiona el BoM)
    implementation("com.google.firebase:firebase-auth-ktx")

    // ViewModel y collectAsStateWithLifecycle para Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Carga de imágenes de productos desde URL (catálogo)
    implementation("io.coil-kt:coil-compose:2.7.0")
}
