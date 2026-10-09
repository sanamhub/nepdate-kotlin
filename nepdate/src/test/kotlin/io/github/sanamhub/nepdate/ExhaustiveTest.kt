package io.github.sanamhub.nepdate

import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/** Every one of the 109,212 supported days, in both directions. */
class ExhaustiveTest {
    @Test
    fun `every serial round-trips through epoch days and through year, month and day`() {
        for (serial in 0..MAX_SERIAL) {
            val date = NepaliDate.ofEpochDay((serial + EPOCH_UNIX_DAYS).toLong())
            assertEquals(serial, date.serial)
            assertEquals(date, NepaliDate.ofEpochDay(date.toEpochDay()))
            assertEquals(date, NepaliDate.of(date.year, date.monthValue, date.dayOfMonth))
        }
    }

    @Test
    fun `every serial agrees with java time`() {
        for (serial in 0..MAX_SERIAL) {
            val date = NepaliDate.ofEpochDay((serial + EPOCH_UNIX_DAYS).toLong())
            val local = LocalDate.ofEpochDay(date.toEpochDay())
            if (local.year != date.gregorianYear ||
                local.monthValue != date.gregorianMonth ||
                local.dayOfMonth != date.gregorianDay
            ) {
                assertEquals(local.toString(), "${date.gregorianYear}-${date.gregorianMonth}-${date.gregorianDay}")
            }
            if (NepaliDate.ofGregorian(local.year, local.monthValue, local.dayOfMonth) != date) {
                assertEquals(date, NepaliDate.ofGregorian(local.year, local.monthValue, local.dayOfMonth))
            }
        }
    }

    @Test
    fun `table conversion agrees with Hinnant for every serial`() {
        // This proves the multiply-shift constants of K1-03: a wrong one fails here.
        for (serial in 0..MAX_SERIAL) {
            val packed = packedAdOf(serial)
            val (y, m, d) = HinnantReference.civilFromDays((serial + EPOCH_UNIX_DAYS).toLong())
            if (packedYear(packed).toLong() != y || packedMonth(packed).toLong() != m || packedDay(packed).toLong() != d) {
                assertEquals("$y-$m-$d", "${packedYear(packed)}-${packedMonth(packed)}-${packedDay(packed)}", "serial $serial")
            }
            assertEquals(serial, serialOfAd(packedYear(packed), packedMonth(packed), packedDay(packed)), "serial $serial")
            assertEquals(
                (serial + EPOCH_UNIX_DAYS).toLong(),
                HinnantReference.daysFromCivil(y, m, d),
                "serial $serial",
            )
        }
    }

    @Test
    fun `serial to BS agrees with a walk over month lengths`() {
        var serial = 0
        for (year in MIN_YEAR..MAX_YEAR) {
            for (month in 1..12) {
                for (day in 1..bsMonthLength(year, month)) {
                    val packed = packedBsOf(serial)
                    if (packed != pack(year, month, day)) {
                        assertEquals("$year-$month-$day", "${packedYear(packed)}-${packedMonth(packed)}-${packedDay(packed)}")
                    }
                    assertEquals(serial, toSerial(year, month, day))
                    serial++
                }
            }
        }
        assertEquals(109212, serial)
    }
}
