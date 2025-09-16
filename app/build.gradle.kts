plugins { id("com.android.application"); id("org.jetbrains.kotlin.android"); id("org.jetbrains.kotlin.plugin.serialization") }
android {
  namespace = "com.example.dancetrainer"
  compileSdk = 35
  defaultConfig {
    applicationId = "com.example.dancetrainer"
    minSdk = 26; targetSdk = 35; versionCode = 1; versionName = "0.1.0"
  }
  buildFeatures { compose = true }
  composeOptions { kotlinCompilerExtensionVersion = "1.5.15" }
}
dependencies {
  val bom = platform("androidx.compose:compose-bom:2024.09.01")
  implementation(bom); androidTestImplementation(bom)
  implementation("androidx.core:core-ktx:1.13.1")
  implementation("androidx.activity:activity-compose:1.9.2")
  implementation("androidx.compose.ui:ui"); implementation("androidx.compose.ui:ui-tooling-preview")
  debugImplementation("androidx.compose.ui:ui-tooling")
  implementation("androidx.compose.material3:material3:1.3.0")
  implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")
  implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.5")
  implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.1")
}
