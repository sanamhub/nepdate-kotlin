package io.github.sanamhub.nepdate.patro.catalog

import org.junit.jupiter.api.Test
import java.io.File
import kotlin.test.assertEquals

/** Every phase-4 `CalendarDataTests` row of the C# catalogue has a test function of that name. */
class CatalogueCoverageTest {
    @Test
    fun `every CalendarDataTests row has a test`() {
        val catalog = File(System.getProperty("nepdate.shared"), "spec/vectors/csharp-tests/catalog.tsv")
        val rows =
            catalog
                .readLines(Charsets.UTF_8)
                .map { it.split('\t') }
                .filter { it.first().startsWith("CalendarDataTests.") && it.last() == "4" }
                .map { it.first().removePrefix("CalendarDataTests.") }
        assertEquals(35, rows.size)
        val tests =
            CalendarDataTests::class.java.declaredMethods
                .filter { it.isAnnotationPresent(kotlin.test.Test::class.java) }
                .map { it.name }
        assertEquals(rows.sorted(), tests.sorted())
    }
}
