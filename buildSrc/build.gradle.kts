plugins {
    `kotlin-dsl`
    `kotlin-dsl-precompiled-script-plugins`
    `java-gradle-plugin`
       id("com.android.application") version "8.12.0" apply false
    id("com.android.library") version "8.12.0" apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("com.google.dagger.hilt.android") version "2.57" apply false
    alias(libs.plugins.kotlin.compose) apply false

}

repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
}

kotlin {
    jvmToolchain(24)
    compilerOptions {
        freeCompilerArgs.addAll(
            "-opt-in=kotlin.RequiresOptIn"
        )
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_2)
    }
}

dependencies {
    // Build plugins with explicit versions (buildSrc cannot use version catalog)
    implementation("com.android.tools.build:gradle:8.11.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.2.0")
    implementation("com.google.devtools.ksp:symbol-processing-gradle-plugin:2.2.0-2.0.2")
    implementation("com.google.dagger:hilt-android-gradle-plugin:2.57")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:7.2.1")
    implementation("io.gitlab.arturbosch.detekt:detekt-gradle-plugin:1.23.8")
    implementation("org.openapitools:openapi-generator-gradle-plugin:7.14.0")
    // Testing
    testImplementation("org.junit.jupiter:junit-jupiter:5.13.4")
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation(gradleTestKit())
}

tasks.test {
    // ...existing code...
}
