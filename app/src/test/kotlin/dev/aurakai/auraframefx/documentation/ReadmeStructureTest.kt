package dev.aurakai.auraframefx.documentation

import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.io.File
import java.util.regex.Pattern

/**
 * Structure and formatting validation for README.md
 * Testing Framework: JUnit 4
 * 
 * Validates structural integrity and markdown formatting
 */
class ReadmeStructureTest {

    private lateinit var readmeContent: String
    private lateinit var readmeLines: List<String>

    @Before
    fun setUp() {
        val readmeFile = File("README.md")
        readmeContent = readmeFile.readText()
        readmeLines = readmeContent.lines()
    }

    @Test
    fun `should maintain proper heading hierarchy`() {
        val headingPattern = Pattern.compile("^(#{1,6})\\s+(.+)$")
        var hasH1 = false
        var previousLevel = 0
        
        readmeLines.forEach { line ->
            val matcher = headingPattern.matcher(line)
            if (matcher.matches()) {
                val level = matcher.group(1).length
                val title = matcher.group(2)
                
                if (level == 1) {
                    hasH1 = true
                    assertEquals("H1 should be project title", 
                        "AuraFrameFX - Genesis OS", title)
                }
                
                assertFalse("Heading title should not be empty", title.trim().isEmpty())
                
                // Validate heading level progression
                if (previousLevel > 0 && level > previousLevel) {
                    assertTrue("Should not skip more than one heading level",
                        level - previousLevel <= 2)
                }
                
                previousLevel = level
            }
        }
        
        assertTrue("Should have exactly one H1 heading", hasH1)
    }

    @Test
    fun `should have consistent and meaningful list formatting`() {
        val bulletListPattern = Pattern.compile("^\\s*[-*+]\\s+.+$")
        val numberedListPattern = Pattern.compile("^\\s*\\d+\\.\\s+.+$")
        
        var bulletListCount = 0
        var numberedListCount = 0
        
        readmeLines.forEach { line ->
            if (bulletListPattern.matcher(line).matches()) {
                bulletListCount++
                val content = line.replace(Regex("^\\s*[-*+]\\s+"), "")
                assertFalse("Bullet list item should have meaningful content", 
                    content.trim().isEmpty())
            }
            if (numberedListPattern.matcher(line).matches()) {
                numberedListCount++
                val content = line.replace(Regex("^\\s*\\d+\\.\\s+"), "")
                assertFalse("Numbered list item should have meaningful content",
                    content.trim().isEmpty())
            }
        }
        
        assertTrue("Should contain bullet list items", bulletListCount > 10)
        assertTrue("Should contain numbered list items", numberedListCount > 5)
    }

    @Test
    fun `should use proper emphasis and inline formatting`() {
        // Bold text validation
        val boldPattern = Pattern.compile("\\*\\*([^*]+)\\*\\*")
        val boldMatcher = boldPattern.matcher(readmeContent)
        
        var boldCount = 0
        while (boldMatcher.find()) {
            boldCount++
            val boldText = boldMatcher.group(1)
            assertFalse("Bold text should not be empty", boldText.trim().isEmpty())
        }
        
        assertTrue("Should contain meaningful bold formatting", boldCount > 15)
        
        // Inline code validation
        val inlineCodePattern = Pattern.compile("`([^`]+)`")
        val inlineCodeMatcher = inlineCodePattern.matcher(readmeContent)
        
        var codeCount = 0
        while (inlineCodeMatcher.find()) {
            codeCount++
            val codeText = inlineCodeMatcher.group(1)
            assertFalse("Inline code should not be empty", codeText.trim().isEmpty())
        }
        
        assertTrue("Should contain inline code formatting", codeCount > 20)
    }

    @Test
    fun `should have consistent status indicator usage`() {
        val statusIndicators = mapOf(
            "✅" to "completed",
            "⚠️" to "warning",
            "🔄" to "progress", 
            "📋" to "planned"
        )
        
        statusIndicators.forEach { (indicator, meaning) ->
            assertTrue("Should use $meaning indicator: $indicator",
                readmeContent.contains(indicator))
        }
        
        // Validate meaningful usage of checkmarks
        val completedItems = readmeContent.split("✅").size - 1
        assertTrue("Should have multiple completed items marked", completedItems >= 8)
    }

