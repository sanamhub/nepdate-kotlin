@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** C# `Core/NepaliDateConstructionTests.cs`. */
class NepaliDateConstructionTests {
    private fun assertYmd(
        date: NepaliDate,
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertEquals(Triple(year, month, day), Triple(date.year, date.monthValue, date.dayOfMonth))
    }

    @Test
    fun Constructor_ValidNepaliDate_CreatesInstance() {
        assertYmd(NepaliDate.of(2080, 5, 15), 2080, 5, 15)
    }

    @Test
    fun Constructor_MinValue_CreatesInstance() {
        assertYmd(NepaliDate.MIN, 1901, 1, 1)
    }

    @Test
    fun Constructor_MaxValue_CreatesInstance() {
        val max = NepaliDate.MAX
        assertEquals(2199, max.year)
        assertEquals(12, max.monthValue)
        assertTrue(max.dayOfMonth > 0)
    }

    @ParameterizedTest
    @CsvSource("2080, 0, 1", "2080, 13, 1", "2080, 1, 0", "2080, 1, 33")
    fun Constructor_InvalidNepaliDate_ThrowsException(
        year: Int,
        month: Int,
        day: Int,
    ) {
        assertFailsWith<NepDateException> { NepaliDate.of(year, month, day) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["2080/05/15", "2080-05-15", "2080.05.15", "2080_05_15", """2080\05\15""", "2080 05 15"])
    fun Constructor_ValidStringFormats_CreatesInstance(input: String) {
        assertYmd(NepaliDate.parse(input), 2080, 5, 15)
    }

    // Not ported, PARITY D-06 (no day and month swapping, no two-digit year guessing):
    // Constructor_AutoAdjustedFormats_CreatesInstance #1..#4,
    // Constructor_AutoAdjust_MonthOverflowWithBoundaryDay_SwapsAsDocumented #1..#2,
    // TryParse_AutoAdjust_MonthOverflowWithBoundaryDay_SwapsAsDocumented.

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "2080/5"])
    fun Constructor_InvalidStringFormats_ThrowsException(input: String) {
        assertFailsWith<NepDateException> { NepaliDate.parse(input) }
    }

    @ParameterizedTest
    @ValueSource(strings = ["2080/5/40"])
    fun Constructor_ValidFormatInvalidDate_ThrowsException(input: String) {
        val error = assertFailsWith<NepDateException> { NepaliDate.parse(input) }
        assertEquals(NepDateException.Kind.INVALID_DAY, error.kind)
    }

    @Test
    fun Constructor_EnglishDate_ConvertsCorrectly() {
        assertYmd(NepaliDate.ofGregorian(2023, 8, 30), 2080, 5, 13)
    }

    @Test
    fun TryParse_ValidString_ReturnsTrueWithCorrectDate() {
        val date = NepaliDate.parseOrNull("2080/05/15")
        assertNotNull(date)
        assertYmd(date, 2080, 5, 15)
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "2080/5"])
    fun TryParse_InvalidString_ReturnsFalseWithDefault(input: String) {
        assertNull(NepaliDate.parseOrNull(input))
    }
}
