package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The date members that mirror a C# member of the same name: destructuring and the lenient null form. */
class NepaliDateApiTest {
    @Test
    fun `Deconstruct gives year month and day`() {
        val (year, month, day) = NepaliDate.of(2081, 4, 15)
        assertEquals(2081, year)
        assertEquals(4, month)
        assertEquals(15, day)
        // The middle component is the card number the C# Deconstruct returns, not the Month enum.
        assertEquals(NepaliDate.of(2081, 4, 15).monthValue, month)
        assertEquals(listOf(2081, 4, 15), listOf(year, month, day))
    }

    @Test
    fun `parseLenientOrNull answers null instead of throwing`() {
        assertEquals(NepaliDate.of(2080, 4, 15), NepaliDate.parseLenientOrNull("15 Shrawan 2080"))
        assertEquals(NepaliDate.of(2080, 5, 15), NepaliDate.parseLenientOrNull("2080/05/15"))
        assertNull(NepaliDate.parseLenientOrNull("not a date"))
        assertNull(NepaliDate.parseLenientOrNull("2081-01-32"))
        assertNull(NepaliDate.parseLenientOrNull("15 Shrawan 81"))
        assertNull(NepaliDate.parseLenientOrNull(""))
    }

    @Test
    fun `parseOrNull and parseLenientOrNull agree on strict input`() {
        for (text in listOf("2081-04-15", "1901-1-1", "२१९९/१२/३०", "2080 05 15")) {
            assertEquals(NepaliDate.parse(text), NepaliDate.parseOrNull(text), text)
            assertEquals(NepaliDate.parse(text), NepaliDate.parseLenientOrNull(text), text)
        }
    }
}
