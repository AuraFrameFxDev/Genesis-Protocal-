/**
 * Android Library Conventions
 *
 * Provides base configuration for Android library modules:
 * - Android Library & Kotlin Android plugins
 * - Kotlin Symbol Processing (KSP)
 * - Hilt dependency injection
 * - Java & Kotlin JVM target settings
 * - ProGuard rules for release builds
 *
 * Usage:
 * plugins {
 *   id("android-library-conventions")
 * }
 *
 * Notes:
 * - Update `namespace` to your module’s package name.
 * - Adjust `jvmTarget` if you prefer Java 21 LTS over Java 24.
 */
import org.gradle.api.JavaVersion
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // Enable KSP for annotation processing
    id("com.google.dagger.hilt.android") // Enable Hilt for dependency injection
}

android {
    namespace = "com.example.mylibrary" // Replace with your library's package name
    compileSdk = 36 // Use your desired compileSdk version

    defaultConfig {
        minSdk = 33 // Use your desired minSdk version
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    testOptions {
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.toVersion("24")
        targetCompatibility = JavaVersion.toVersion("24")
    }

    // Replace kotlinOptions with compilerOptions
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        }
    }
}
