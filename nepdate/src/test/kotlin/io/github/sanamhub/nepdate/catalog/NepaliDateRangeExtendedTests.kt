@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.NepaliDateRange
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * C# `Core/NepaliDateRangeExtendedTests.cs`, phase 2. The port's range is a list of days with month,
 * year, contains and size only (PARITY D-09). It has no empty range: `of` rejects an end before the
 * start, so every row built on an empty range is covered by that rule instead.
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

    // The Current* rows need today in Nepal time.
    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentMonth_StartIsFirstDayOfCurrentMonth() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentMonth_EndIsLastDayOfCurrentMonth() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentMonth_ContainsToday() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentFiscalYear_ContainsToday() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentFiscalYear_StartIsShrawan() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentFiscalYear_EndIsAshadh() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentCalendarYear_ContainsToday() {
        fail(NEEDS_TODAY)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun CurrentCalendarYear_StartIsBaishakh1() {
        fail(NEEDS_TODAY)
    }

    // Not ported, PARITY D-09 (no splitting, interval iteration, weekend or working-day helpers):
    // SplitByFiscalQuarter_EmptyRange_ReturnsEmptyArray, SplitByFiscalQuarter_RangeWithinOneQuarter_ReturnsSingleRange,
    // SplitByFiscalQuarter_RangeSpanningTwoQuarters_ReturnsTwoRanges, SplitByFiscalQuarter_FullFiscalYear_ReturnsFourRanges,
    // SplitByFiscalQuarter_RangeInQ4Months_SingleRange, DatesWithInterval_Zero_ThrowsArgumentOutOfRangeException,
    // DatesWithInterval_NegativeInterval_ThrowsArgumentOutOfRangeException, DatesWithInterval_IntervalOfOne_YieldsEveryDay,
    // DatesWithInterval_IntervalOfTwo_YieldsOddDays, DatesWithInterval_IntervalLargerThanRange_YieldsOnlyStartDate,
    // DatesWithInterval_EmptyRange_YieldsNothing, WeekendDays_IncludeSundayTrue_ReturnsBothSaturdayAndSunday,
    // WeekendDays_IncludeSundayFalse_ReturnsOnlySaturday, WeekendDays_RangeWithNoWeekend_ReturnsEmpty,
    // WeekendDays_EmptyRange_ReturnsEmpty, WorkingDays_ExcludeSundayFalse_ExcludesOnlySaturday,
    // WorkingDays_ExcludeSundayTrue_ExcludesBothSatAndSun, WorkingDays_EmptyRange_ReturnsEmpty,
    // FromDayCount_ZeroDays_ThrowsArgumentOutOfRangeException, FromDayCount_NegativeDays_ThrowsArgumentOutOfRangeException.

    // Not ported, PARITY D-09 (no set operations): Intersect_NonOverlappingRanges_ReturnsEmptyRange,
    // Intersect_AdjacentRanges_ReturnsEmptyRange, Intersect_OneRangeFullyInsideOther_ReturnsInnerRange,
    // Union_LeftRangeIsEmpty_ReturnsRightRange, Union_RightRangeIsEmpty_ReturnsLeftRange,
    // Union_NonOverlappingRanges_SpansEntireGap, IsAdjacentTo_GapBetweenRanges_ReturnsFalse,
    // IsAdjacentTo_OverlappingRanges_ReturnsFalse, IsAdjacentTo_EmptyRange_ReturnsFalse,
    // Except_NonOverlappingExcludeRange_ReturnsOriginalRange, Except_ExcludeRangeFullyContainsThis_ReturnsEmptyArray,
    // Except_ExcludeOverlapsStart_ReturnsRightPart, Except_ExcludeOverlapsEnd_ReturnsLeftPart.

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

    // Not ported, PARITY D-09 (no empty range): Contains_EmptyRange_ContainsNothing,
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

    // Not ported, PARITY D-09 (the range is a List; its toString is the list's): ToString_DefaultFormat_IsStartDashEnd,
    // ToString_EmptyRange_ReturnsEmptyRangeString, ToString_WithDateFormat_UsesSpecifiedFormat,
    // ToString_WithDateFormat_EmptyRange_ReturnsEmptyRangeString.

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
