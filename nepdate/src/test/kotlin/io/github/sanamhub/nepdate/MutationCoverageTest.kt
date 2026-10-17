package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests added for mutants that survived the first PIT run (K7-02). Each test names the behaviour it
 * pins, not the mutant, so it stays useful after PIT changes.
 */
class MutationCoverageTest {
    @Test
    fun `list bridges on a range call the typed overrides`() {
        val range = NepaliDateRange.ofMonth(2081, 4)

        @Suppress("UNCHECKED_CAST")
        val list = range as List<Any>
        val day = NepaliDate.of(2081, 4, 15)
        val outside = NepaliDate.of(2081, 5, 1)
        assertTrue(list.contains(day))
        assertFalse(list.contains(outside))
        assertFalse(list.contains("2081/04/15"))
        assertEquals(14, list.indexOf(day))
        assertEquals(-1, list.indexOf(outside))
        assertEquals(-1, list.indexOf("2081/04/15"))
        assertEquals(14, list.lastIndexOf(day))
        assertEquals(-1, list.lastIndexOf(outside))
        assertEquals(-1, list.lastIndexOf("2081/04/15"))
    }

    @Test
    fun `list bridges on quarter months call the typed overrides`() {
        @Suppress("UNCHECKED_CAST")
        val months = Quarter.Q1.months as List<Any>
        assertTrue(months.contains(Month.SHRAWAN))
        assertTrue(months.contains(Month.ASHOJ))
        assertFalse(months.contains(Month.MAGH))
        assertFalse(months.contains("Shrawan"))
        assertEquals(0, months.indexOf(Month.SHRAWAN))
        assertEquals(2, months.indexOf(Month.ASHOJ))
        assertEquals(-1, months.indexOf(Month.MAGH))
        assertEquals(-1, months.indexOf("Shrawan"))
        assertEquals(0, months.lastIndexOf(Month.SHRAWAN))
        assertEquals(2, months.lastIndexOf(Month.ASHOJ))
        assertEquals(-1, months.lastIndexOf(Month.MAGH))
        assertEquals(-1, months.lastIndexOf("Shrawan"))
    }

    @Test
    fun `ofOrNull and parseOrNull return a date for valid input`() {
        val expected = NepaliDate.of(2081, 4, 15)
        assertEquals(expected, NepaliDate.ofOrNull(2081, 4, 15))
        assertEquals(expected, NepaliDate.parseOrNull("2081/04/15"))
        assertNull(NepaliDate.parseOrNull("0/0/0"))
    }

    @Test
    fun `parse treats a zero date as a range error`() {
        val error = assertFailsWith<NepDateException> { NepaliDate.parse("0/0/0") }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
        assertEquals(NepDateException.Kind.WRONG_GROUP_COUNT, assertFailsWith<NepDateException> { NepaliDate.parse("2080/05/") }.kind)
        assertEquals(NepDateException.Kind.WRONG_GROUP_COUNT, assertFailsWith<NepDateException> { NepaliDate.parse("2080/05//") }.kind)
    }

    @Test
    fun `every ASCII and Devanagari digit parses`() {
        assertEquals(NepaliDate.of(2080, 5, 1), NepaliDate.parse("2080/05/01"))
        assertEquals(NepaliDate.of(2080, 5, 9), NepaliDate.parse("2080/05/09"))
        assertEquals(NepaliDate.of(2089, 10, 10), NepaliDate.parse("2089/10/10"))
        assertEquals(NepaliDate.of(2080, 5, 1), NepaliDate.parse("२०८०/०५/०१"))
        assertEquals(NepaliDate.of(2080, 5, 9), NepaliDate.parse("२०८०/०५/०९"))
        assertEquals(NepaliDate.of(2089, 10, 10), NepaliDate.parse("२०८९/१०/१०"))
    }

