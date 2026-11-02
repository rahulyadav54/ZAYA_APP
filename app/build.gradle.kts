import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.google.services)
    alias(libs.plugins.ksp)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(localPropertiesFile.inputStream())
    }
}

fun localProperty(key: String, defaultValue: String = ""): String =
    localProperties.getProperty(key, defaultValue)

android {
    namespace = "com.zayaai.app"
    compileSdk = 36

    signingConfigs {
        create("release") {
            storeFile = rootProject.file("aizaya-keystore.jks")
            storePassword = localProperty("KEYSTORE_PASSWORD")
            keyAlias = localProperty("KEY_ALIAS", "aizaya-key")
            keyPassword = localProperty("KEY_PASSWORD")
        }
    }

    defaultConfig {
        applicationId = "com.zayaai.app"
        minSdk = 25
        targetSdk = 36
        // Bump versionCode for Play Store uploads. Previous versions already used.
        versionCode = 15
        versionName = "2.2.5"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Permanent production Cloudflare Worker URL
        buildConfigField("String", "BACKEND_URL", "\"https://aizaya-api.zayacodehub-aizaya.workers.dev/\"")
    }

    buildTypes {
        debug {
            // Development/Debug: Production worker default
            buildConfigField("String", "BACKEND_URL", "\"${localProperty("BACKEND_URL", "https://aizaya-api.zayacodehub-aizaya.workers.dev/")}\"")
            buildConfigField("String", "OPENROUTER_API_KEY", "\"${localProperty("OPENROUTER_API_KEY")}\"")
            buildConfigField("String", "GROQ_API_KEY", "\"${localProperty("GROQ_API_KEY")}\"")
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
            // Production Release: Strictly connects to public HTTPS backend; NO API keys packaged
            buildConfigField("String", "BACKEND_URL", "\"https://aizaya-api.zayacodehub-aizaya.workers.dev/\"")
            buildConfigField("String", "OPENROUTER_API_KEY", "\"\"")
            buildConfigField("String", "GROQ_API_KEY", "\"\"")
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation(libs.navigation.compose)
    
    // AppCompat (needed for FragmentActivity + BiometricPrompt)
    implementation("androidx.appcompat:appcompat:1.7.0")
    
    // Material (needed for XML theme reference)
    implementation("com.google.android.material:material:1.12.0")

    // DataStore & Biometric
    implementation(libs.datastore.preferences)
    implementation(libs.biometric)

    // Room Database
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)

    // Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation(libs.google.auth)
    
    // AI SDK
    implementation(libs.google.ai.client)
    implementation(libs.coil.compose)

    // ML Kit OCR + PDF extraction
    implementation(libs.mlkit.text.recognition)
    implementation(libs.pdfbox.android)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
