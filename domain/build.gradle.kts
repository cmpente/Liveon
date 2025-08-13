// domain/build.gradle.kts
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.kotlinx.serialization)
}

android {
    namespace = "com.liveongames.domain" // Add a unique namespace
    compileSdk = 34 // Use your project's compileSdk

    defaultConfig {
        minSdk = 24 // Match your project
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        jvmToolchain(17) // Match your project
    }

    // If domain has no resources, aidl, etc., you can disable them
    buildFeatures {
        // Set any features you don't need to false to reduce build overhead
        aidl = false
        renderScript = false
        shaders = false
        resValues = false
        buildConfig = false // If you don't use BuildConfig
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.gson)
    implementation(libs.javax.inject)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
}