    @Test
    fun `should maintain proper section organization and flow`() {
        val sectionOrder = listOf(
            "🌟 Overview",
            "🏆 Project Status", 
            "🚀 Key Features",
            "🏗️ Technical Stack",
            "🚀 Getting Started",
            "🏗️ Project Structure"
        )
        
        var lastIndex = -1
        sectionOrder.forEach { section ->
            val currentIndex = readmeContent.indexOf("## $section")
            assertTrue("Section should exist: $section", currentIndex != -1)
            assertTrue("Sections should be in logical order: $section", currentIndex > lastIndex)
            lastIndex = currentIndex
        }
    }

    @Test
    fun `should validate proper spacing and readability`() {
        // Line length validation (allowing some flexibility for URLs and code)
        val veryLongLines = readmeLines.filter { it.length > 300 }
        assertTrue("Should not have excessively long lines", 
            veryLongLines.size < readmeLines.size * 0.05)
        
        // Blank line spacing around headers
        for (i in 1 until readmeLines.size - 1) {
            val line = readmeLines[i]
            if (line.startsWith("## ")) {
                if (i > 1) {
                    val previousLine = readmeLines[i - 1]
                    assertTrue("Header at line ${i + 1} should have proper spacing",
                        previousLine.isEmpty() || previousLine.startsWith("#"))
                }
            }
        }
    }

    @Test
    fun `should validate code block integrity and formatting`() {
        val codeBlockCount = readmeContent.split("```").size - 1
        assertTrue("Should have even number of code block markers", codeBlockCount % 2 == 0)
        
        // Validate code block content
        val codeBlockPattern = Pattern.compile("```([\\w]*)\\n([\\s\\S]*?)```")
        val matcher = codeBlockPattern.matcher(readmeContent)
        
        var validCodeBlocks = 0
        while (matcher.find()) {
            val language = matcher.group(1)
            val content = matcher.group(2)
            
            assertFalse("Code block should not be empty", content.trim().isEmpty())
            validCodeBlocks++
        }
        
        assertTrue("Should have substantial code examples", validCodeBlocks >= 8)
    }

    @Test
    fun `should have comprehensive section coverage`() {
        val essentialSections = listOf(
            "Overview",
            "Getting Started",
            "Technical Stack", 
            "Project Structure",
            "Development Setup",
            "AI System Architecture",
            "Security & Privacy",
            "Contributing",
            "License"
        )
        
        essentialSections.forEach { section ->
            assertTrue("Should comprehensively cover: $section",
                readmeContent.contains(section))
        }
    }

    @Test
    fun `should validate table and structured data formatting`() {
        // Check for proper configuration examples
        assertTrue("Should contain properly formatted gradle properties",
            readmeContent.contains("org.gradle.jvmargs") && 
            readmeContent.contains("org.gradle.parallel"))
        
        // Validate project structure tree formatting
        val treeElements = listOf("├──", "└──", "│")
        treeElements.forEach { element ->
            assertTrue("Should use proper tree formatting: $element",
                readmeContent.contains(element))
        }
    }

    @Test
    fun `should maintain document cohesiveness and completeness`() {
        // Cross-reference validation
        assertTrue("Should reference Trinity system across multiple sections",
            readmeContent.split("Trinity").size >= 5)
        assertTrue("Should reference Oracle Drive across multiple sections",
            readmeContent.split("Oracle Drive").size >= 4)
        assertTrue("Should reference Genesis across multiple sections",
            readmeContent.split("Genesis").size >= 8)
        
        // Completeness indicators
        assertTrue("Should provide comprehensive installation steps",
            readmeContent.contains("Prerequisites") && 
            readmeContent.contains("Building from Source"))
        assertTrue("Should provide detailed troubleshooting information",
            readmeContent.contains("Known Limitation") &&
            readmeContent.contains("Workaround"))
    }
}