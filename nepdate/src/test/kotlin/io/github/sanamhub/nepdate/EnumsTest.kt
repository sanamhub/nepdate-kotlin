package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs

class EnumsTest {
    @Test
    fun `month of number`() {
        assertEquals(Month.SHRAWAN, Month.of(4))
        assertEquals(Month.BAISHAKH, Month.of(1))
        assertEquals(Month.CHAITRA, Month.of(12))
        for (month in Month.entries) assertEquals(month, Month.of(month.value))
    }

    @Test
    fun `month outside 1 to 12 throws INVALID_MONTH`() {
        for (value in listOf(0, 13, -1, Int.MIN_VALUE, Int.MAX_VALUE)) {
            val error = assertFailsWith<NepDateException> { Month.of(value) }
            assertEquals(NepDateException.Kind.INVALID_MONTH, error.kind)
            assertEquals(0, error.monthLength)
        }
    }

    @Test
    fun `exception is an IllegalArgumentException`() {
        val error = assertFailsWith<IllegalArgumentException> { Month.of(0) }
        assertIs<NepDateException>(error)
    }

    @Test
    fun `weekday order is Sunday first`() {
        assertEquals(Weekday.SUNDAY, Weekday.entries[0])
        assertEquals(Weekday.SATURDAY, Weekday.entries[6])
    }

    @Test
    fun `languages`() {
        assertEquals(listOf(Lang.ENGLISH, Lang.NEPALI), Lang.entries)
    }
}
