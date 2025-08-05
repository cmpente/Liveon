// domain/build.gradle.kts
plugins {
    alias(libs.plugins.kotlinJvm)
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    // Core coroutines for domain module
    implementation(libs.coroutines.core)

    // For dependency injection in pure Kotlin modules, use javax.inject
    implementation(libs.javax.inject)

    // Test dependencies
    testImplementation(libs.junit)
}