plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
    alias(libs.plugins.spotless)
}

android {
    compileSdk = libs.versions.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "24"
        freeCompilerArgs += listOf(
            "-Xskip-prerelease-check",
            "-opt-in=kotlin.RequiresOptIn",
            "-opt-in=kotlin.ExperimentalStdlibApi",
            "-Xjvm-default=all",
            "-progressive"
        )
        apiVersion = "2.2"
        languageVersion = "2.2"
    }
}

dependencies {
    // Add module-specific dependencies here
    // Using local JAR files for system interaction
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
}
