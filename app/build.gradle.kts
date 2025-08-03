import org.jetbrains.kotlin.gradle.dsl.JvmTarget

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.dagger.hilt.android")

    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("org.jetbrains.kotlin.plugin.compose")



android {
    namespace = "dev.aurakai.auraframefx"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        testInstrumentationRunnerArguments += mapOf()
        applicationId = "dev.aurakai.auraframefx"
        minSdk = libs.versions.minSdk.get().toInt()
        targetSdk = libs.versions.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        testInstrumentationRunnerArguments["clearPackageData"] = "true"

        vectorDrawables {
            useSupportLibrary = true
        }
        externalNativeBuild {
            cmake {
                cppFlags += ""
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
    }

    buildFeatures {
        viewBinding = true
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_24
        targetCompatibility = JavaVersion.VERSION_24
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_24)
        }
    }

    packaging {
        resources {
            excludes += setOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "/META-INF/AL2.0",
                "/META-INF/LGPL2.1",
                "META-INF/LICENSE*",
                "META-INF/NOTICE*",
                "META-INF/*.version",
                "META-INF/proguard/*",
                "/*.properties",
                "fabric/*.properties",
                "DebugProbesKt.bin"
            )
        }
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
        unitTests.isReturnDefaultValues = true
    }

    sourceSets["main"].java.srcDir("build/generated/openapi/src/main/kotlin")
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }
}

dependencies {
    // Core AndroidX
    implementation(libs.androidx.core.ktx
    implementation(libs.androidx.material)
    implementation(libs.androidx.recyclerview)
    implementation("androidx.appcompat:appcompat:1.7.0")

    implementation(libs.bundles.lifecycle)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.bundles.compose)

    // Hilt - Temporarily disable KSP for successful build
    implementation(libs.hilt.android)
    // kapt(libs.hilt.compiler) // Re-enabled for Hilt
    implementation(libs.hilt.navigation.compose)
    implementation("androidx.hilt:hilt-work:1.2.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-core:1.1.7")

    // Moshi for JSON
    implementation("com.squareup.moshi:moshi:1.15.2")
    implementation("com.squareup.moshi:moshi-kotlin:1.15.2")
    implementation("com.squareup.retrofit2:converter-moshi:3.0.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.3")

    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    
    // OpenAPI Generated Code Dependencies
    implementation(libs.retrofit)
    implementation(libs.retrofit.converter.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp3.logging.interceptor)
    implementation(libs.kotlinx.serialization.json)

    // Core library desugaring
    coreLibraryDesugaring(libs.coreLibraryDesugaring)

    // Testing
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.engine)

    // Android Instrumentation Tests
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.hilt.android.testing)
    kaptAndroidTest(libs.hilt.compiler)

    // Debug implementations
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // System interaction and documentation (using local JAR files)
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
    // Dokka for documentation
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.4.0")
}
