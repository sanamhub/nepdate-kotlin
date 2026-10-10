@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.DateOrder
import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Separator
import io.github.sanamhub.nepdate.Weekday
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * C# `Core/OptimizationVerificationTests.cs`, the phase 1 methods (prefixes Constructor, CompareTo,
 * Equals, GetHashCode, Operator, DayOfWeek, EnglishDate, MonthEndDay, IsDefault, IsLeapYear), the
 * phase 2 methods (prefix Subtraction) and the phase 3 methods (ToString, ToLongDateString,
 * ToUnicodeString, UnicodeConversion, SmartDateParser).
 */
class OptimizationVerificationTests {
    private fun local(date: NepaliDate): LocalDate = LocalDate.of(date.gregorianYear, date.gregorianMonth, date.gregorianDay)

    @ParameterizedTest
    @ValueSource(
        strings = ["2080/05/15", "2080-05-15", "2080.05.15", "2080_05_15", """2080\05\15""", "2080 05 15", "2080।05।15", "2080|05|15"],
    )
    fun Constructor_AllSeparators_ParseCorrectly(input: String) {
        val date = NepaliDate.parse(input)
        assertEquals(Triple(2080, 5, 15), Triple(date.year, date.monthValue, date.dayOfMonth))
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "2080/05", "2080", "2080/05/15/99", "abc/def/ghi"])
    fun Constructor_InvalidStrings_ThrowsException(input: String) {
        assertFailsWith<NepDateException> { NepaliDate.parse(input) }
    }

    @Test
    fun Constructor_NullString_ThrowsException() {
        // Kotlin can't pass null to parse; a Java caller that does gets NullPointerException.
        assertFailsWith<NullPointerException> { parseFromJava("parse", null) }
    }

    @ParameterizedTest
    @CsvSource("2080/5/15, 2080, 5, 15", "2080/05/5, 2080, 5, 5", "2080/1/1, 2080, 1, 1")
    fun Constructor_WithoutLeadingZeros_ParsesCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        val date = NepaliDate.parse(input)
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

    // IsLeapYear_ChecksEnglishYearCorrectly #1..#3: not ported, PARITY D-12 (C#'s IsLeapYear() is the
    // leap-ness of the AD year, which changes in the middle of a BS year; the port has no such member).

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

    // ---- Phase 3: formatting and lenient parsing (C#'s SmartDateParser is parseLenient) ----

    @ParameterizedTest
    @CsvSource(
        "2080, 5, 15, 2080/05/15",
        "2080, 1, 1, 2080/01/01",
        "2080, 12, 30, 2080/12/30",
        "1901, 1, 1, 1901/01/01",
        "2199, 12, 30, 2199/12/30",
        "2000, 6, 9, 2000/06/09",
        "2100, 10, 29, 2100/10/29",
    )
    fun ToString_ProducesExactFormat(
        year: Int,
        month: Int,
        day: Int,
        expected: String,
    ) {
        assertEquals(expected, NepaliDate.of(year, month, day).toString())
    }

    @Test
    fun ToString_OutputIsExactly10Characters() {
        val text = earlier.toString()
        assertEquals(10, text.length)
        assertEquals('/', text[4])
        assertEquals('/', text[7])
    }

    @Test
    fun ToString_AllMonthsZeroPadded() {
        for (month in 1..12) assertEquals(month.toString().padStart(2, '0'), NepaliDate.of(2080, month, 1).toString().substring(5, 7))
    }

    @Test
    fun ToString_AllDaysZeroPadded() {
        val end = NepaliDate.of(2080, 1, 1).lengthOfMonth()
        for (day in 1..end) assertEquals(day.toString().padStart(2, '0'), NepaliDate.of(2080, 1, day).toString().substring(8, 10))
    }

    @Test
    fun ToString_RoundtripsWithConstructor() {
        for (year in 1901..2199 step 50) {
            for (month in 1..12) {
                val date = NepaliDate.of(year, month, 1)
                assertEquals(date, NepaliDate.parse(date.toString()))
            }
        }
    }

    // C#'s DateFormats and Separators are DateOrder and Separator; the third column is pad.
    @ParameterizedTest
    @CsvSource(
        "YMD, SLASH, true, 2080/05/15",
        "YMD, DASH, true, 2080-05-15",
        "YMD, DOT, true, 2080.05.15",
        "YMD, UNDERSCORE, true, 2080_05_15",
        "YMD, SPACE, true, '2080 05 15'",
        "YMD, BACKSLASH, true, 2080\\05\\15",
        "DMY, SLASH, true, 15/05/2080",
        "DMY, DASH, true, 15-05-2080",
        "MDY, SLASH, true, 05/15/2080",
        "YDM, SLASH, true, 2080/15/05",
        "MYD, SLASH, true, 05/2080/15",
        "DYM, SLASH, true, 15/2080/05",
    )
    fun ToString_WithFormatAndSeparator_ProducesCorrectOutput(
        order: DateOrder,
        separator: Separator,
        pad: Boolean,
        expected: String,
    ) {
        assertEquals(expected, earlier.toShortString(order, separator, pad))
    }

    @ParameterizedTest
    @CsvSource("YMD, SLASH, false, 2080/5/15", "DMY, DASH, false, 15-5-2080", "MDY, DOT, false, 5.15.2080")
    fun ToString_WithoutLeadingZeros_ProducesCorrectOutput(
        order: DateOrder,
        separator: Separator,
        pad: Boolean,
        expected: String,
    ) {
        assertEquals(expected, earlier.toShortString(order, separator, pad))
    }

    // C#'s ToUnicodeString is toShortString(lang = NEPALI).
    @Test
    fun ToUnicodeString_DefaultFormat_ConvertsAllDigits() {
        assertEquals("२०८०/०५/१५", earlier.toShortString(lang = Lang.NEPALI))
    }

    @Test
    fun ToUnicodeString_EachDigitConvertsCorrectly() {
        val unicode = NepaliDate.of(1902, 3, 4).toShortString(lang = Lang.NEPALI)
        for (digit in listOf("१", "९", "०", "२", "३", "४")) assertTrue(digit in unicode, digit)
    }

    @Test
    fun ToUnicodeString_SeparatorsPreserved() {
        val unicode = earlier.toShortString(DateOrder.YMD, Separator.DASH, lang = Lang.NEPALI)
        assertTrue("-" in unicode)
        assertFalse("/" in unicode)
    }

    @Test
    fun ToUnicodeString_SameLengthAsToString() {
        for (year in 1901..2199 step 100) {
            val date = NepaliDate.of(year, 6, 15)
            assertEquals(date.toString().length, date.toShortString(lang = Lang.NEPALI).length)
        }
    }

    @ParameterizedTest
    @CsvSource("२०८०/०५/१५, 2080, 5, 15", "२०८०-०५-१५, 2080, 5, 15")
    fun SmartDateParser_NepaliDigits_ParseCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertEquals(NepaliDate.of(year, month, day), NepaliDate.parseLenient(input))
    }

    @Test
    fun SmartDateParser_PureEnglishDigits_NoConversionNeeded() {
        assertEquals(earlier, NepaliDate.parseLenient("2080/05/15"))
    }

    @Test
    fun SmartDateParser_MixedNepaliEnglishDigits_ParseCorrectly() {
        assertEquals(earlier, NepaliDate.parseLenient("2०८0/0५/१5"))
    }

    @ParameterizedTest
    @CsvSource(
        "2080/05/15 B.S., 2080, 5, 15",
        "2080/05/15 BS, 2080, 5, 15",
        "2080/05/15 V.S., 2080, 5, 15",
        "2080/05/15 VS, 2080, 5, 15",
        "2080/05/15 bs, 2080, 5, 15",
        "2080/05/15 v.s., 2080, 5, 15",
    )
    fun SmartDateParser_WithIndicators_RemovesAndParsesCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertEquals(NepaliDate.of(year, month, day), NepaliDate.parseLenient(input))
    }

    @Test
    fun SmartDateParser_WithNepaliKeywords_RemovesAndParses() {
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("15 Shrawan 2080 गते"))
    }

    @Test
    fun SmartDateParser_WithExtraSpaces_ParsesCorrectly() {
        assertEquals(earlier, NepaliDate.parseLenient("  2080 / 05 / 15  "))
    }

    @ParameterizedTest
    @CsvSource(
        "15 Baisakh 2080, 2080, 1, 15",
        "15 Jestha 2080, 2080, 2, 15",
        "15 Asar 2080, 2080, 3, 15",
        "15 Shrawan 2080, 2080, 4, 15",
        "15 Bhadra 2080, 2080, 5, 15",
        "15 Ashwin 2080, 2080, 6, 15",
        "15 Kartik 2080, 2080, 7, 15",
        "15 Mangsir 2080, 2080, 8, 15",
        "15 Poush 2080, 2080, 9, 15",
        "15 Magh 2080, 2080, 10, 15",
        "15 Falgun 2080, 2080, 11, 15",
        "15 Chaitra 2080, 2080, 12, 15",
    )
    fun SmartDateParser_AllMonthNames_ParseCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertEquals(NepaliDate.of(year, month, day), NepaliDate.parseLenient(input))
    }

    @ParameterizedTest
    @CsvSource("'Baisakh 15, 2080', 2080, 1, 15", "2080 Baisakh 15, 2080, 1, 15")
    fun SmartDateParser_MonthNameDifferentPositions_ParsesCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertEquals(NepaliDate.of(year, month, day), NepaliDate.parseLenient(input))
    }

    @ParameterizedTest
    @CsvSource("15 वैशाख 2080, 2080, 1, 15", "15 जेष्ठ 2080, 2080, 2, 15", "15 असार 2080, 2080, 3, 15", "15 श्रावण 2080, 2080, 4, 15")
    fun SmartDateParser_NepaliUnicodeMonthNames_ParseCorrectly(
        input: String,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertEquals(NepaliDate.of(year, month, day), NepaliDate.parseLenient(input))
    }

    // D-06: two-digit years are never expanded; the port reports them as ambiguous.
    @Test
    fun SmartDateParser_TwoDigitYear_AdjustedTo2000s() {
        val error = assertFailsWith<NepDateException> { NepaliDate.parseLenient("80/05/15") }
        assertEquals(NepDateException.Kind.AMBIGUOUS, error.kind)
    }

    @Test
    fun SmartDateParser_InvalidInput_ThrowsFormatException() {
        assertFailsWith<NepDateException> { NepaliDate.parseLenient("not a date") }
    }

    @Test
    fun SmartDateParser_NullInput_ThrowsException() {
        // Kotlin can't pass null; a Java caller that does gets NullPointerException.
        assertFailsWith<NullPointerException> { parseFromJava("parseLenient", null) }
    }

    @Test
    fun SmartDateParser_EmptyInput_ThrowsException() {
        assertFailsWith<NepDateException> { NepaliDate.parseLenient("") }
    }

    @Test
    fun SmartDateParser_WhitespaceInput_ThrowsException() {
        assertFailsWith<NepDateException> { NepaliDate.parseLenient("   ") }
    }

    @Test
    fun SmartDateParser_TryParse_ValidInput_ReturnsTrue() {
        assertEquals(2080, NepaliDate.parseOrNull("2080/05/15")?.year)
    }

    @Test
    fun SmartDateParser_TryParse_InvalidInput_ReturnsFalse() {
        assertNull(NepaliDate.parseOrNull("garbage"))
    }

    // C# returns false for null; on the JVM a null argument is a NullPointerException, because the
    // parameter is declared non-null for Kotlin callers.
    @Test
    fun SmartDateParser_TryParse_NullInput_ReturnsFalse() {
        assertFailsWith<NullPointerException> { parseFromJava("parseOrNull", null) }
    }

    @Test
    fun ToString_EveryMonthFirstDay_RoundtripsViaConstructor() {
        for (year in 1901..2199) {
            for (month in 1..12) {
                val date = NepaliDate.of(year, month, 1)
                assertEquals(date, NepaliDate.parse(date.toString()))
            }
        }
    }

    @Test
    fun UnicodeConversion_AllDigits_RoundtripCorrectly() {
        for (year in 2070..2090) {
            for (month in 1..12) {
                val date = NepaliDate.of(year, month, 15)
                assertEquals(date, NepaliDate.parseLenient(date.toShortString(lang = Lang.NEPALI)))
            }
        }
    }

    // C#'s ToLongDateString(displayDayName, displayYear) is toLongString(weekday, year).
    @Test
    fun ToLongDateString_Default_ContainsMonthNameAndDay() {
        val text = earlier.toLongString()
        assertTrue("15" in text)
        assertTrue("2080" in text)
        assertTrue("," in text)
    }

    @Test
    fun ToLongDateString_WithDayName_ContainsDayOfWeek() {
        assertTrue(earlier.dayOfWeek.displayName() in earlier.toLongString(weekday = true))
    }

    @Test
    fun ToLongDateString_WithoutYear_DoesNotContainYear() {
        assertFalse("2080" in earlier.toLongString(year = false))
    }

    // IsDefault_DefaultStruct_ReturnsTrue and IsDefault_ValidDate_ReturnsFalse: not ported, PARITY D-01
    // (there is no default or invalid NepaliDate to ask about).
}
