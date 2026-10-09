package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GregorianTest {
    private fun kindOf(block: () -> Unit): NepDateException.Kind = assertFailsWith<NepDateException> { block() }.kind

    private fun ad(date: NepaliDate) = Triple(date.gregorianYear, date.gregorianMonth, date.gregorianDay)

    @Test
    fun `reference values of ALGORITHM section 9`() {
        val rows =
            listOf(
                Triple(NepaliDate.of(1901, 1, 1), Triple(1844, 4, 11), Weekday.THURSDAY),
                Triple(NepaliDate.of(2081, 1, 1), Triple(2024, 4, 13), Weekday.SATURDAY),
                Triple(NepaliDate.of(2082, 1, 1), Triple(2025, 4, 14), Weekday.MONDAY),
                Triple(NepaliDate.of(2083, 1, 1), Triple(2026, 4, 14), Weekday.TUESDAY),
            )
        for ((bs, adDate, weekday) in rows) {
            assertEquals(adDate, ad(bs), bs.toString())
            assertEquals(weekday, bs.dayOfWeek, bs.toString())
            assertEquals(bs, NepaliDate.ofGregorian(adDate.first, adDate.second, adDate.third))
        }
        assertEquals(Triple(2143, 4, 15), ad(NepaliDate.MAX))
        assertEquals(NepaliDate.MAX, NepaliDate.ofGregorian(2143, 4, 15))
    }

    @Test
    fun `AD dates outside the range throw OUT_OF_RANGE`() {
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.ofGregorian(1844, 4, 10) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.ofGregorian(2143, 4, 16) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.ofGregorian(1, 1, 1) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.ofGregorian(Int.MAX_VALUE, 1, 1) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.ofGregorian(Int.MIN_VALUE, 1, 1) })
    }

    @Test
    fun `dates that don't exist throw INVALID_GREGORIAN before the range check`() {
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, kindOf { NepaliDate.ofGregorian(2023, 2, 29) })
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, kindOf { NepaliDate.ofGregorian(1900, 2, 29) })
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, kindOf { NepaliDate.ofGregorian(2024, 13, 1) })
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, kindOf { NepaliDate.ofGregorian(2024, 4, 31) })
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, kindOf { NepaliDate.ofGregorian(3000, 0, 1) })
        assertEquals(NepDateException.Kind.INVALID_GREGORIAN, kindOf { NepaliDate.ofGregorian(2024, 1, 0) })
        assertEquals(NepaliDate.of(2080, 11, 17), NepaliDate.ofGregorian(2024, 2, 29))
    }

    @Test
    fun `epoch days use the LocalDate epoch`() {
        assertEquals(-45920L, NepaliDate.MIN.toEpochDay())
        assertEquals(63291L, NepaliDate.MAX.toEpochDay())
        assertEquals(NepaliDate.MIN, NepaliDate.ofEpochDay(-45920))
        assertEquals(NepaliDate.MAX, NepaliDate.ofEpochDay(63291))
        assertEquals(NepaliDate.ofGregorian(1970, 1, 1), NepaliDate.ofEpochDay(0))
        for (bad in listOf(-45921L, 63292L, Long.MIN_VALUE, Long.MAX_VALUE, Int.MAX_VALUE + 1L)) {
            assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.ofEpochDay(bad) })
        }
    }

    @Test
    fun `derived properties`() {
        val date = NepaliDate.of(2081, 4, 15)
        assertEquals(32, date.lengthOfMonth())
        assertEquals(NepaliDate.of(2081, 4, 1), date.firstDayOfMonth())
        assertEquals(NepaliDate.of(2081, 4, 32), date.lastDayOfMonth())
        assertEquals(1, NepaliDate.of(2081, 1, 1).dayOfYear)
        assertEquals(31 + 32 + 31 + 15, date.dayOfYear)
        assertEquals(NepaliDate.MAX, NepaliDate.MAX.lastDayOfMonth())
        assertEquals(NepaliDate.MIN, NepaliDate.MIN.firstDayOfMonth())
    }
}
