package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class NepaliDateTest {
    private fun kindOf(block: () -> Unit): NepDateException.Kind = assertFailsWith<NepDateException> { block() }.kind

    @Test
    fun `day 32 is valid in a 32-day month and 33 throws with the month length`() {
        val date = NepaliDate.of(2081, 4, 32)
        assertEquals(2081, date.year)
        assertEquals(4, date.monthValue)
        assertEquals(32, date.dayOfMonth)
        val error = assertFailsWith<NepDateException> { NepaliDate.of(2081, 4, 33) }
        assertEquals(NepDateException.Kind.INVALID_DAY, error.kind)
        assertEquals(32, error.monthLength)
        assertNull(NepaliDate.ofOrNull(2081, 4, 33))
    }

    @Test
    fun `year outside the range throws OUT_OF_RANGE`() {
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.of(1900, 1, 1) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.of(2200, 1, 1) })
    }

    @Test
    fun `errors follow ALGORITHM section 3 precedence`() {
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.of(1900, 13, 40) })
        assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf { NepaliDate.of(2081, 13, 40) })
        assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf { NepaliDate.of(2081, 0, 1) })
        assertEquals(NepDateException.Kind.INVALID_DAY, kindOf { NepaliDate.of(2081, 1, 0) })
        assertEquals(0, assertFailsWith<NepDateException> { NepaliDate.of(2081, 13, 1) }.monthLength)
    }

    @Test
    fun `2199-12-30 is the last day`() {
        assertEquals(NepaliDate.MAX, NepaliDate.of(2199, 12, 30))
        assertEquals(NepDateException.Kind.INVALID_DAY, kindOf { NepaliDate.of(2199, 12, 31) })
        assertEquals(NepaliDate.MIN, NepaliDate.of(1901, 1, 1))
    }

    @Test
    fun `of with a Month enum`() {
        assertEquals(NepaliDate.of(2081, 4, 15), NepaliDate.of(2081, Month.SHRAWAN, 15))
        assertEquals(Month.SHRAWAN, NepaliDate.of(2081, 4, 15).month)
    }

    @Test
    fun `ofOrNull agrees with of`() {
        assertEquals(NepaliDate.of(2081, 4, 15), NepaliDate.ofOrNull(2081, 4, 15))
        assertNull(NepaliDate.ofOrNull(1900, 1, 1))
        assertNull(NepaliDate.ofOrNull(2081, 13, 1))
    }

    @Test
    fun `equality, hash and order follow the day`() {
        val a = NepaliDate.of(2081, 4, 15)
        val b = NepaliDate.of(2081, 4, 15)
        val c = NepaliDate.of(2081, 4, 16)
        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, c)
        assertTrue(a < c)
        assertEquals(0, a.compareTo(b))
        assertNotEquals<Any>(a, "2081/04/15")
    }

    @Test
    fun `toString is YYYY slash MM slash DD`() {
        assertEquals("2081/04/15", NepaliDate.of(2081, 4, 15).toString())
        assertEquals("1901/01/01", NepaliDate.MIN.toString())
        assertEquals("2199/12/30", NepaliDate.MAX.toString())
    }
}
