@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.DateOrder
import io.github.sanamhub.nepdate.FiscalYear
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.Month
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.NepaliDateRange
import io.github.sanamhub.nepdate.Separator
import io.github.sanamhub.nepdate.Weekday
import io.github.sanamhub.nepdate.nepaliToday
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * C# `Core/NepaliDateRangeExtendedTests.cs`. The port's range has no empty range: `of` rejects an
 * end before the start (PARITY D-01), so every case built on an empty range is covered by that
 * rule instead. The rest of the C# range surface is in `RangeVectorTest` and its siblings.
 */
class NepaliDateRangeExtendedTests {
    private fun d(
        y: Int,
        m: Int,
        day: Int,
    ) = NepaliDate.of(y, m, day)

    private fun range(
        from: NepaliDate,
        to: NepaliDate,
    ) = NepaliDateRange.of(from, to)

    @Test
    fun ForCalendarYear_StartsAtBaishakhOne_EndsAtChaitraEnd() {
        val year = NepaliDateRange.ofYear(2080)
        assertEquals(d(2080, 1, 1), year.start)
        assertEquals(d(2080, 12, 1).lastDayOfMonth(), year.endInclusive)
    }

    @Test
    fun ForCalendarYear_LengthIsReasonable() {
        assertTrue(NepaliDateRange.ofYear(2080).size in 365..366)
    }

    // The Current* rows use today in Nepal time (nepaliToday).
    @Test
    fun CurrentMonth_StartIsFirstDayOfCurrentMonth() {
        val today = nepaliToday()
        assertEquals(today.firstDayOfMonth(), NepaliDateRange.ofMonth(today.year, today.monthValue).start)
    }

    @Test
    fun CurrentMonth_EndIsLastDayOfCurrentMonth() {
        val today = nepaliToday()
        assertEquals(today.lastDayOfMonth(), NepaliDateRange.ofMonth(today.year, today.monthValue).endInclusive)
    }

    @Test
    fun CurrentMonth_ContainsToday() {
        val today = nepaliToday()
        assertTrue(NepaliDateRange.ofMonth(today.year, today.monthValue).contains(today))
    }

    @Test
    fun CurrentFiscalYear_ContainsToday() {
        val today = nepaliToday()
        assertTrue(today.fiscalYear.range().contains(today))
    }

    @Test
    fun CurrentFiscalYear_StartIsShrawan() {
        assertEquals(Month.SHRAWAN, nepaliToday().fiscalYear.start().month)
    }

    @Test
    fun CurrentFiscalYear_EndIsAshadh() {
        assertEquals(Month.ASHAD, nepaliToday().fiscalYear.end().month)
    }

    @Test
    fun CurrentCalendarYear_ContainsToday() {
        val today = nepaliToday()
        assertTrue(NepaliDateRange.ofYear(today.year).contains(today))
    }

    @Test
    fun CurrentCalendarYear_StartIsBaishakh1() {
        val today = nepaliToday()
        assertEquals(NepaliDate.of(today.year, 1, 1), NepaliDateRange.ofYear(today.year).start)
    }

    @Test
    fun SplitByFiscalQuarter_RangeWithinOneQuarter_ReturnsSingleRange() {
        val range = range(d(2080, 4, 10), d(2080, 4, 20))
        assertEquals(listOf(range), range.splitByFiscalQuarter())
    }

    @Test
    fun SplitByFiscalQuarter_RangeSpanningTwoQuarters_ReturnsTwoRanges() {
        val pieces = range(d(2080, 6, 10), d(2080, 7, 20)).splitByFiscalQuarter()
        assertEquals(2, pieces.size)
        assertEquals(d(2080, 6, 10), pieces.first().start)
        assertEquals(d(2080, 6, 30), pieces.first().endInclusive)
        assertEquals(d(2080, 7, 1), pieces.last().start)
        assertEquals(d(2080, 7, 20), pieces.last().endInclusive)
    }

    @Test
    fun SplitByFiscalQuarter_FullFiscalYear_ReturnsFourRanges() {
        assertEquals(4, FiscalYear(2080).range().splitByFiscalQuarter().size)
    }

    @Test
    fun SplitByFiscalQuarter_RangeInQ4Months_SingleRange() {
        // Baishakh to Ashad of the next BS year is one quarter, Q4.
        val range = range(d(2081, 1, 10), d(2081, 3, 20))
        assertEquals(listOf(range), range.splitByFiscalQuarter())
    }

    private fun intervalError(interval: Int) =
        assertEquals(
            NepDateException.Kind.OUT_OF_RANGE,
            assertFailsWith<NepDateException> { range(d(2080, 1, 1), d(2080, 1, 10)).datesWithInterval(interval) }.kind,
        )

    @Test
    fun DatesWithInterval_Zero_ThrowsArgumentOutOfRangeException() {
        intervalError(0)
    }

    @Test
    fun DatesWithInterval_NegativeInterval_ThrowsArgumentOutOfRangeException() {
        intervalError(-1)
    }

