plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
}

android {
    namespace = "com.example.dancetrainer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.dancetrainer"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            // keep defaults
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
    }
    composeOptions {
        // Works with Kotlin 1.9.23 + Compose UI 1.7.x
        kotlinCompilerExtensionVersion = "1.5.15"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM to keep versions aligned
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))

    // Compose UI + Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Icons used in UI (e.g., Add FAB)
    implementation("androidx.compose.material:material-icons-extended")

    // Activity + Navigation for Compose
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // KotlinX Serialization (we use it in Storage.kt)
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // DocumentFile (SAF folder access)
    implementation("androidx.documentfile:documentfile:1.0.1")

    // KTX / Lifecycle helpers
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    // Tooling (debug only)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
