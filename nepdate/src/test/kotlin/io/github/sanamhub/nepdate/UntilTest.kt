package io.github.sanamhub.nepdate

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class UntilTest {
    private fun check(
        from: NepaliDate,
        to: NepaliDate,
        negative: Boolean,
        years: Int,
        months: Int,
        days: Int,
        total: Long,
    ) {
        val diff = from.until(to)
        val context = "$from until $to"
        assertEquals(negative, diff.isNegative, context)
        assertEquals(years, diff.years, context)
        assertEquals(months, diff.months, context)
        assertEquals(days, diff.days, context)
        assertEquals(total, diff.totalDays, context)
    }

    private fun d(
        y: Int,
        m: Int,
        day: Int,
    ) = NepaliDate.of(y, m, day)

    @Test
    fun `examples of ALGORITHM section 8a`() {
        check(d(2080, 1, 1), d(2081, 4, 15), false, 1, 3, 14, 473)
        check(d(2081, 4, 15), d(2080, 1, 1), true, 1, 3, 14, -473)
        check(d(2081, 4, 32), d(2081, 5, 31), false, 0, 1, 0, 31)
        check(d(2081, 4, 32), d(2081, 6, 1), false, 0, 1, 1, 32)
        check(d(2081, 1, 31), d(2081, 2, 30), false, 0, 0, 30, 30)
        check(d(2081, 3, 15), d(2081, 3, 15), false, 0, 0, 0, 0)
    }

    @Test
    fun `until examples`() {
        check(d(2081, 1, 1), d(2082, 1, 1), false, 1, 0, 0, d(2081, 1, 1).daysUntil(d(2082, 1, 1)))
        check(d(2081, 4, 32), d(2081, 6, 30), false, 0, 2, 0, 61)
        check(d(2081, 4, 15), d(2081, 5, 14), false, 0, 0, 31, 31)
        check(d(2081, 5, 14), d(2081, 4, 15), true, 0, 0, 31, -31)
    }

    @Test
    fun `1000 seeded pairs agree with daysUntil and with the reverse`() {
        val random = Random(42)
        repeat(1000) {
            val a = NepaliDate.ofEpochDay(random.nextLong(-45920, 63292))
            val b = NepaliDate.ofEpochDay(random.nextLong(-45920, 63292))
            val forward = a.until(b)
            val backward = b.until(a)
            assertEquals(a.daysUntil(b), forward.totalDays)
            assertEquals(-forward.totalDays, backward.totalDays)
            assertEquals(Triple(forward.years, forward.months, forward.days), Triple(backward.years, backward.months, backward.days))
            if (a != b) assertNotEquals(forward.isNegative, backward.isNegative)
        }
    }

    @Test
    fun `equality uses all five values`() {
        assertEquals(d(2081, 1, 1).until(d(2081, 2, 1)), d(2081, 1, 1).until(d(2081, 2, 1)))
        assertEquals(d(2081, 1, 1).until(d(2081, 2, 1)).hashCode(), d(2081, 1, 1).until(d(2081, 2, 1)).hashCode())
        assertNotEquals(d(2081, 1, 1).until(d(2081, 2, 1)), d(2081, 2, 1).until(d(2081, 1, 1)))
        // Same breakdown, different day count: 2081-01 has 31 days, 2081-02 has 32.
        assertNotEquals(d(2081, 1, 1).until(d(2081, 2, 1)), d(2081, 2, 1).until(d(2081, 3, 1)))
        assertEquals(true, d(2081, 1, 1).until(d(2081, 2, 1)).toString().isNotEmpty())
    }
}
