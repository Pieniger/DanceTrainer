// (what you already have)
pluginManagement {
    repositories {
        gradlePluginPortal()
@@ -10,12 +11,3 @@ pluginManagement {
        id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "DanceTrainer"
include(":app")
