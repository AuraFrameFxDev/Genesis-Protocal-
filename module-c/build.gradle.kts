@Suppress("DSL_SCOPE_VIOLATION")
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
    namespace = "dev.aurakai.auraframefx.modulec"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
        vectorDrawables {
            useSupportLibrary = true
        }
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.timber)
    implementation(libs.coil.compose)
    coreLibraryDesugaring(libs.coreLibraryDesugaring)
    // System interaction and documentation (using local JAR files)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
    dokkaHtmlPlugin(libs.dokka)
    // Add other dependencies as needed
}
