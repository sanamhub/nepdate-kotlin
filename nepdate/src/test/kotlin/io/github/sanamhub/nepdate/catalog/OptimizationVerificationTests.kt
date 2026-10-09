@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import java.time.Year
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * C# `Core/OptimizationVerificationTests.cs`, the phase 1 methods (prefixes Constructor, CompareTo,
 * Equals, GetHashCode, Operator, DayOfWeek, EnglishDate, MonthEndDay, IsDefault, IsLeapYear) and the
 * phase 2 methods (prefix Subtraction).
 */
class OptimizationVerificationTests {
    private fun local(date: NepaliDate): LocalDate = LocalDate.of(date.gregorianYear, date.gregorianMonth, date.gregorianDay)

    // Built with `of` until K3-04: parse(input) for each separator, including । and |.
    @ParameterizedTest
    @ValueSource(
        strings = ["2080/05/15", "2080-05-15", "2080.05.15", "2080_05_15", """2080\05\15""", "2080 05 15", "2080।05।15", "2080|05|15"],
    )
    fun Constructor_AllSeparators_ParseCorrectly(input: String) {
        assertEquals(10, input.length)
        val date = NepaliDate.of(2080, 5, 15) // K3-04: NepaliDate.parse(input)
        assertEquals(Triple(2080, 5, 15), Triple(date.year, date.monthValue, date.dayOfMonth))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "2080/05", "2080", "2080/05/15/99", "abc/def/ghi"])
    @Disabled(NEEDS_PARSE)
    fun Constructor_InvalidStrings_ThrowsException(input: String) {
        fail("$NEEDS_PARSE: $input")
    }

    @Test
    @Disabled(NEEDS_PARSE)
    fun Constructor_NullString_ThrowsException() {
        // Kotlin callers can't pass null; K3-04 checks that a Java caller's null is rejected.
        fail(NEEDS_PARSE)
    }

    @ParameterizedTest
    @CsvSource("2080/5/15, 2080, 5, 15", "2080/05/5, 2080, 5, 5", "2080/1/1, 2080, 1, 1")
    fun Constructor_WithoutLeadingZeros_ParsesCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertTrue(input.startsWith("$year/"))
        val date = NepaliDate.of(year, month, day) // K3-04: NepaliDate.parse(input)
        assertEquals(Triple(year, month, day), Triple(date.year, date.monthValue, date.dayOfMonth))
    }

    private val earlier = NepaliDate.of(2080, 5, 15)
    private val later = NepaliDate.of(2080, 5, 16)

    @Test
    fun Operator_Equality_SameDates_ReturnsTrue() {
        val b = NepaliDate.of(2080, 5, 15)
        assertTrue(earlier == b)
        assertFalse(earlier != b)
    }

    @Test
    fun Operator_Inequality_DifferentDates_ReturnsTrue() {
        assertTrue(earlier != later)
        assertFalse(earlier == later)
    }

    @Test
    fun Operator_LessThan_EarlierDate_ReturnsTrue() {
        val same = earlier
        assertTrue(earlier < later)
        assertFalse(later < earlier)
        assertFalse(earlier < same)
    }

    @Test
    fun Operator_LessThanOrEqual_SameAndEarlier_ReturnsTrue() {
        val same = earlier
        assertTrue(earlier <= later)
        assertTrue(earlier <= same)
        assertFalse(later <= earlier)
    }

    @Test
    fun Operator_GreaterThan_LaterDate_ReturnsTrue() {
        val same = later
        assertTrue(later > earlier)
        assertFalse(earlier > later)
        assertFalse(later > same)
    }

    @Test
    fun Operator_GreaterThanOrEqual_SameAndLater_ReturnsTrue() {
        val same = later
        assertTrue(later >= earlier)
        assertTrue(later >= same)
        assertFalse(earlier >= later)
    }

    @Test
    fun Operator_MinMaxBoundary_CorrectOrdering() {
        assertTrue(NepaliDate.MIN < NepaliDate.MAX)
        assertTrue(NepaliDate.MAX > NepaliDate.MIN)
        assertTrue(NepaliDate.MIN <= NepaliDate.MAX)
        assertTrue(NepaliDate.MAX >= NepaliDate.MIN)
    }

    @Test
    fun CompareTo_EarlierDate_ReturnsNegative() {
        assertTrue(earlier.compareTo(later) < 0)
    }

    @Test
    fun CompareTo_SameDate_ReturnsZero() {
        assertEquals(0, earlier.compareTo(earlier))
    }

    @Test
    fun CompareTo_LaterDate_ReturnsPositive() {
        assertTrue(later.compareTo(earlier) > 0)
    }

    @Test
    fun CompareTo_DifferentYears_CorrectOrdering() {
        assertTrue(NepaliDate.of(2080, 1, 1) < NepaliDate.of(2081, 1, 1))
    }

    @Test
    fun CompareTo_DifferentMonths_CorrectOrdering() {
        assertTrue(NepaliDate.of(2080, 1, 1) < NepaliDate.of(2080, 2, 1))
    }

    @Test
    fun Equals_SameDates_ReturnsTrue() {
        val b = NepaliDate.of(2080, 5, 15)
        assertTrue(earlier.equals(b))
        assertTrue(earlier.equals(b as Any))
    }

    @Test
    fun Equals_DifferentDates_ReturnsFalse() {
        assertFalse(earlier.equals(later))
    }

    @Test
    fun GetHashCode_EqualDates_SameHashCode() {
        assertEquals(earlier.hashCode(), NepaliDate.of(2080, 5, 15).hashCode())
    }

    @Test
    fun GetHashCode_StableAcrossMultipleCalls() {
        assertEquals(earlier.hashCode(), earlier.hashCode())
    }

    // C# IsLeapYear() asks whether the date's AD year is a leap year. docs/API.md has no such member,
    // so the row checks the same fact on gregorianYear. Like C#, it ignores the expected column,
    // whose first row (2080 -> AD 2023) is wrong.
    @ParameterizedTest
    @CsvSource("2080, 1, 1, true", "2081, 1, 1, true", "2079, 1, 1, false")
    fun IsLeapYear_ChecksEnglishYearCorrectly(
        year: Int,
        month: Int,
        day: Int,
        @Suppress("UNUSED_PARAMETER", "unused") expected: Boolean,
    ) {
        val date = NepaliDate.of(year, month, day)
        val engYear = date.gregorianYear
        val expectedFromEng = engYear % 4 == 0 && (engYear % 100 != 0 || engYear % 400 == 0)
        assertEquals(expectedFromEng, Year.isLeap(engYear.toLong()))
        assertEquals(local(date).isLeapYear, expectedFromEng)
    }

    @ParameterizedTest
    @CsvSource("2080, 0, 15", "2080, 13, 15", "2080, 5, 0", "2080, 5, 33", "1900, 1, 1", "2200, 1, 1")
    fun Constructor_InvalidYearMonthDay_ThrowsException(
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertFailsWith<NepDateException> { NepaliDate.of(year, month, day) }
    }

    @Test
    fun Constructor_MinValue_IsValid() {
        assertEquals(Triple(1901, 1, 1), Triple(NepaliDate.MIN.year, NepaliDate.MIN.monthValue, NepaliDate.MIN.dayOfMonth))
    }

    @Test
    fun Constructor_MaxValue_IsValid() {
        val max = NepaliDate.MAX
        assertEquals(2199, max.year)
        assertEquals(12, max.monthValue)
        assertTrue(max.dayOfMonth in 29..32)
    }

    @Test
    fun EnglishDate_MinValue_ConvertsAndRoundtrips() {
        val eng = local(NepaliDate.MIN)
        assertEquals(NepaliDate.MIN, NepaliDate.ofGregorian(eng.year, eng.monthValue, eng.dayOfMonth))
    }

    @Test
    fun EnglishDate_MaxValue_ConvertsAndRoundtrips() {
        val eng = local(NepaliDate.MAX)
        assertEquals(NepaliDate.MAX, NepaliDate.ofGregorian(eng.year, eng.monthValue, eng.dayOfMonth))
    }

    @Test
    fun EnglishDate_JustBeforeMin_ThrowsException() {
        val before = local(NepaliDate.MIN).minusDays(1)
        assertFailsWith<NepDateException> { NepaliDate.ofGregorian(before.year, before.monthValue, before.dayOfMonth) }
    }

    @Test
    fun EnglishDate_JustAfterMax_ThrowsException() {
        val after = local(NepaliDate.MAX).plusDays(1)
        assertFailsWith<NepDateException> { NepaliDate.ofGregorian(after.year, after.monthValue, after.dayOfMonth) }
    }

    @Test
    fun MonthEndDay_CreatingDateWithExactEndDay_Succeeds() {
        for (year in 2070..2090) {
            for (month in 1..12) {
                val endDay = NepaliDate.of(year, month, 1).lengthOfMonth()
                assertEquals(endDay, NepaliDate.of(year, month, endDay).dayOfMonth)
                assertFailsWith<NepDateException> { NepaliDate.of(year, month, endDay + 1) }
            }
        }
    }

    @Test
    fun DayOfWeek_MatchesEnglishDateDayOfWeek() {
        for (year in 1910..2190 step 30) {
            for (month in 1..12 step 3) {
                val date = NepaliDate.of(year, month, 15)
                // java.time numbers Monday 1 to Sunday 7; Weekday is Sunday first.
                val expected = Weekday.entries[local(date).dayOfWeek.value % 7]
                assertEquals(expected, date.dayOfWeek)
            }
        }
    }

    // C# subtracts dates into a TimeSpan; the port's signed day count is daysUntil (PARITY D-07).
    @Test
    fun Subtraction_SameDate_ReturnsZeroDays() {
        assertEquals(0L, earlier.daysUntil(earlier))
    }

    @Test
    fun Subtraction_AdjacentDays_ReturnsOneDay() {
        assertEquals(1L, earlier.daysUntil(later))
        assertEquals(-1L, later.daysUntil(earlier))
    }

    @Test
    fun Subtraction_CrossMonth_CorrectDays() {
        assertEquals(1L, NepaliDate.of(2080, 4, 32).daysUntil(NepaliDate.of(2080, 5, 1)))
    }

    @Test
    fun Subtraction_CrossYear_CorrectDays() {
        val lastOfPreviousYear = NepaliDate.of(2080, 12, 1).lastDayOfMonth()
        assertEquals(1L, lastOfPreviousYear.daysUntil(NepaliDate.of(2081, 1, 1)))
    }

    // IsDefault_DefaultStruct_ReturnsTrue and IsDefault_ValidDate_ReturnsFalse: not ported, PARITY D-01
    // (there is no default or invalid NepaliDate to ask about).
}
