plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    // REQUIRED with Kotlin 2.0+ when compose = true
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
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
        vectorDrawables.useSupportLibrary = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
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
    }

    // With Kotlin 2.0 + compose plugin, you do NOT need composeOptions.kotlinCompilerExtensionVersion
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))

    // Compose + Material3
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.0")

    // Activity & lifecycle
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    // DocumentFile (optional convenience)
    implementation("androidx.documentfile:documentfile:1.0.1")

    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
