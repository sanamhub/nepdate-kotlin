@file:JvmName("NepaliDateJavaTime")

package io.github.sanamhub.nepdate

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.zone.ZoneRulesException

/**
 * Nepal's time zone (UTC+05:45, no daylight saving). Built from `Asia/Kathmandu` when the JDK's
 * time-zone data has it; on a JDK whose zone data is stripped down, it falls back to a fixed
 * `+05:45` offset, so "today in Nepal" still works (K4-07).
 */
@Suppress("SwallowedException") // the fallback is the point: a JDK without the zone data still gets Nepal time
@JvmField
public val KATHMANDU: ZoneId =
    try {
        ZoneId.of("Asia/Kathmandu")
    } catch (e: ZoneRulesException) {
        ZoneOffset.ofHoursMinutes(5, 45)
    }

/**
 * This date as a `java.time.LocalDate`, through [NepaliDate.toEpochDay].
 *
 * @return the same day in the ISO calendar, always valid (BS 1901-01-01..2199-12-30 is inside
 *   `LocalDate`'s range).
 */
public fun NepaliDate.toLocalDate(): LocalDate = LocalDate.ofEpochDay(toEpochDay())

/**
 * The BS date of this `java.time.LocalDate`, through [NepaliDate.ofEpochDay].
 *
 * @return the same day in the Bikram Sambat calendar.
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when the date is outside
 *   AD 1844-04-11..2143-04-15.
 */
@Throws(NepDateException::class)
public fun LocalDate.toNepaliDate(): NepaliDate = NepaliDate.ofEpochDay(toEpochDay())

/**
 * Today's date in [zone]. With no argument it uses Nepal time ([KATHMANDU]), never the device zone
 * (ADR-0003).
 *
 * @return today's BS date in [zone].
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   AD 1844-04-11..2143-04-15.
 */
@JvmOverloads
@Throws(NepDateException::class)
public fun nepaliToday(zone: ZoneId = KATHMANDU): NepaliDate = LocalDate.now(zone).toNepaliDate()

/**
 * Today's date in [clock]'s zone, for tests and callers that already hold a clock.
 *
 * @return today's BS date in the clock's zone.
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when the clock's date is
 *   outside AD 1844-04-11..2143-04-15.
 */
@Throws(NepDateException::class)
public fun nepaliToday(clock: Clock): NepaliDate = LocalDate.now(clock).toNepaliDate()
