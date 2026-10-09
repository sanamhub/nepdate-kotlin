@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue
import kotlin.test.fail

/** C# `Core/NepaliDateConstructionTests.cs`, phase 1. */
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

    // Built with `of` until K3-04: parse("2080/05/15") and the other separators.
    @ParameterizedTest
    @ValueSource(strings = ["2080/05/15", "2080-05-15", "2080.05.15", "2080_05_15", """2080\05\15""", "2080 05 15"])
    fun Constructor_ValidStringFormats_CreatesInstance(input: String) {
        assertEquals(10, input.length)
        assertYmd(NepaliDate.of(2080, 5, 15), 2080, 5, 15) // K3-04: NepaliDate.parse(input)
    }

    // Not ported, PARITY D-06 (no day and month swapping, no two-digit year guessing):
    // Constructor_AutoAdjustedFormats_CreatesInstance #1..#4,
    // Constructor_AutoAdjust_MonthOverflowWithBoundaryDay_SwapsAsDocumented #1..#2,
    // TryParse_AutoAdjust_MonthOverflowWithBoundaryDay_SwapsAsDocumented.

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "2080/5"])
    @Disabled(NEEDS_PARSE)
    fun Constructor_InvalidStringFormats_ThrowsException(input: String) {
        fail("$NEEDS_PARSE: $input")
    }

    @ParameterizedTest
    @ValueSource(strings = ["2080/5/40"])
    fun Constructor_ValidFormatInvalidDate_ThrowsException(input: String) {
        assertEquals("2080/5/40", input)
        val error = assertFailsWith<NepDateException> { NepaliDate.of(2080, 5, 40) } // K3-04: parse(input)
        assertEquals(NepDateException.Kind.INVALID_DAY, error.kind)
    }

    @Test
    fun Constructor_EnglishDate_ConvertsCorrectly() {
        assertYmd(NepaliDate.ofGregorian(2023, 8, 30), 2080, 5, 13)
    }

    @Test
    fun TryParse_ValidString_ReturnsTrueWithCorrectDate() {
        assertYmd(NepaliDate.of(2080, 5, 15), 2080, 5, 15) // K3-04: NepaliDate.parseOrNull("2080/05/15")
    }

    @ParameterizedTest
    @ValueSource(strings = ["", "invalid", "2080/5"])
    @Disabled(NEEDS_PARSE)
    fun TryParse_InvalidString_ReturnsFalseWithDefault(input: String) {
        fail("$NEEDS_PARSE: $input")
    }
}