    @Test
    fun `lenient dots in every position`() {
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("15.04.2080"))
        assertEquals(NepaliDate.of(2080, 5, 15), NepaliDate.parseLenient("2080.05.15"))
        assertEquals(NepaliDate.of(2080, 4, 1), NepaliDate.parseLenient("01.04.2080"))
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("15.04.2080."))
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("..15.04.2080"))
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("15..04..2080"))
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("15...04...2080"))
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenient("15.04.2080..."))
        assertEquals(NepaliDate.of(2080, 5, 15), NepaliDate.parseLenient("2080...05...15"))
    }

    @Test
    fun `lenient rejects an empty or dots-only input`() {
        assertEquals(NepDateException.Kind.UNRECOGNIZED, assertFailsWith<NepDateException> { NepaliDate.parseLenient("") }.kind)
        assertEquals(NepDateException.Kind.UNRECOGNIZED, assertFailsWith<NepDateException> { NepaliDate.parseLenient(".") }.kind)
        assertEquals(NepDateException.Kind.UNRECOGNIZED, assertFailsWith<NepDateException> { NepaliDate.parseLenient("...") }.kind)
        assertEquals(NepDateException.Kind.UNRECOGNIZED, assertFailsWith<NepDateException> { NepaliDate.parseLenient("15...") }.kind)
        assertEquals(NepDateException.Kind.UNRECOGNIZED, assertFailsWith<NepDateException> { NepaliDate.parseLenient("...15") }.kind)
    }

    @Test
    fun `lenient keeps a zero number in every position`() {
        assertEquals(NepDateException.Kind.INVALID_DAY, assertFailsWith<NepDateException> { NepaliDate.parseLenient("0.04.2080") }.kind)
        assertEquals(NepDateException.Kind.INVALID_DAY, assertFailsWith<NepDateException> { NepaliDate.parseLenient("2080.04.0") }.kind)
        assertEquals(NepDateException.Kind.INVALID_MONTH, assertFailsWith<NepDateException> { NepaliDate.parseLenient("2080.0.04") }.kind)
    }

    @Test
    fun `digitValue covers both digit ranges and their neighbours`() {
        for (digit in 0..9) {
            assertEquals(digit, digitValue('0' + digit))
            assertEquals(digit, digitValue('०' + digit))
        }
        assertEquals(-1, digitValue(':'))
        assertEquals(-1, digitValue('/'))
        assertEquals(-1, digitValue('०' - 1))
        assertEquals(-1, digitValue('९' + 1))
    }

    @Test
    fun `DateDiff toString keeps the sign`() {
        val positive = NepaliDate.of(2081, 1, 1).until(NepaliDate.of(2081, 2, 1))
        assertFalse(positive.isNegative)
        assertFalse(positive.toString().startsWith("DateDiff(-"))
        val negative = NepaliDate.of(2081, 2, 1).until(NepaliDate.of(2081, 1, 1))
        assertTrue(negative.isNegative)
        assertTrue(negative.toString().startsWith("DateDiff(-"))
    }

    @Test
    fun `single digit fiscal years use Devanagari digits`() {
        assertEquals("९/१०", FiscalYear(9).label(Lang.NEPALI))
        assertEquals("०/०१", FiscalYear(0).label(Lang.NEPALI))
    }

    @Test
    fun `every fiscal year label uses Devanagari digits`() {
        for (year in 1901..2199) {
            val english = FiscalYear(year).label(Lang.ENGLISH)
            val expected =
                english
                    .map { if (it in '0'..'9') '०' + (it - '0') else it }
                    .joinToString("")
            assertEquals(expected, FiscalYear(year).label(Lang.NEPALI), "FY $year")
        }
    }

    @Test
    fun `Gregorian errors say whether the date exists or is out of range`() {
        val missing = assertFailsWith<NepDateException> { NepaliDate.ofGregorian(2024, 2, 30) }
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, missing.kind)
        assertTrue(missing.message!!.contains("not a date"))
        val outside = assertFailsWith<NepDateException> { NepaliDate.ofGregorian(1800, 1, 1) }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, outside.kind)
        assertTrue(outside.message!!.contains("outside"))
    }

    @Test
    fun `invalid day message pads a single-digit month and leaves a two-digit month alone`() {
        val april = assertFailsWith<NepDateException> { NepaliDate.of(2081, 4, 40) }
        assertTrue(april.message!!.contains("2081-04"), april.message)
        val ashoj = assertFailsWith<NepDateException> { NepaliDate.of(2081, 10, 40) }
        assertTrue(ashoj.message!!.contains("2081-10"), ashoj.message)
    }

    @Test
    fun `plusYears reports the month amount it really passed on`() {
        val high = assertFailsWith<NepDateException> { NepaliDate.of(2081, 1, 1).plusYears(299) }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, high.kind)
        assertTrue(high.message!!.contains("3588 months"), high.message)
        val low = assertFailsWith<NepDateException> { NepaliDate.of(2081, 1, 1).plusYears(-299) }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, low.kind)
        assertTrue(low.message!!.contains("3588 months"), low.message)
    }

    @Test
    fun `nepaliDigits pads Long MIN VALUE when asked`() {
        assertEquals(
            "-" + "०".repeat(6) + "९२२३३७२०३६८५४७७५८०८",
            nepaliDigits(Long.MIN_VALUE, 25),
        )
    }

    @Test
    fun `short form pads or drops the year and field zeros`() {
        val date = NepaliDate.of(2081, 4, 5)
        assertEquals("2081/04/05", date.toShortString())
        assertEquals("2081/4/5", date.toShortString(pad = false))
        assertEquals("5/4/2081", date.toShortString(DateOrder.DMY, Separator.SLASH, pad = false))
    }
}
