package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * `range-factories.tsv`: one day, a day count, a month, a calendar year and a fiscal year. A port
 * refuses a start after the end (PARITY D-01), so the C# empty-range construct row is an error.
 */
class RangeFactoryVectorTest {
    @Test
    fun `every row of range-factories tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/range-factories.tsv")
        assertEquals(27, rows.size)
        for (row in rows) {
            val context = row.toString()
            val arguments = row.getValue("argument").split(',')
            val expected = row.getValue("result")
            val factory: () -> NepaliDateRange =
                when (row.getValue("factory")) {
                    "single_day" -> {
                        { NepaliDateRange.singleDay(SharedFiles.date(arguments[0])) }
                    }
                    "from_day_count" -> {
                        { NepaliDateRange.fromDayCount(SharedFiles.date(arguments[0]), arguments[1].toInt()) }
                    }
                    "for_month" -> {
                        { NepaliDateRange.ofMonth(arguments[0].toInt(), arguments[1].toInt()) }
                    }
                    "for_calendar_year" -> {
                        { NepaliDateRange.ofYear(arguments[0].toInt()) }
                    }
                    "for_fiscal_year" -> {
                        { FiscalYear(arguments[0].toInt()).range() }
                    }
                    "construct" -> {
                        { NepaliDateRange.of(SharedFiles.date(arguments[0]), SharedFiles.date(arguments[1])) }
                    }
                    else -> error("unknown factory in $context")
                }
            if (expected == "ERR") {
                assertFailsWith<NepDateException>(context) { factory() }
            } else {
                assertEquals(SharedFiles.range(expected), factory(), context)
            }
        }
    }

    @Test
    fun `a day count near the type's maximum cannot wrap the range`() {
        val start = NepaliDate.of(2081, 4, 15)
        // The last day is computed as a Long before it is narrowed, so Int.MAX_VALUE days is a
        // range that ends past 2199-12-30 rather than one that wraps back into the supported span.
        assertFailsWith<NepDateException> { NepaliDateRange.fromDayCount(start, Int.MAX_VALUE) }
        assertFailsWith<NepDateException> { NepaliDateRange.fromDayCount(start, 109212) }
        assertEquals(NepaliDate.MAX, NepaliDateRange.fromDayCount(NepaliDate.MAX, 1).endInclusive)
        assertEquals(
            NepaliDateRange.of(NepaliDate.of(2199, 12, 25), NepaliDate.MAX),
            NepaliDateRange.fromDayCount(NepaliDate.of(2199, 12, 25), 6),
        )
        assertEquals(109212, NepaliDateRange.fromDayCount(NepaliDate.MIN, 109212).size)
    }

    @Test
    fun `singleDay and ofMonth bounds`() {
        val date = NepaliDate.of(2081, 4, 15)
        assertEquals(date, NepaliDateRange.singleDay(date).start)
        assertEquals(date, NepaliDateRange.singleDay(date).endInclusive)
        assertEquals(1, NepaliDateRange.singleDay(date).size)
        assertEquals(NepaliDate.of(2081, 4, 1), NepaliDateRange.ofMonth(2081, 4).start)
        assertEquals(NepaliDate.of(2081, 4, 32), NepaliDateRange.ofMonth(2081, 4).endInclusive)
    }
}
