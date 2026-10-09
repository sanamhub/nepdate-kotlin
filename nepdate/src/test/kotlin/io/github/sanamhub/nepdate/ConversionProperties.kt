package io.github.sanamhub.nepdate

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import kotlin.test.assertEquals
import kotlin.test.assertNull

// Seeds are fixed per property so a CI failure reproduces locally (ADR-0004).
private const val SEED = "20261010"

class ConversionProperties {
    @Property(seed = SEED)
    fun `a valid date round-trips through its serial`(
        @ForAll @IntRange(min = MIN_YEAR, max = MAX_YEAR) year: Int,
        @ForAll @IntRange(min = 1, max = 12) month: Int,
        @ForAll @IntRange(min = 1, max = 32) day: Int,
    ) {
        val date = NepaliDate.ofOrNull(year, month, day) ?: return
        assertEquals(toSerial(year, month, day), date.serial)
        assertEquals(date, NepaliDate.ofSerial(date.serial))
        assertEquals(Triple(year, month, day), Triple(date.year, date.monthValue, date.dayOfMonth))
    }

    @Property(seed = SEED)
    fun `order, equality and hash follow the epoch day`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) a: Int,
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) b: Int,
    ) {
        val x = NepaliDate.ofSerial(a)
        val y = NepaliDate.ofSerial(b)
        assertEquals(x.toEpochDay().compareTo(y.toEpochDay()), x.compareTo(y).coerceIn(-1, 1))
        assertEquals(a == b, x == y)
        if (x == y) assertEquals(x.hashCode(), y.hashCode())
    }

    @Property(seed = SEED)
    fun `ofOrNull is null exactly when of throws`(
        @ForAll @IntRange(min = 1890, max = 2210) year: Int,
        @ForAll @IntRange(min = -1, max = 14) month: Int,
        @ForAll @IntRange(min = -1, max = 34) day: Int,
    ) {
        val thrown = runCatching { NepaliDate.of(year, month, day) }.exceptionOrNull() is NepDateException
        if (thrown) {
            assertNull(
                NepaliDate.ofOrNull(year, month, day),
            )
        } else {
            assertEquals(NepaliDate.of(year, month, day), NepaliDate.ofOrNull(year, month, day))
        }
    }
}
