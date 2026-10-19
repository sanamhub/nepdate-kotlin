@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.FiscalYear
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.NepaliDateRange
import io.github.sanamhub.nepdate.Weekday
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * C# `Core/NepaliDateRangeTests.cs`. The port's range is a list of days with month, year,
 * contains and size only (PARITY D-09): no empty ranges, set operations, splitting or working days.
 */
class NepaliDateRangeTests {
    private fun d(
        y: Int,
        m: Int,
        day: Int,
    ) = NepaliDate.of(y, m, day)

    @Test
    fun Constructor_ValidDates_CreatesRange() {
        val range = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15))
        assertEquals(d(2080, 1, 1), range.start)
        assertEquals(d(2080, 1, 15), range.endInclusive)
        assertFalse(range.isEmpty())
        assertEquals(15, range.size)
    }

    // C# makes an empty range when the end is before the start; the port has no empty range
    // (PARITY D-09) and rejects it.
    @Test
    fun Constructor_EndBeforeStart_CreatesEmptyRange() {
        val error = assertFailsWith<NepDateException> { NepaliDateRange.of(d(2080, 1, 15), d(2080, 1, 1)) }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
    }

    @Test
    fun SingleDay_CreatesRangeWithOneDay() {
        val date = d(2080, 1, 1)
        val range = NepaliDateRange.of(date, date)
        assertEquals(date, range.start)
        assertEquals(date, range.endInclusive)
        assertEquals(1, range.size)
    }

    // No FromDayCount (PARITY D-09): of(start, start.plusDays(days - 1)) builds the same range.
    @Test
    fun FromDayCount_CreatesCorrectRange() {
        val start = d(2080, 1, 1)
        val range = NepaliDateRange.of(start, start.plusDays(10 - 1))
        assertEquals(start, range.start)
        assertEquals(start.plusDays(9), range.endInclusive)
        assertEquals(10, range.size)
    }

    @Test
    fun ForMonth_CreatesCorrectRange() {
        val range = NepaliDateRange.ofMonth(2080, 1)
        assertEquals(d(2080, 1, 1), range.start)
        assertEquals(d(2080, 1, 1).lastDayOfMonth(), range.endInclusive)
    }

    @Test
    fun ForFiscalYear_CreatesCorrectRange() {
        val range = FiscalYear(2080).range()
        assertEquals(d(2080, 4, 1), range.start)
        assertEquals(d(2081, 3, 1).lastDayOfMonth(), range.endInclusive)
    }

    @Test
    fun Contains_DateInRange_ReturnsTrue() {
        assertTrue(NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15)).contains(d(2080, 1, 10)))
    }

    @Test
    fun Contains_DateOutsideRange_ReturnsFalse() {
        assertFalse(NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15)).contains(d(2080, 1, 20)))
    }

    // No Contains(range) (PARITY D-09); List.containsAll answers the same question.
    @Test
    fun Contains_RangeFullyContained_ReturnsTrue() {
        val outer = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 30))
        val inner = NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 20))
        assertTrue(outer.containsAll(inner))
    }

    // Not ported, PARITY D-09 (no set operations or splitting): Overlaps_RangesOverlap_ReturnsTrue,
    // Overlaps_RangesDontOverlap_ReturnsFalse, IsAdjacentTo_AdjacentRanges_ReturnsTrue,
    // Intersect_OverlappingRanges_ReturnsIntersection, Union_OverlappingRanges_ReturnsUnion,
    // Except_RemovingRangeFromMiddle_ReturnsTwoRanges, SplitByMonth_RangeAcrossMonths_SplitsCorrectly,
    // Demo_RangeOperations.

    // No WorkingDays (PARITY D-09: "iterate and filter for working days"); the row filters.
    @Test
    fun WorkingDays_ReturnsCorrectDays() {
        val start = d(2080, 1, 1)
        val range = NepaliDateRange.of(start, start.plusDays(13))
        val expectedSaturdays = (0 until 14).count { (start.dayOfWeek.ordinal + it) % 7 == Weekday.SATURDAY.ordinal }
        val working = range.filter { it.dayOfWeek != Weekday.SATURDAY }
        assertEquals(14 - expectedSaturdays, working.size)
    }

    @Test
    fun Enumeration_EnumeratesAllDaysInRange() {
        val days = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 10)).toList()
        assertEquals((1..10).map { d(2080, 1, it) }, days)
    }
}
