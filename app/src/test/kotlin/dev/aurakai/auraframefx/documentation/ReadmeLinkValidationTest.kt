package dev.aurakai.auraframefx.documentation

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File
import java.util.regex.Pattern

/**
 * Link validation tests for README.md
 * Testing Framework: JUnit 4
 * 
 * Validates external links and file references
 */
class ReadmeLinkValidationTest {

    private lateinit var readmeContent: String

    @Before
    fun setUp() {
        val readmeFile = File("README.md")
        readmeContent = readmeFile.readText()
    }

    @Test
    fun `should validate GitHub repository links format`() {
        val githubPattern = Pattern.compile("https://github\\.com/[^\\s\\)]+")
        val matcher = githubPattern.matcher(readmeContent)
        
        assertTrue("Should contain GitHub repository links", matcher.find())
        
        // Validate all GitHub links
        matcher.reset()
        while (matcher.find()) {
            val url = matcher.group()
            assertTrue("GitHub URL should reference correct repository: $url",
                url.contains("AuraFrameFxDev/Genesis-Os"))
        }
    }

    @Test
    fun `should validate Oracle GraalVM download link specificity`() {
        assertTrue("Should contain specific GraalVM download link",
            readmeContent.contains("https://download.oracle.com/graalvm"))
        
        val graalvmPattern = Pattern.compile("https://download\\.oracle\\.com/graalvm/[^\\s\\)]+")
        val matcher = graalvmPattern.matcher(readmeContent)
        
        if (matcher.find()) {
            val url = matcher.group()
            assertTrue("GraalVM URL should specify version 24", url.contains("24"))
            assertTrue("GraalVM URL should be for JDK", url.contains("jdk"))
            assertTrue("GraalVM URL should specify Windows x64", url.contains("windows-x64"))
        }
    }

    @Test
    fun `should reference all critical project files`() {
        val criticalFiles = listOf(
            "CONTRIBUTING.md",
            "LICENSE",
            "local.properties",
            "enhanced-openapi.yml",
            "generate-openapi.ps1",
            "gradle.properties"
        )
        
        criticalFiles.forEach { fileName ->
            assertTrue("Should reference critical file: $fileName",
                readmeContent.contains(fileName))
        }
    }

    @Test
    fun `should validate project structure path references`() {
        val structurePaths = listOf(
            "app/ai_backend/",
            "buildSrc/src/main/kotlin/",
            "Libs/",
            "api-spec/",
            "gradle/",
            "app/src/main/java/",
            "datavein-oracle-drive/",
            "oracle-drive-integration/",
            "secure-comm/",
            "sandbox-ui/",
            "collab-canvas/"
        )
        
        structurePaths.forEach { path ->
            assertTrue("Should reference project path: $path",
                readmeContent.contains(path))
        }
    }

    @Test
    fun `should validate dependency and library references`() {
        val dependencies = listOf(
            "api-82.jar",
            "api-82-sources.jar", 
            "google-cloud-aiplatform",
            "vertexai",
            "LSPosed framework",
            "Xposed framework"
        )
        
        dependencies.forEach { dep ->
            assertTrue("Should reference dependency: $dep",
                readmeContent.contains(dep))
        }
    }

    @Test
    fun `should validate command examples format and completeness`() {
        val commands = listOf(
            "git clone https://github.com/AuraFrameFxDev/Genesis-Os.git",
            "cd Genesis-Os",
            ".\\generate-openapi.ps1",
            "pip install google-cloud-aiplatform vertexai"
        )
        
        commands.forEach { command ->
            assertTrue("Should contain properly formatted command: $command",
                readmeContent.contains(command))
        }
    }

    @Test
    fun `should validate configuration file examples`() {
        // Gradle properties validation
        assertTrue("Should contain gradle.properties example",
            readmeContent.contains("org.gradle.jvmargs=-Xmx6g"))
        assertTrue("Should contain kotlin daemon configuration",
            readmeContent.contains("kotlin.daemon.jvmargs"))
        
        // Local properties validation  
        assertTrue("Should contain local.properties template",
            readmeContent.contains("sdk.dir=/path/to/your/android/sdk"))
    }

    @Test
    fun `should validate API specification references`() {
        val apiSpecs = listOf(
            "enhanced-openapi.yml",
            "openapi-generation-conventions.gradle.kts",
            "GenesisBridgeService",
            "VertexAI integration"
        )
        
        apiSpecs.forEach { spec ->
            assertTrue("Should reference API specification: $spec",
                readmeContent.contains(spec))
        }
    }
}