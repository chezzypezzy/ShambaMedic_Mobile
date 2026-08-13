import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlinCompose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.googleServices)
}

// Local, gitignored dev config (SDK path, API keys). Same convention this project
// uses for other machine-specific values - never commit real secrets into
// build.gradle.kts, which is checked into version control.
val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localPropertiesFile.inputStream().use { load(it) }
    }
}

android {
    namespace = "com.example.shambamedic"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.shambamedic"
        minSdk = 28
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // REQUIRES EXTERNAL SETUP: must match the backend's SYNC_API_KEY value from
        // backend/.env - get this value from whoever manages the backend deployment
        // before this will authenticate successfully. Sourced from local.properties
        // (gitignored), not hardcoded here, since this file is version-controlled.
        buildConfigField(
            "String",
            "SYNC_API_KEY",
            "\"${localProperties.getProperty("sync.api.key", "REPLACE_WITH_BACKEND_SYNC_API_KEY")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            // Release always targets the real deployed backend over HTTPS, regardless of
            // whatever local backend the developer's machine has configured for debug testing.
            buildConfigField(
                "String",
                "BASE_URL",
                "\"https://shambamedic-backend.onrender.com/\""
            )
        }
        debug {
            // Base URL for local development. Sourced from local.properties (gitignored) so
            // each developer/machine can point at a local backend (emulator 10.0.2.2 or a LAN
            // IP) without touching version-controlled files. Defaults to the hosted backend if
            // unset, so a fresh checkout still points somewhere sensible without requiring
            // local backend setup.
            buildConfigField(
                "String",
                "BASE_URL",
                "\"${localProperties.getProperty("base.url", "https://shambamedic-backend.onrender.com/")}\""
            )
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
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/DEPENDENCIES"
        }
        jniLibs {
            pickFirsts += "**/libusb.so"
        }
    }
    aaptOptions {
        noCompress += "tflite"
    }
}

dependencies {
    // Jetpack Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.appcompat)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)
    
    // Room
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    ksp(libs.room.compiler)
    
    // Retrofit & Networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.okhttp.logging)
    
    // TensorFlow Lite
    implementation(libs.tensorflow.lite)
    // implementation(libs.tensorflow.lite.support)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    implementation(libs.kotlinx.coroutines.core)
    
    // Lifecycle
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    
    // Accompanist (Permissions)
    implementation(libs.accompanist.permissions)
    
    // Coil
    implementation(libs.coil.compose)
    
    // DataStore
    implementation(libs.androidx.datastore.preferences)
    
    // CameraX
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)

    // Core
    implementation(libs.androidx.core.ktx)

    // Google Sign-In (Credential Manager)
    implementation(libs.play.services.auth)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // WorkManager (periodic background sync) + Hilt integration
    implementation(libs.androidx.work.runtime.ktx)
    implementation(libs.androidx.hilt.work)
    ksp(libs.androidx.hilt.compiler)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
