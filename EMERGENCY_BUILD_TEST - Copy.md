# Quick Local Build Test - Minimal Configuration

# 1. Temporarily disable most modules
# Edit settings.gradle.kts and comment out most modules:

rootProject.name = "Genesis-Os"

# Include only essential modules for testing
include(":app")
# include(":core-module")
# include(":feature-module") 
# include(":datavein-oracle-drive")
# include(":datavein-oracle-native")
# include(":secure-comm")

# 2. Further reduce memory in gradle.properties:
org.gradle.jvmargs=-Xmx128m -Xms64m -XX:MaxMetaspaceSize=128m -XX:+UseSerialGC
kotlin.daemon.jvmargs=-Xmx512m -Xms128m -XX:+UseSerialGC

# 3. Try building just the app module:
./gradlew :app:assembleDebug

# This will test if the basic Android + Kotlin setup works on your system.