    @Test
    fun DatesWithInterval_IntervalOfOne_YieldsEveryDay() {
        assertEquals(range(d(2080, 1, 1), d(2080, 1, 10)).toList(), range(d(2080, 1, 1), d(2080, 1, 10)).datesWithInterval(1))
    }

    @Test
    fun DatesWithInterval_IntervalOfTwo_YieldsOddDays() {
        val days = range(d(2080, 1, 1), d(2080, 1, 10)).datesWithInterval(2)
        assertEquals(listOf(1, 3, 5, 7, 9).map { d(2080, 1, it) }, days)
    }

    @Test
    fun DatesWithInterval_IntervalLargerThanRange_YieldsOnlyStartDate() {
        assertEquals(listOf(d(2080, 1, 1)), range(d(2080, 1, 1), d(2080, 1, 10)).datesWithInterval(30))
    }

    @Test
    fun WeekendDays_IncludeSundayTrue_ReturnsBothSaturdayAndSunday() {
        val weekend = range(d(2080, 1, 1), d(2080, 1, 14)).weekendDays(includeSunday = true)
        assertTrue(weekend.all { it.dayOfWeek == Weekday.SATURDAY || it.dayOfWeek == Weekday.SUNDAY })
        assertTrue(weekend.any { it.dayOfWeek == Weekday.SATURDAY })
        assertTrue(weekend.any { it.dayOfWeek == Weekday.SUNDAY })
    }

    @Test
    fun WeekendDays_IncludeSundayFalse_ReturnsOnlySaturday() {
        val weekend = range(d(2080, 1, 1), d(2080, 1, 14)).weekendDays(includeSunday = false)
        assertTrue(weekend.all { it.dayOfWeek == Weekday.SATURDAY })
    }

    @Test
    fun WorkingDays_ExcludeSundayFalse_ExcludesOnlySaturday() {
        val range = range(d(2080, 1, 1), d(2080, 1, 14))
        val working = range.workingDays(excludeSunday = false)
        assertTrue(working.none { it.dayOfWeek == Weekday.SATURDAY })
        assertTrue(working.any { it.dayOfWeek == Weekday.SUNDAY })
        assertEquals(range.size - range.weekendDays(includeSunday = false).size, working.size)
    }

    @Test
    fun WorkingDays_ExcludeSundayTrue_ExcludesBothSatAndSun() {
        val range = range(d(2080, 1, 1), d(2080, 1, 14))
        val working = range.workingDays(excludeSunday = true)
        assertTrue(working.none { it.dayOfWeek == Weekday.SATURDAY || it.dayOfWeek == Weekday.SUNDAY })
        assertEquals(range.size - range.weekendDays(includeSunday = true).size, working.size)
    }

    private fun dayCountError(days: Int) =
        assertEquals(
            NepDateException.Kind.OUT_OF_RANGE,
            assertFailsWith<NepDateException> { NepaliDateRange.fromDayCount(d(2080, 1, 1), days) }.kind,
        )

    @Test
    fun FromDayCount_ZeroDays_ThrowsArgumentOutOfRangeException() {
        dayCountError(0)
    }

    @Test
    fun FromDayCount_NegativeDays_ThrowsArgumentOutOfRangeException() {
        dayCountError(-1)
    }

    @Test
    fun Intersect_NonOverlappingRanges_ReturnsEmptyRange() {
        assertNull(range(d(2080, 1, 1), d(2080, 1, 10)).intersect(range(d(2080, 2, 1), d(2080, 2, 10))))
    }

    @Test
    fun Intersect_AdjacentRanges_ReturnsEmptyRange() {
        assertNull(range(d(2080, 1, 1), d(2080, 1, 10)).intersect(range(d(2080, 1, 11), d(2080, 1, 20))))
    }

    @Test
    fun Intersect_OneRangeFullyInsideOther_ReturnsInnerRange() {
        val inner = range(d(2080, 1, 5), d(2080, 1, 10))
        assertEquals(inner, range(d(2080, 1, 1), d(2080, 1, 20)).intersect(inner))
    }

    @Test
    fun Union_NonOverlappingRanges_SpansEntireGap() {
        assertEquals(
            range(d(2080, 1, 1), d(2080, 2, 10)),
            range(d(2080, 1, 1), d(2080, 1, 10)).union(range(d(2080, 2, 1), d(2080, 2, 10))),
        )
    }

    @Test
    fun IsAdjacentTo_GapBetweenRanges_ReturnsFalse() {
        assertFalse(range(d(2080, 1, 1), d(2080, 1, 10)).isAdjacentTo(range(d(2080, 1, 12), d(2080, 1, 20))))
    }

    @Test
    fun IsAdjacentTo_OverlappingRanges_ReturnsFalse() {
        assertFalse(range(d(2080, 1, 1), d(2080, 1, 10)).isAdjacentTo(range(d(2080, 1, 10), d(2080, 1, 20))))
    }

    @Test
    fun Except_NonOverlappingExcludeRange_ReturnsOriginalRange() {
        val range = range(d(2080, 1, 1), d(2080, 1, 10))
        assertEquals(listOf(range), range.minus(range(d(2080, 2, 1), d(2080, 2, 10))))
    }

