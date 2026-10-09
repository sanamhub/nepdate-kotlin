package io.github.sanamhub.nepdate

import net.jqwik.api.Assume
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import net.jqwik.api.constraints.LongRange
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private const val SEED = "20261010"

private fun epoch(serial: Int): Long = (serial + EPOCH_UNIX_DAYS).toLong()

class ArithmeticProperties {
    @Property(seed = SEED)
    fun `plusDays moves the epoch day by n`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) serial: Int,
        @ForAll @LongRange(min = -MAX_SERIAL.toLong(), max = MAX_SERIAL.toLong()) n: Long,
    ) {
        Assume.that(serial + n in 0..MAX_SERIAL)
        val date = NepaliDate.ofEpochDay(epoch(serial))
        assertEquals(epoch(serial) + n, date.plusDays(n).toEpochDay())
        assertEquals(-n, date.plusDays(n).daysUntil(date))
    }

    @Property(seed = SEED)
    fun `clamping never raises the day`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) serial: Int,
        @ForAll @LongRange(min = -3588, max = 3588) n: Long,
    ) {
        val date = NepaliDate.ofEpochDay(epoch(serial))
        val result = runCatching { date.plusMonths(n, MonthOverflow.CLAMP) }.getOrNull() ?: return
        assertTrue(result.dayOfMonth <= date.dayOfMonth)
    }

    @Property(seed = SEED)
    fun `months there and back return the date when nothing was clamped`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) serial: Int,
        @ForAll @LongRange(min = -3588, max = 3588) n: Long,
    ) {
        val date = NepaliDate.ofEpochDay(epoch(serial))
        val there = runCatching { date.plusMonths(n) }.getOrNull() ?: return
        Assume.that(there.dayOfMonth == date.dayOfMonth)
        assertEquals(date, there.plusMonths(-n))
    }

    @Property(seed = SEED)
    fun `a range holds daysUntil plus one days`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) a: Int,
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) b: Int,
    ) {
        val first = NepaliDate.ofEpochDay(epoch(minOf(a, b)))
        val last = NepaliDate.ofEpochDay(epoch(maxOf(a, b)))
        assertEquals(first.daysUntil(last) + 1, NepaliDateRange.of(first, last).size.toLong())
    }
}
