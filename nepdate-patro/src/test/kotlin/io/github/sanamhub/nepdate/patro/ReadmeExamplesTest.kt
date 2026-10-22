package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

/**
 * The Kotlin snippet under `## Quick start` in README.md must be exactly the marked region
 * below, and the same snippet must run. The Java snippet is checked against the marked region in
 * ReadmeExamples.java, which the `javaApiTest` task compiles and runs.
 */
class ReadmeExamplesTest {
    @Test
    fun kotlinExampleRuns() {
        // README-START
        val date = NepaliDate.of(2081, 4, 15)
        val fromAd = NepaliDate.ofGregorian(2024, 7, 30)
        val longForm = date.toLongString(lang = Lang.NEPALI)
        val parsed = NepaliDate.parseLenient("15 Shrawan 2080")
        val tithi = date.calendarInfo.tithi
        // README-END
        assertEquals(NepaliDate.of(2081, 4, 15), fromAd)
        assertEquals(NepaliDate.of(2080, 4, 15), parsed)
        assertTrue(longForm.isNotBlank())
        assertTrue(tithi != null)
    }

    @Test
    fun readmeBlocksMatchTheExamples() {
        val readme = File(repoRoot(), "README.md").readText()
        val kotlinBlock = firstFencedBlock(readme, "## Quick start", "kotlin")
        val javaBlock = firstFencedBlock(readme, "## Quick start", "java")
        val kotlinSource =
            File(repoRoot(), "nepdate-patro/src/test/kotlin/io/github/sanamhub/nepdate/patro/ReadmeExamplesTest.kt")
                .readText()
        val javaSource =
            File(repoRoot(), "nepdate-patro/src/javaApiTest/java/io/github/sanamhub/nepdate/patro/ReadmeExamples.java")
                .readText()
        assertEquals(normalize(markedRegion(kotlinSource)), normalize(kotlinBlock), "README kotlin block")
        assertEquals(normalize(markedRegion(javaSource)), normalize(javaBlock), "README java block")
    }

    /**
     * The README install block is the consumer check's dependencies block at the version in
     * gradle.properties, so a version bump that forgets the README fails here.
     */
    @Test
    fun readmeInstallBlockMatchesConsumer() {
        val readme = File(repoRoot(), "README.md").readText()
        val installBlock = firstFencedBlock(readme, "## Install", "kotlin")
        val consumerBuild = File(repoRoot(), "tools/consumer-check/build.gradle.kts").readText()
        val version =
            File(repoRoot(), "gradle.properties")
                .readLines()
                .first { it.startsWith("version=") }
                .substringAfter("=")
                .trim()
        val expected = dependenciesBlock(consumerBuild).replace("\$nepdateVersion", version)
        assertEquals(normalize(expected), normalize(installBlock), "README install block")
    }

    private fun dependenciesBlock(source: String): String {
        val start = source.indexOf("dependencies {")
        assertTrue(start >= 0, "consumer build file has no dependencies block")
        var depth = 0
        var i = start
        while (i < source.length) {
            when (source[i]) {
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return source.substring(start, i + 1)
                }
            }
            i++
        }
        throw AssertionError("unbalanced braces in the consumer dependencies block")
    }

    private fun repoRoot(): String = System.getProperty("nepdate.repoRoot")

    private fun firstFencedBlock(
        text: String,
        section: String,
        language: String,
    ): String {
        val sectionStart = text.indexOf(section)
        assertTrue(sectionStart >= 0, "$section not found in README.md")
        val fence = "```$language"
        val fenceStart = text.indexOf(fence, sectionStart)
        assertTrue(fenceStart >= 0, "$fence block not found under $section")
        val contentStart = fenceStart + fence.length + 1
        val contentEnd = text.indexOf("```", contentStart)
        assertTrue(contentEnd >= 0, "closing fence missing after $fence")
        return text.substring(contentStart, contentEnd)
    }

    private fun markedRegion(source: String): String {
        val start = source.indexOf("// README-START")
        val end = source.indexOf("// README-END")
        assertTrue(start >= 0 && end > start, "marked region missing")
        val contentStart = source.indexOf('\n', start) + 1
        val lines = source.substring(contentStart, end).lineSequence().toList()
        val indent = lines.filter { it.isNotBlank() }.minOf { it.indexOfFirst { c -> !c.isWhitespace() } }
        return lines.joinToString("\n") { it.drop(indent) }.trim()
    }

    private fun normalize(block: String): String = block.lineSequence().joinToString("\n") { it.trimEnd() }.trim()
}
