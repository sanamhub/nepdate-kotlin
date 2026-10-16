package io.github.sanamhub.nepdate.catalog

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

/** Every phase-4 `DateTimeExtensionsTests` row of the C# catalogue has a test function of that name. */
class DateTimeExtensionsCoverageTest {
    @Test
    fun `every DateTimeExtensionsTests row has a test`() {
        val catalog = File(System.getProperty("nepdate.shared"), "spec/vectors/csharp-tests/catalog.tsv")
        val rows =
            catalog
                .readLines(Charsets.UTF_8)
                .map { it.split('\t') }
                .filter { it.first().startsWith("DateTimeExtensionsTests.") && it.last() == "4" }
                .map { it.first().removePrefix("DateTimeExtensionsTests.") }
        assertEquals(5, rows.size)
        val tests =
            DateTimeExtensionsTests::class.java.declaredMethods
                .filter { it.isAnnotationPresent(kotlin.test.Test::class.java) }
                .map { it.name }
        assertEquals(rows.sorted(), tests.sorted())
    }
}
