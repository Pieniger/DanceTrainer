dependencies {
    // Compose BOM to align artifacts
    implementation(platform("androidx.compose:compose-bom:2024.09.01"))

    // Compose UI + Material3 (Compose widgets)
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // ✅ Add this: Material Components (provides Theme.Material3.* XML themes)
    implementation("com.google.android.material:material:1.12.0")

    // Activity (Compose)
    implementation("androidx.activity:activity-compose:1.9.2")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    // DocumentFile (SAF folder access)
    implementation("androidx.documentfile:documentfile:1.0.1")

    // KTX / Lifecycle
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.5")

    // Tooling (debug)
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
