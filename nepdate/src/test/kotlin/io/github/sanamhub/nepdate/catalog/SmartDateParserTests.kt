@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * C# `Core/SmartDateParserTests.cs`. SmartDateParser is `parseLenient` (PARITY D-06): a
 * closed month list, no fuzzy matching, no guessing of month-day order or two-digit years. Rows
 * that rely on guessing check that the port rejects the input instead.
 */
class SmartDateParserTests {
    private val expected = NepaliDate.of(2080, 4, 15)

    private fun kindOf(text: String) = assertFailsWith<NepDateException>("[$text]") { NepaliDate.parseLenient(text) }.kind

    // No lenient OrNull form exists; TryParse rows wrap parseLenient.
    private fun tryLenient(text: String): NepaliDate? = runCatching { NepaliDate.parseLenient(text) }.getOrNull()

    @Test
    fun Parse_StandardFormat_ReturnsCorrectDate() {
        for (text in listOf("2080/04/15", "2080-04-15", "2080.04.15")) assertEquals(expected, NepaliDate.parseLenient(text))
    }

    @Test
    fun Parse_InvertedFormat_ReturnsCorrectDate() {
        for (text in listOf("15/04/2080", "15-04-2080", "15.04.2080")) assertEquals(expected, NepaliDate.parseLenient(text))
    }

    // D-06: with the year last, the order is always day, month, year; month-day-year is rejected.
    @Test
    fun Parse_MonthDayYearFormat_ReturnsCorrectDate() {
        for (text in listOf("04/15/2080", "04-15-2080", "04.15.2080")) assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf(text))
    }

    @Test
    fun Parse_WithMonthNames_ReturnsCorrectDate() {
        for (text in listOf("15 Shrawan 2080", "15 Sawan 2080", "15 Saun 2080", "Shrawan 15, 2080", "Shrawan 15 2080")) {
            assertEquals(expected, NepaliDate.parseLenient(text), text)
        }
    }

    @Test
    fun Parse_WithNepaliUnicode_ReturnsCorrectDate() {
        for (text in listOf("२०८०/०४/१५", "१५/०४/२०८०", "१५ श्रावण २०८०", "श्रावण १५, २०८०")) {
            assertEquals(expected, NepaliDate.parseLenient(text), text)
        }
    }

    @Test
    fun Parse_MixedFormats_ReturnsCorrectDate() {
        for (text in listOf("15 साउन 2080", "साउन 15, २०८०", "15 Shrawan २०८०")) assertEquals(expected, NepaliDate.parseLenient(text), text)
    }

    @Test
    fun Parse_WithSuffixes_ReturnsCorrectDate() {
        for (text in listOf("15 Shrawan 2080 B.S.", "15 साउन 2080 BS", "15 Shrawan 2080 V.S.", "15 साउन, 2080 मिति", "15 साउन, 2080 गते")) {
            assertEquals(expected, NepaliDate.parseLenient(text), text)
        }
    }

    // D-06: Srawan is in the closed list (PARSING section 3); Shraawan is not, and isn't guessed.
    @Test
    fun Parse_WithTypos_ReturnsCorrectDate() {
        assertEquals(expected, NepaliDate.parseLenient("15 Srawan 2080"))
        assertEquals(NepDateException.Kind.UNRECOGNIZED, kindOf("15 Shraawan 2080"))
    }

    // D-06: two- and three-digit years are never expanded.
    @Test
    fun Parse_ShorterYearFormats_ReturnsCorrectDate() {
        assertEquals(NepDateException.Kind.AMBIGUOUS, kindOf("15/04/80"))
        assertEquals(NepDateException.Kind.AMBIGUOUS, kindOf("15/04/080"))
    }

    @Test
    fun Parse_InvalidFormat_ThrowsFormatException() {
        assertEquals(NepDateException.Kind.UNRECOGNIZED, kindOf("not a date"))
        assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf("15/13/2080"))
        assertEquals(NepDateException.Kind.INVALID_DAY, kindOf("32/03/2080"))
    }

    @Test
    fun TryParse_ValidFormat_ReturnsTrue() {
        assertEquals(expected, tryLenient("15 Shrawan 2080"))
    }

    @Test
    fun TryParse_InvalidFormat_ReturnsFalse() {
        assertNull(tryLenient("not a date"))
    }

    // C#'s string extension ToNepaliDate is SmartDateParser.Parse; the port has no string
    // extensions, so these rows call parseLenient.
    @Test
    fun ExtensionMethod_ToNepaliDate_ParsesCorrectly() {
        for (text in listOf("2080/04/15", "२०८०/०४/१५", "15 Shrawan 2080")) assertEquals(expected, NepaliDate.parseLenient(text))
    }

    @Test
    fun ExtensionMethod_TryToNepaliDate_ParsesCorrectly() {
        assertEquals(expected, tryLenient("15 Shrawan 2080"))
    }
}
