plugins {
    id("android-library-conventions")
    id("detekt-conventions")
    id("spotless-conventions")
}

android {
    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    // Add module-specific dependencies here
    // Using local JAR files for system interaction
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
    dokkaHtmlPlugin(libs.dokka)

    // Placeholder for Antaive integration (please specify details if needed)
}
