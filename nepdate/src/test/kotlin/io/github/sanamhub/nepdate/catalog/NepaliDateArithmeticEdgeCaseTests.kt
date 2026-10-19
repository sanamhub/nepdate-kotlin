@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** C# `Core/NepaliDateArithmeticEdgeCaseTests.cs`. */
class NepaliDateArithmeticEdgeCaseTests {
    private fun ymd(date: NepaliDate) = Triple(date.year, date.monthValue, date.dayOfMonth)

    private fun lastOfChaitra2080() = NepaliDate.of(2080, 12, 1).lastDayOfMonth()

    @Test
    fun AddDays_Zero_ReturnsSameDate() {
        val date = NepaliDate.of(2080, 5, 15)
        assertEquals(date, date.plusDays(0))
    }

    @Test
    fun AddDays_PositiveAcrossMonthBoundary_IsCorrect() {
        assertEquals(Triple(2080, 6, 1), ymd(NepaliDate.of(2080, 5, 31).plusDays(1)))
    }

    @Test
    fun AddDays_NegativeAcrossMonthBoundary_IsCorrect() {
        assertEquals(Triple(2080, 5, 31), ymd(NepaliDate.of(2080, 6, 1).plusDays(-1)))
    }

    @Test
    fun AddDays_PositiveAcrossYearBoundary_IsCorrect() {
        assertEquals(Triple(2081, 1, 1), ymd(lastOfChaitra2080().plusDays(1)))
    }

    @Test
    fun AddDays_NegativeAcrossYearBoundary_IsCorrect() {
        assertEquals(lastOfChaitra2080(), NepaliDate.of(2081, 1, 1).plusDays(-1))
    }

    @Test
    fun AddDays_LargePositiveValue_IsCorrect() {
        val start = NepaliDate.of(2080, 1, 1)
        val result = start.plusDays(365)
        assertTrue(result.year == 2081 || result.year == 2080)
        assertTrue(result >= start)
    }

    @Test
    fun AddMonths_PositiveCrossesYearBoundary_IsCorrect() {
        assertEquals(Triple(2081, 4, 15), ymd(NepaliDate.of(2080, 11, 15).plusMonths(5)))
    }

    @Test
    fun AddMonths_NegativeCrossesYearBoundary_IsCorrect() {
        assertEquals(Triple(2079, 10, 15), ymd(NepaliDate.of(2080, 3, 15).plusMonths(-5)))
    }

    @Test
    fun AddMonths_PositiveMoreThan12_CrossesMultipleYears() {
        assertEquals(Triple(2081, 7, 15), ymd(NepaliDate.of(2080, 5, 15).plusMonths(14)))
    }

    @Test
    fun AddMonths_Exactly12_SameMonthNextYear() {
        assertEquals(Triple(2081, 6, 15), ymd(NepaliDate.of(2080, 6, 15).plusMonths(12)))
    }

    @Test
    fun AddMonths_DayCappedAtMonthEnd_WhenResultMonthIsShorter() {
        val result = NepaliDate.of(2081, 4, 32).plusMonths(5)
        assertEquals(2081, result.year)
        assertEquals(9, result.monthValue)
        assertTrue(result.dayOfMonth in 1..result.lengthOfMonth())
    }

    @Test
    fun AddMonths_Negative_SameDayPreservedWhenFits() {
        assertEquals(Triple(2080, 7, 10), ymd(NepaliDate.of(2080, 8, 10).plusMonths(-1)))
    }

    // Not ported, PARITY D-02 (plusMonths takes whole months; use plusDays for part of a month):
    // AddMonths_HalfMonth_AddsApproximately15Days, AddMonths_NegativeHalfMonth_SubtractsApproximately15Days,
    // AddMonths_OneAndHalfMonths_AddsApproximately46Days, AddMonths_FractionalValue_ReturnsDateFurtherThanZeroMonths.

    // C# builds from a DateTime with a time of day. ofGregorian takes only the date, so the time
    // can't change the result; these rows drop the time with LocalDateTime.toLocalDate().
    private fun fromDateTime(dateTime: LocalDateTime): NepaliDate {
        val date = dateTime.toLocalDate()
        return NepaliDate.ofGregorian(date.year, date.monthValue, date.dayOfMonth)
    }

    @Test
    fun Constructor_DateTimeWithMidnightTime_IgnoresTime() {
        assertEquals(Triple(2080, 5, 15), ymd(fromDateTime(LocalDateTime.of(2023, 9, 1, 0, 0, 0))))
    }

    @Test
    fun Constructor_DateTimeWithNoonTime_IgnoresTime() {
        assertEquals(Triple(2080, 5, 15), ymd(fromDateTime(LocalDateTime.of(2023, 9, 1, 12, 30, 45))))
    }

    @Test
    fun Constructor_DateTimeWithEndOfDayTime_IgnoresTime() {
        assertEquals(Triple(2080, 5, 15), ymd(fromDateTime(LocalDateTime.of(2023, 9, 1, 23, 59, 59))))
    }

    @Test
    fun Constructor_TwoDateTimesOnSameEnglishDateDifferentTimes_ProduceSameNepaliDate() {
        assertEquals(fromDateTime(LocalDateTime.of(2023, 1, 1, 6, 0, 0)), fromDateTime(LocalDateTime.of(2023, 1, 1, 22, 0, 0)))
    }

    // Not ported, PARITY D-06 (no day, month and year swapping or two-digit year expansion):
    // TryParse_AutoAdjust_DayYearMonthFormat_ReturnsCorrectDate, TryParse_AutoAdjust_MonthDayYearInvertedOrder_ReturnsCorrectDate,
    // TryParse_AutoAdjust_TwoDigitYear_ExpandsToCurrentMillennium, TryParse_AutoAdjust_InvalidDateAfterAdjustment_ReturnsFalse.

    @Test
    fun TryParse_AutoAdjust_ValidStandardFormat_ReturnsCorrectDate() {
        // Without auto-adjusting this is a plain strict parse.
        assertEquals(NepaliDate.of(2080, 5, 15), NepaliDate.parseOrNull("2080/05/15"))
    }
}