    @Test
    fun Except_ExcludeRangeFullyContainsThis_ReturnsEmptyArray() {
        assertEquals(emptyList(), range(d(2080, 1, 5), d(2080, 1, 10)).minus(range(d(2080, 1, 1), d(2080, 1, 20))))
    }

    @Test
    fun Except_ExcludeOverlapsStart_ReturnsRightPart() {
        assertEquals(
            listOf(range(d(2080, 1, 6), d(2080, 1, 10))),
            range(d(2080, 1, 1), d(2080, 1, 10)).minus(range(d(2080, 1, 1), d(2080, 1, 5))),
        )
    }

    @Test
    fun Except_ExcludeOverlapsEnd_ReturnsLeftPart() {
        assertEquals(
            listOf(range(d(2080, 1, 1), d(2080, 1, 5))),
            range(d(2080, 1, 1), d(2080, 1, 10)).minus(range(d(2080, 1, 6), d(2080, 1, 20))),
        )
    }

    private val fifthToTwentieth = range(d(2080, 1, 5), d(2080, 1, 20))

    @Test
    fun Contains_ExactStartDate_ReturnsTrue() {
        assertTrue(fifthToTwentieth.contains(d(2080, 1, 5)))
    }

    @Test
    fun Contains_ExactEndDate_ReturnsTrue() {
        assertTrue(fifthToTwentieth.contains(d(2080, 1, 20)))
    }

    @Test
    fun Contains_OneDayBeforeStart_ReturnsFalse() {
        assertFalse(fifthToTwentieth.contains(d(2080, 1, 4)))
    }

    @Test
    fun Contains_OneDayAfterEnd_ReturnsFalse() {
        assertFalse(fifthToTwentieth.contains(d(2080, 1, 21)))
    }

    // Not ported, PARITY D-01 (no empty range): Contains_EmptyRange_ContainsNothing,
    // GetEnumerator_EmptyRange_YieldsNoDates, Equals_BothEmpty_ReturnsTrue.

    @Test
    fun GetEnumerator_ThreeDayRange_YieldsExactlyThreeDates() {
        val dates = mutableListOf<NepaliDate>()
        for (date in range(d(2080, 5, 13), d(2080, 5, 15))) dates.add(date)
        assertEquals(listOf(d(2080, 5, 13), d(2080, 5, 14), d(2080, 5, 15)), dates)
    }

    @Test
    fun GetEnumerator_SingleDayRange_YieldsOneDate() {
        assertEquals(listOf(d(2080, 5, 15)), range(d(2080, 5, 15), d(2080, 5, 15)).toList())
    }

    // The empty C# range is not ported (PARITY D-01), so the two empty-range string cases have no
    // port row. The two non-empty ones are in RangeFormatVectorTest and here.

    @Test
    fun ToString_DefaultFormat_IsStartDashEnd() {
        assertEquals("2080/01/01 - 2080/01/15", firstToFifteenth.toString())
    }

    @Test
    fun ToString_WithDateFormat_UsesSpecifiedFormat() {
        assertEquals(
            "01-01-2080 - 15-01-2080",
            firstToFifteenth.toFormattedString(DateOrder.DMY, Separator.DASH, Lang.ENGLISH),
        )
    }

    private val firstToFifteenth = range(d(2080, 1, 1), d(2080, 1, 15))

    @Test
    fun Equals_IdenticalRanges_ReturnsTrue() {
        assertTrue(firstToFifteenth.equals(range(d(2080, 1, 1), d(2080, 1, 15))))
    }

    @Test
    fun Equals_DifferentStart_ReturnsFalse() {
        assertFalse(firstToFifteenth.equals(range(d(2080, 1, 2), d(2080, 1, 15))))
    }

    @Test
    fun Equals_DifferentEnd_ReturnsFalse() {
        assertFalse(firstToFifteenth.equals(range(d(2080, 1, 1), d(2080, 1, 16))))
    }

    @Test
    fun Equals_ObjectOverload_SameRange_ReturnsTrue() {
        val other: Any = range(d(2080, 1, 1), d(2080, 1, 15))
        assertTrue(firstToFifteenth.equals(other))
    }

    @Test
    fun Equals_ObjectOverload_WrongType_ReturnsFalse() {
        assertFalse(firstToFifteenth.equals("not a range"))
    }

    @Test
    fun GetHashCode_EqualRanges_SameHash() {
        assertEquals(firstToFifteenth.hashCode(), range(d(2080, 1, 1), d(2080, 1, 15)).hashCode())
    }

    @Test
    fun OperatorEquals_IdenticalRanges_ReturnsTrue() {
        val other = range(d(2080, 1, 1), d(2080, 1, 15))
        assertTrue(firstToFifteenth == other)
        assertFalse(firstToFifteenth != other)
    }

    @Test
    fun OperatorNotEquals_DifferentRanges_ReturnsTrue() {
        val other = range(d(2080, 1, 1), d(2080, 1, 16))
        assertNotEquals(firstToFifteenth, other)
        assertTrue(firstToFifteenth != other)
    }
}
