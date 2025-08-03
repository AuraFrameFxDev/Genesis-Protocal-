import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

plugins {
    // This plugin builds on the base library conventions
    id("android-library-conventions")
}

val libs = extensions.getByType<VersionCatalogsExtension>().named("libs")

dependencies {
    implementation(libs.findLibrary("room.runtime").get())
    implementation(libs.findLibrary("room.ktx").get())
    add("ksp", libs.findLibrary("room.compiler").get())
    testImplementation(libs.findLibrary("room.testing").get())
}

android {
    // Room schema export configuration
    defaultConfig {
        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.schemaLocation"] = "$projectDir/schemas"
            }
        }
    }
}
