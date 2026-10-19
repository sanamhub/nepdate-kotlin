@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * C# `Abilities/NepaliDateFormattableTests.cs`. C#'s `IFormattable.ToString(format, provider)`
 * is `format(pattern)` here (FORMATTING section 5); a null format is the empty pattern.
 */
class NepaliDateFormattableTests {
    private val date = NepaliDate.of(2081, 4, 15)

    @Test
    fun ToString_NullFormat_ReturnsDefault() {
        assertEquals("2081/04/15", date.format(""))
    }

    @Test
    fun ToString_G_SameAsDefault() {
        assertEquals(date.toString(), date.format("G"))
    }

    @Test
    fun ToString_g_SameAsDefault() {
        assertEquals(date.toString(), date.format("g"))
    }

    @Test
    fun ToString_d_SameAsDefault() {
        assertEquals("2081/04/15", date.format("d"))
    }

    @Test
    fun ToString_D_ReturnsLongDateString() {
        val result = date.format("D")
        assertTrue("2081" in result)
        assertTrue("Shrawan" in result)
    }

    @Test
    fun ToString_s_ReturnsSortableIsoFormat() {
        assertEquals("2081-04-15", date.format("s"))
    }

    @Test
    fun CustomFormat_yyyy_MM_dd_WithDash() {
        assertEquals("2081-04-15", date.format("yyyy-MM-dd"))
    }

    @Test
    fun CustomFormat_dd_MM_yyyy_WithDot() {
        assertEquals("15.04.2081", date.format("dd.MM.yyyy"))
    }

    @Test
    fun CustomFormat_MM_slash_dd_slash_yyyy() {
        assertEquals("04/15/2081", date.format("MM/dd/yyyy"))
    }

    @Test
    fun CustomFormat_MMMxxx_ReturnsThreeCharMonthAbbreviation() {
        val result = date.format("MMM yyyy")
        assertTrue(result.startsWith("Shr"))
        assertTrue(result.endsWith("2081"))
    }

    @Test
    fun CustomFormat_MMMM_ReturnsFullMonthName() {
        assertTrue(date.format("MMMM dd, yyyy").startsWith("Shrawan"))
    }

    @Test
    fun CustomFormat_yy_ReturnsTwoDigitYear() {
        assertTrue(date.format("yy/MM/dd").startsWith("81/"))
    }

    @Test
    fun CustomFormat_M_WithoutLeadingZero_ReturnsUnpadded() {
        assertEquals("4/15/2081", date.format("M/d/yyyy"))
    }

    @Test
    fun CustomFormat_SingleDigitMonth_WithoutLeadingZero() {
        assertEquals("1/5/2081", NepaliDate.of(2081, 1, 5).format("M/d/yyyy"))
    }

    @Test
    fun CustomFormat_LiteralInSingleQuotes() {
        assertEquals("2081BS", date.format("yyyy'BS'"))
    }

    @Test
    fun CustomFormat_BackslashEscapedTokens_ProduceLiterals() {
        assertEquals("2081MM-dd", date.format("""yyyy\M\M-\d\d"""))
    }

    @Test
    fun CustomFormat_BackslashBeforeLiteralChar_IsPassedThrough() {
        assertEquals("2081-04-15", date.format("""yyyy\-MM\-dd"""))
    }

    // C# string interpolation and string.Format reach IFormattable; Kotlin templates call
    // toString(), so the pattern is passed to format() directly.
    @Test
    fun StringInterpolation_WithFormatSpecifier_UsesIFormattable() {
        assertEquals("2081-04-15", "${date.format("s")}")
    }

    @Test
    fun StringFormatMethod_WithSpecifier() {
        assertEquals("2081-04-15", date.format("yyyy-MM-dd"))
    }

    @Test
    fun SortableFormat_LexicographicOrderEqualsChronologicalOrder() {
        val earlier = NepaliDate.of(2080, 12, 30).format("s")
        val later = NepaliDate.of(2081, 1, 1).format("s")
        assertTrue(earlier < later)
    }
}
