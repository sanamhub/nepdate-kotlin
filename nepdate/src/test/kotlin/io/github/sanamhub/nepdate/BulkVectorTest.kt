package io.github.sanamhub.nepdate

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * `bulk.tsv`: the ordered batch conversions both ways. There is one function each way, not the five
 * C# `BulkConvert`/`BatchProcess*` entry points, and the result is in input order.
 */
class BulkVectorTest {
    private fun adDates(text: String): List<LocalDate> = text.split(';').map { LocalDate.parse(it) }

    @Test
    fun `every row of bulk tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/bulk.tsv")
        assertEquals(6, rows.size)
        for (row in rows) {
            val context = row.toString()
            val input = row.getValue("input")
            val expected = row.getValue("output")
            when (row.getValue("operation")) {
                "to_nepali_dates", "batch_to_nepali_dates" -> {
                    val result = toNepaliDates(input.split(';').map { LocalDate.parse(it) })
                    assertEquals(expected.split(';').map { SharedFiles.date(it) }, result, context)
                }
                "to_english_dates_from_strings" ->
                    if (expected == "ERR") {
                        assertFailsWith<NepDateException>(context) { toEnglishDates(input.split(';')) }
                    } else {
                        assertEquals(adDates(expected), toEnglishDates(input.split(';')), context)
                    }
                "to_english_dates_from_dates", "batch_to_english_dates" ->
                    assertEquals(adDates(expected), toEnglishDates(input.split(';').map { SharedFiles.date(it) }), context)
                else -> error("unknown operation in $context")
            }
        }
    }

    @Test
    fun `bulk conversion keeps the input order and the input size`() {
        val inputs =
            listOf(
                LocalDate.of(2024, 7, 31),
                LocalDate.of(1844, 4, 11),
                LocalDate.of(2143, 4, 15),
                LocalDate.of(2000, 1, 1),
                LocalDate.of(2025, 4, 14),
            )
        val converted = toNepaliDates(inputs)
        assertEquals(5, converted.size)
        assertEquals(inputs.map { it.toNepaliDate() }, converted)
        assertEquals(inputs, toEnglishDates(converted))
        assertEquals(toEnglishDates(converted), toEnglishDates(converted.map { it.toIsoString() }))
        assertEquals(emptyList(), toNepaliDates(emptyList<LocalDate>()))
        assertEquals(emptyList(), toEnglishDates(emptyList<NepaliDate>()))
    }

    @Test
    fun `one bad string fails the whole call`() {
        val good = listOf("2081-04-15")
        assertEquals(listOf(LocalDate.of(2024, 7, 30)), toEnglishDates(good))
        assertFailsWith<NepDateException> { toEnglishDates(good + "2081-01-32") }
    }
}
