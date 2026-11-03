@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.FiscalYear
import io.github.sanamhub.nepdate.Month
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.NepaliDateRange
import io.github.sanamhub.nepdate.Weekday
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * C# `Core/NepaliDateRangeTests.cs`. The port's range is never empty (PARITY D-01), so the C# cases
 * that build one are cases that must fail here.
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
        assertEquals(15, range.size)
    }

    // C# makes an empty range when the end is before the start; the port has no empty range
    // (PARITY D-01) and rejects it.
    @Test
    fun Constructor_EndBeforeStart_CreatesEmptyRange() {
        val error = assertFailsWith<NepDateException> { NepaliDateRange.of(d(2080, 1, 15), d(2080, 1, 1)) }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
    }

    @Test
    fun SingleDay_CreatesRangeWithOneDay() {
        val date = d(2080, 1, 1)
        val range = NepaliDateRange.singleDay(date)
        assertEquals(date, range.start)
        assertEquals(date, range.endInclusive)
        assertEquals(1, range.size)
    }

    @Test
    fun FromDayCount_CreatesCorrectRange() {
        val start = d(2080, 1, 1)
        val range = NepaliDateRange.fromDayCount(start, 10)
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

    @Test
    fun Contains_RangeFullyContained_ReturnsTrue() {
        val outer = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 30))
        val inner = NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 20))
        assertTrue(outer.containsRange(inner))
        assertFalse(inner.containsRange(outer))
    }

    @Test
    fun Overlaps_RangesOverlap_ReturnsTrue() {
        val first = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15))
        val second = NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 25))
        assertTrue(first.overlaps(second))
        assertTrue(second.overlaps(first))
    }

    @Test
    fun Overlaps_RangesDontOverlap_ReturnsFalse() {
        val first = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15))
        val second = NepaliDateRange.of(d(2080, 1, 16), d(2080, 1, 25))
        assertFalse(first.overlaps(second))
    }

    @Test
    fun IsAdjacentTo_AdjacentRanges_ReturnsTrue() {
        val first = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15))
        val second = NepaliDateRange.of(d(2080, 1, 16), d(2080, 1, 25))
        assertTrue(first.isAdjacentTo(second))
        assertTrue(second.isAdjacentTo(first))
        assertFalse(first.overlaps(second))
    }

    @Test
    fun Intersect_OverlappingRanges_ReturnsIntersection() {
        val first = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15))
        val second = NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 25))
        assertEquals(NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 15)), first.intersect(second))
    }

    @Test
    fun Union_OverlappingRanges_ReturnsUnion() {
        val first = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 15))
        val second = NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 25))
        assertEquals(NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 25)), first.union(second))
    }

    @Test
    fun Except_RemovingRangeFromMiddle_ReturnsTwoRanges() {
        val range = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 30))
        val removed = NepaliDateRange.of(d(2080, 1, 10), d(2080, 1, 20))
        assertEquals(
            listOf(
                NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 9)),
                NepaliDateRange.of(d(2080, 1, 21), d(2080, 1, 30)),
            ),
            range.minus(removed),
        )
    }

    @Test
    fun SplitByMonth_RangeAcrossMonths_SplitsCorrectly() {
        val range = NepaliDateRange.of(d(2080, 1, 15), d(2080, 2, 10))
        val pieces = range.splitByMonth()
        assertEquals(2, pieces.size)
        assertEquals(range.start, pieces.first().start)
        assertEquals(d(2080, 1, 1).lastDayOfMonth(), pieces.first().endInclusive)
        assertEquals(d(2080, 2, 1), pieces.last().start)
        assertEquals(range.endInclusive, pieces.last().endInclusive)
    }

    @Test
    fun WorkingDays_ReturnsCorrectDays() {
        val start = d(2080, 1, 1)
        val range = NepaliDateRange.fromDayCount(start, 14)
        assertEquals(range.toList().filter { it.dayOfWeek != Weekday.SATURDAY }, range.workingDays())
        assertEquals(range.workingDays().size, 14 - range.weekendDays(includeSunday = false).size)
    }

    @Test
    fun Enumeration_EnumeratesAllDaysInRange() {
        val days = NepaliDateRange.of(d(2080, 1, 1), d(2080, 1, 10)).toList()
        assertEquals((1..10).map { d(2080, 1, it) }, days)
    }

    @Test
    fun Demo_RangeOperations() {
        // The C# demo: a fiscal year, one month inside it, and what is left when the month goes.
        val fiscal = FiscalYear(2080).range()
        val month = NepaliDateRange.ofMonth(2080, Month.MAGH.value)
        assertTrue(fiscal.containsRange(month))
        val rest = fiscal.minus(month)
        assertEquals(2, rest.size)
        assertEquals(fiscal.start, rest.first().start)
        assertEquals(fiscal.endInclusive, rest.last().endInclusive)
        assertNull(rest.first().intersect(month))
        assertEquals(fiscal, rest.first().union(rest.last()).union(month))
    }
}
