plugins {
    id("org.jetbrains.kotlin.jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
    implementation(libs.coroutines.core)
    implementation(libs.gson)
    implementation(libs.javax.inject)

    testImplementation(libs.junit)
}