package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * `range-days.tsv`: the interval, working-day, weekend-day and enumeration helpers. Every helper
 * includes 2199-12-30 and never advances past it (PARITY D-16).
 */
class RangeDaysVectorTest {
    @Test
    fun `every row of range-days tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/range-days.tsv")
        assertEquals(18, rows.size)
        for (row in rows) {
            val context = row.toString()
            val range = SharedFiles.range("${row.getValue("start")}..${row.getValue("end")}")
            val argument = row.getValue("argument")
            val expected = row.getValue("result")
            val actual: () -> List<NepaliDate> =
                when (row.getValue("operation")) {
                    "dates_with_interval" -> {
                        { range.datesWithInterval(argument.toInt()) }
                    }
                    "working_days" -> {
                        { range.workingDays(argument.toBooleanStrict()) }
                    }
                    "weekend_days" -> {
                        { range.weekendDays(argument.toBooleanStrict()) }
                    }
                    "enumerate" -> {
                        { range.toList() }
                    }
                    else -> error("unknown operation in $context")
                }
            if (expected == "ERR") {
                assertFailsWith<NepDateException>(context) { actual() }
            } else {
                assertEquals(SharedFiles.dates(expected), actual(), context)
            }
        }
    }

    @Test
    fun `an interval near the type's maximum cannot wrap`() {
        val range = NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20))
        // The next date is accumulated as a Long, so the step after the first date is past the end
        // of the range rather than a wrapped serial that lands back inside it.
        assertEquals(listOf(NepaliDate.of(2081, 4, 10)), range.datesWithInterval(Int.MAX_VALUE))
        assertEquals(listOf(NepaliDate.of(2081, 4, 10)), range.datesWithInterval(11))
        assertEquals(11, range.datesWithInterval(1).size)
        assertEquals(1, NepaliDateRange.singleDay(NepaliDate.MAX).datesWithInterval(Int.MAX_VALUE).size)
    }

    @Test
    fun `working days skip Saturday and Sunday`() {
        val range = NepaliDateRange.of(NepaliDate.of(2081, 4, 13), NepaliDate.of(2081, 4, 21))
        assertEquals(8, range.workingDays().size)
        assertEquals(6, range.workingDays(excludeSunday = true).size)
        assertEquals(3, range.weekendDays().size)
        assertEquals(1, range.weekendDays(includeSunday = false).size)
        // 2081-04-15 is a Tuesday, so a one-day range has no weekend day.
        val tuesday = NepaliDateRange.singleDay(NepaliDate.of(2081, 4, 15))
        assertEquals(1, tuesday.workingDays().size)
        assertEquals(0, tuesday.weekendDays().size)
    }

    @Test
    fun `a range of one day at the data end lists that day`() {
        val last = NepaliDateRange.singleDay(NepaliDate.MAX)
        assertEquals(listOf(NepaliDate.MAX), last.toList())
        assertEquals(listOf(NepaliDate.MAX), last.datesWithInterval(1))
        assertEquals(1, last.weekendDays().size + last.workingDays().size)
    }
}
