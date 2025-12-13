plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.jarvismini.engine"
    compileSdk = 34

    defaultConfig {
        minSdk = 26
        targetSdk = 34
    }
}

dependencies {
    // Kotlin coroutines (REQUIRED)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
