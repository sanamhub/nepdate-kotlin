package io.github.sanamhub.nepdate

import io.github.sanamhub.nepdate.CalendarTables.CUM
import io.github.sanamhub.nepdate.CalendarTables.JAN1
import io.github.sanamhub.nepdate.CalendarTables.JAN1_BASE_YEAR
import io.github.sanamhub.nepdate.CalendarTables.LEAP_BASE
import io.github.sanamhub.nepdate.CalendarTables.MONTH_START

// The constant-time conversions of ADR-0002 section 3. Each multiply-shift estimate is at most one
// step low, and one compare fixes it; ExhaustiveTest checks that for every serial, so changing a
// constant here fails it.

/** Last valid serial: BS 2199-12-30. */
internal const val MAX_SERIAL: Int = 109211

/** Number of BS years supported, 1901..2199. */
internal const val YEARS: Int = MAX_YEAR - MIN_YEAR + 1

// Day 32 needs six bits, so the day takes bits 0..5 and the month bits 6..9. ADR-0002 section 1
// writes `year shl 9 or month shl 5 or day`, where day 32 would set the month's lowest bit.
internal fun pack(
    year: Int,
    month: Int,
    day: Int,
): Int = (year shl 10) or (month shl 6) or day

internal fun packedYear(packed: Int): Int = packed ushr 10

internal fun packedMonth(packed: Int): Int = (packed ushr 6) and 0xF

internal fun packedDay(packed: Int): Int = packed and 0x3F

/** Serial of a valid BS date. */
internal fun toSerial(
    year: Int,
    month: Int,
    day: Int,
): Int = MONTH_START[(year - MIN_YEAR) * 12 + month - 1] + day - 1

/** The BS date of a valid serial, packed. */
internal fun packedBsOf(serial: Int): Int {
    var mi = ((serial - 25) * 2153) shr 16
    if (serial >= MONTH_START[mi + 1]) mi++
    return pack(MIN_YEAR + mi / 12, mi % 12 + 1, serial - MONTH_START[mi] + 1)
}

/** The AD date of a valid serial, packed. */
internal fun packedAdOf(serial: Int): Int {
    var i = ((serial - 40) * 5730) shr 21
    if (serial >= JAN1[i + 1]) i++
    val year = JAN1_BASE_YEAR + i
    val doy = serial - JAN1[i]
    val base = if (isGregorianLeap(year)) LEAP_BASE else 0
    var m = ((doy - 6) * 1000) shr 15
    if (doy >= CUM[base + m + 1]) m++
    return pack(year, m + 1, doy - CUM[base + m] + 1)
}

/** Serial of a real AD date inside the JAN1 table; the caller checks the result's range. */
internal fun serialOfAd(
    year: Int,
    month: Int,
    day: Int,
): Int {
    val base = if (isGregorianLeap(year)) LEAP_BASE else 0
    return JAN1[year - JAN1_BASE_YEAR] + CUM[base + month - 1] + day - 1
}
