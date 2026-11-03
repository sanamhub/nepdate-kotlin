// Every java.time call in the core lives in this one class, because checkCoreSplit fails any other
// class that references java.time (Android below API 26 has no java.time, ADR-0004 gate 8). The
// clock, zone, batch and java.time-bridge functions therefore share one file, which is why it has
// more functions than the detekt default allows.
@file:JvmName("NepaliDateJavaTime")
@file:Suppress("TooManyFunctions")

package io.github.sanamhub.nepdate

import java.time.Clock
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.zone.ZoneRulesException

/**
 * Nepal's time zone (UTC+05:45, no daylight saving). Built from `Asia/Kathmandu` when the JDK's
 * time-zone data has it; on a JDK whose zone data is stripped down, it falls back to a fixed
 * `+05:45` offset, so "today in Nepal" still works.
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

/**
 * Whether this date is today in [zone], C# `IsToday`.
 *
 * @return true when [zone]'s current date is this date
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   the supported range
 */
@Throws(NepDateException::class)
public fun NepaliDate.isToday(zone: ZoneId): Boolean = serial == nepaliToday(zone).serial

/**
 * Whether this date is yesterday in [zone], C# `IsYesterday`.
 *
 * @return true when the day before [zone]'s current date is this date
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   the supported range
 */
@Throws(NepDateException::class)
public fun NepaliDate.isYesterday(zone: ZoneId): Boolean = serial == nepaliToday(zone).serial - 1

/**
 * Whether this date is tomorrow in [zone], C# `IsTomorrow`.
 *
 * @return true when the day after [zone]'s current date is this date
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   the supported range
 */
@Throws(NepDateException::class)
public fun NepaliDate.isTomorrow(zone: ZoneId): Boolean = serial == nepaliToday(zone).serial + 1

/**
 * Whether this date is today in [clock]'s zone, the testable form of [isToday].
 *
 * @return true when the clock's current date is this date
 */
@Throws(NepDateException::class)
public fun NepaliDate.isToday(clock: Clock): Boolean = serial == nepaliToday(clock).serial

/**
 * Whether this date is yesterday in [clock]'s zone, the testable form of [isYesterday].
 *
 * @return true when the day before the clock's current date is this date
 */
@Throws(NepDateException::class)
public fun NepaliDate.isYesterday(clock: Clock): Boolean = serial == nepaliToday(clock).serial - 1

/**
 * Whether this date is tomorrow in [clock]'s zone, the testable form of [isTomorrow].
 *
 * @return true when the day after the clock's current date is this date
 */
@Throws(NepDateException::class)
public fun NepaliDate.isTomorrow(clock: Clock): Boolean = serial == nepaliToday(clock).serial + 1

/**
 * The BS dates of [dates], in input order. This is the one batch conversion this way; C#'
 * `BulkConvert.ToNepaliDates` and `BatchProcessToNepaliDates` are the same operation here, and the
 * `useParallel` and `batchSize` arguments do not cross into the port.
 *
 * @return one date per input, in the order given
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] for the first input outside
 *   AD 1844-04-11..2143-04-15
 */
@Throws(NepDateException::class)
public fun toNepaliDates(dates: Iterable<LocalDate>): List<NepaliDate> {
    val out = ArrayList<NepaliDate>(sizeHint(dates))
    for (date in dates) out.add(date.toNepaliDate())
    return out
}

/**
 * The AD dates of [strings], in input order. Each string is read by the strict parser, as C#
 * `BulkConvert.ToEnglishDates` does.
 *
 * @return one Gregorian date per input, in the order given
 * @throws NepDateException when a string does not parse, so no partial list is returned
 */
@JvmName("toEnglishDatesFromStrings")
@Throws(NepDateException::class)
public fun toEnglishDates(strings: Iterable<String>): List<LocalDate> {
    val out = ArrayList<LocalDate>(sizeHint(strings))
    for (text in strings) out.add(NepaliDate.parse(text).toLocalDate())
    return out
}

/**
 * The AD dates of [dates], in input order, C# `BulkConvert.ToEnglishDates`. This is the one batch
 * conversion this way; `BatchProcessToEnglishDates` is the same operation here.
 *
 * @return one Gregorian date per input, in the order given
 */
@JvmName("toEnglishDatesFromDates")
public fun toEnglishDates(dates: Iterable<NepaliDate>): List<LocalDate> {
    val out = ArrayList<LocalDate>(sizeHint(dates))
    for (date in dates) out.add(date.toLocalDate())
    return out
}

/**
 * Every day of the BS month [zone] is in now, C# `CurrentMonth`.
 *
 * @return the current month's range
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   the supported range
 */
@JvmOverloads
@Throws(NepDateException::class)
public fun NepaliDateRange.Companion.currentMonth(zone: ZoneId = KATHMANDU): NepaliDateRange {
    val today = nepaliToday(zone)
    return NepaliDateRange.ofMonth(today.year, today.monthValue)
}

/**
 * Every day of the fiscal year [zone] is in now, C# `CurrentFiscalYear`.
 *
 * @return the current fiscal year's range
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   the supported range
 */
@JvmOverloads
@Throws(NepDateException::class)
public fun NepaliDateRange.Companion.currentFiscalYear(zone: ZoneId = KATHMANDU): NepaliDateRange =
    FiscalYear.containing(nepaliToday(zone)).range()

/**
 * Every day of the BS year [zone] is in now, C# `CurrentCalendarYear`.
 *
 * @return the current BS year's range
 * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when today's date is outside
 *   the supported range
 */
@JvmOverloads
@Throws(NepDateException::class)
public fun NepaliDateRange.Companion.currentCalendarYear(zone: ZoneId = KATHMANDU): NepaliDateRange =
    NepaliDateRange.ofYear(nepaliToday(zone).year)

/**
 * Every day of the BS month [clock] is in now, the testable form of [currentMonth].
 *
 * @return the current month's range
 */
@Throws(NepDateException::class)
public fun NepaliDateRange.Companion.currentMonth(clock: Clock): NepaliDateRange {
    val today = nepaliToday(clock)
    return NepaliDateRange.ofMonth(today.year, today.monthValue)
}

/**
 * Every day of the fiscal year [clock] is in now, the testable form of [currentFiscalYear].
 *
 * @return the current fiscal year's range
 */
@Throws(NepDateException::class)
public fun NepaliDateRange.Companion.currentFiscalYear(clock: Clock): NepaliDateRange = FiscalYear.containing(nepaliToday(clock)).range()

/**
 * Every day of the BS year [clock] is in now, the testable form of [currentCalendarYear].
 *
 * @return the current BS year's range
 */
@Throws(NepDateException::class)
public fun NepaliDateRange.Companion.currentCalendarYear(clock: Clock): NepaliDateRange = NepaliDateRange.ofYear(nepaliToday(clock).year)

/** The known size of [items] when it has one, so the result list is not grown repeatedly. */
private fun sizeHint(items: Iterable<*>): Int = if (items is Collection<*>) items.size else 10
