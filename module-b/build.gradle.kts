plugins {
    id("android-library-conventions")
    id("detekt-conventions")
    id("spotless-conventions")
}

dependencies {
    // Add module-specific dependencies here
    // Using local JAR files for system interaction
    implementation(files("${project.rootDir}/Libs/api-82.jar"))
    implementation(files("${project.rootDir}/Libs/api-82-sources.jar"))
}
