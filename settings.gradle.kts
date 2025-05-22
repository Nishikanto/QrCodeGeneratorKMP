enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
pluginManagement {
    plugins {
        id("org.jetbrains.compose") version "1.6.10"
    }
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "QrCodeGeneratorKMP"
include(":androidApp")
include(":shared")
