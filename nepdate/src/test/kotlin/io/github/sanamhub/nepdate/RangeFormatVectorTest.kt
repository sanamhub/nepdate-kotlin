package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * `range-format.tsv`: the default `start - end` form and the formatted one, both clipped to the
 * two bounds. The empty C# range cannot be built here (PARITY D-01), and the Nepali-language rows
 * come from the language selector (PARITY D-15).
 */
class RangeFormatVectorTest {
    @Test
    fun `every row of range-format tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/range-format.tsv")
        assertEquals(18, rows.size)
        for (row in rows) {
            val context = row.toString()
            val start = SharedFiles.date(row.getValue("start"))
            val end = SharedFiles.date(row.getValue("end"))
            val result = row.getValue("result")
            if (result == "ERR") {
                assertFailsWith<NepDateException>(context) { NepaliDateRange.of(start, end) }
                continue
            }
            val range = NepaliDateRange.of(start, end)
            val actual =
                when (row.getValue("kind")) {
                    "default" -> range.toString()
                    "short" ->
                        range.toFormattedString(
                            DateOrder.valueOf(row.getValue("order").uppercase()),
                            Separator.valueOf(row.getValue("separator").uppercase()),
                            if (row.getValue("lang") == "ne") Lang.NEPALI else Lang.ENGLISH,
                        )
                    else -> error("unknown kind in $context")
                }
            assertEquals(result, actual, context)
        }
    }

    @Test
    fun `toString is the bounds, not the list`() {
        val range = NepaliDateRange.of(NepaliDate.of(2199, 1, 1), NepaliDate.MAX)
        // Printing a 109,000-day range is one line, not one string per day.
        assertEquals("2199/01/01 - 2199/12/30", range.toString())
        assertEquals("०१/०१/२१९९ - ३०/१२/२१९९", range.toFormattedString(DateOrder.DMY, Separator.SLASH, Lang.NEPALI))
    }
}
