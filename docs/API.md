# `nepdate` and `nepdate-patro` (Kotlin): public API contract (v0.1)

The complete public surface of both artifacts for v0.1. Implement exactly this; a new public symbol needs this file
changed in the same PR, approved by the human. Behaviour: `shared/spec/`. Conventions: ADR-0003.
Names follow `java.time.LocalDate` where a counterpart exists. Every type is immutable and
thread-safe.

```kotlin
// ---------- artifact io.github.sanamhub:nepdate, package io.github.sanamhub.nepdate ----------
public class NepaliDate private constructor(/* serial: Int, ymd: Int; ADR-0002 §1 */) :
    Comparable<NepaliDate> {
    public val year: Int
    public val monthValue: Int                                // 1..12
    public val month: Month
    public val dayOfMonth: Int
    public val dayOfWeek: Weekday                             // Sunday-first
    public val dayOfYear: Int
    public fun lengthOfMonth(): Int                           // 29..32
    public fun firstDayOfMonth(): NepaliDate
    public fun lastDayOfMonth(): NepaliDate

    public val gregorianYear: Int
    public val gregorianMonth: Int
    public val gregorianDay: Int
    public fun toEpochDay(): Long                             // days since 1970-01-01, as LocalDate

    public fun plusDays(days: Long): NepaliDate
    @JvmOverloads public fun plusMonths(months: Long, overflow: MonthOverflow = MonthOverflow.CLAMP): NepaliDate
    @JvmOverloads public fun plusYears(years: Long, overflow: MonthOverflow = MonthOverflow.CLAMP): NepaliDate
    public fun minusDays(days: Long): NepaliDate
    public fun daysUntil(other: NepaliDate): Long             // other - this (D-07)
    public fun until(other: NepaliDate): DateDiff             // ALGORITHM §8a
    public val fiscalYear: FiscalYear
    public val quarter: Quarter

    override fun toString(): String                           // "YYYY/MM/DD"
    public fun toIsoString(): String                          // "YYYY-MM-DD" (D-11)
    @JvmOverloads public fun toShortString(order: DateOrder = DateOrder.YMD, separator: Separator = Separator.SLASH,
                                           pad: Boolean = true, lang: Lang = Lang.ENGLISH): String
    @JvmOverloads public fun toLongString(weekday: Boolean = false, year: Boolean = true, pad: Boolean = true,
                                          lang: Lang = Lang.ENGLISH): String
    @JvmOverloads public fun format(pattern: String, lang: Lang = Lang.ENGLISH): String  // FORMATTING §5
    // The same three formats appended to the caller's builder; return `out`. No allocation once
    // `out` has capacity (FORMATTING: "implementations write into a stream/buffer").
    @JvmOverloads public fun appendShortString(out: StringBuilder, order: DateOrder = DateOrder.YMD,
                                               separator: Separator = Separator.SLASH, pad: Boolean = true,
                                               lang: Lang = Lang.ENGLISH): StringBuilder
    @JvmOverloads public fun appendLongString(out: StringBuilder, weekday: Boolean = false, year: Boolean = true,
                                              pad: Boolean = true, lang: Lang = Lang.ENGLISH): StringBuilder
    @JvmOverloads public fun formatTo(out: StringBuilder, pattern: String, lang: Lang = Lang.ENGLISH): StringBuilder

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
    override fun compareTo(other: NepaliDate): Int

    public companion object {
        @JvmField public val MIN: NepaliDate                  // 1901-01-01
        @JvmField public val MAX: NepaliDate                  // 2199-12-30
        @JvmStatic public fun of(year: Int, month: Int, day: Int): NepaliDate              // throws
        @JvmStatic public fun of(year: Int, month: Month, day: Int): NepaliDate
        @JvmStatic public fun ofOrNull(year: Int, month: Int, day: Int): NepaliDate?
        @JvmStatic public fun ofGregorian(year: Int, month: Int, day: Int): NepaliDate     // throws
        @JvmStatic public fun ofEpochDay(epochDay: Long): NepaliDate                       // throws
        @JvmStatic public fun parse(text: CharSequence): NepaliDate          // PARSING §1, throws
        @JvmStatic public fun parseLenient(text: CharSequence): NepaliDate   // PARSING §2, throws
        @JvmStatic public fun parseOrNull(text: CharSequence): NepaliDate?
    }
}

public enum class Lang { ENGLISH, NEPALI }
public enum class Month {
    BAISHAKH, JESTHA, ASHAD, SHRAWAN, BHADRA, ASHOJ, KARTIK, MANGSIR, POUSH, MAGH, FALGUN, CHAITRA;
    public val value: Int                                     // 1..12
    @JvmOverloads public fun displayName(lang: Lang = Lang.ENGLISH): String     // FORMATTING §1
    @JvmOverloads public fun shortName(lang: Lang = Lang.ENGLISH): String       // D-04: Asa / Aso
    public companion object { @JvmStatic public fun of(value: Int): Month }     // throws
}
public enum class Weekday {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
    @JvmOverloads public fun displayName(lang: Lang = Lang.ENGLISH): String
    @JvmOverloads public fun shortName(lang: Lang = Lang.ENGLISH): String
}

// file NepaliDigits.kt, @file:JvmName("NepaliDigits")
@JvmOverloads public fun nepaliDigits(n: Long, minWidth: Int = 0): String

public class NepDateException internal constructor(
    public val kind: Kind,
    public val monthLength: Int,                              // set for INVALID_DAY, else 0
    message: String,
) : IllegalArgumentException(message) {
    public enum class Kind {
        OUT_OF_RANGE, INVALID_MONTH, INVALID_DAY, INVALID_GREGORIAN,
        INVALID_CHARACTER, WRONG_GROUP_COUNT, NUMBER_TOO_LONG, AMBIGUOUS, UNRECOGNIZED,
    }
}

public enum class MonthOverflow { CLAMP, SPILL }              // ALGORITHM §8
public class DateDiff internal constructor(/* ... */) {
    public val isNegative: Boolean
    public val years: Int; public val months: Int; public val days: Int; public val totalDays: Long
    // equals, hashCode, toString
}
public enum class DateOrder { YMD, YDM, MYD, MDY, DYM, DMY }
public enum class Separator(public val char: Char) { SLASH('/'), BACKSLASH('\\'), DOT('.'), UNDERSCORE('_'), DASH('-'), SPACE(' ') }

public class FiscalYear(public val startYear: Int) : Comparable<FiscalYear> {   // FY 2082/83 = FiscalYear(2082); equals, hashCode; toString() = label()
    public fun start(): NepaliDate                            // throws OUT_OF_RANGE
    public fun end(): NepaliDate
    public fun range(): NepaliDateRange
    public fun range(quarter: Quarter): NepaliDateRange
    @JvmOverloads public fun label(lang: Lang = Lang.ENGLISH): String  // "2082/83" / "२०८२/८३"
    public companion object { @JvmStatic public fun containing(date: NepaliDate): FiscalYear }
}
public enum class Quarter { Q1, Q2, Q3, Q4; public val months: List<Month> }

public class NepaliDateRange internal constructor(/* first and last serial */) :
    AbstractList<NepaliDate>(), RandomAccess {                // inclusive; D-09
    public val start: NepaliDate
    public val endInclusive: NepaliDate
    // List<NepaliDate>: size, get(index), contains(element) are O(1)
    public companion object {
        @JvmStatic public fun of(start: NepaliDate, endInclusive: NepaliDate): NepaliDateRange  // OUT_OF_RANGE if start > end
        @JvmStatic public fun ofMonth(year: Int, month: Int): NepaliDateRange
        @JvmStatic public fun ofYear(year: Int): NepaliDateRange
    }
}

// file JavaTime.kt, @file:JvmName("NepaliDateJavaTime"); the only file that touches java.time
public fun NepaliDate.toLocalDate(): java.time.LocalDate
public fun java.time.LocalDate.toNepaliDate(): NepaliDate                         // throws
@JvmOverloads public fun nepaliToday(zone: java.time.ZoneId = KATHMANDU): NepaliDate
public fun nepaliToday(clock: java.time.Clock): NepaliDate                         // date in clock's zone
@JvmField public val KATHMANDU: java.time.ZoneId              // Asia/Kathmandu, or fixed +05:45 if the zone data lacks it

// ---------- artifact io.github.sanamhub:nepdate-patro, package io.github.sanamhub.nepdate.patro ----------
// Depends on io.github.sanamhub:nepdate (same version). Nothing in the core refers to this package (hub ADR-0004).
public object Patro {
    @JvmStatic public fun info(date: NepaliDate): CalendarInfo   // PATRO §2 `info`; never throws, never allocates
    @JvmStatic public val coverage: NepaliDateRange              // 2077-01-01..2084-08-20 today
    @JvmStatic public val publicHolidays: List<NepaliDate>       // ascending, unmodifiable, built on first use
}
public class CalendarInfo internal constructor(/* ... */) {   // equals, hashCode, toString
    public val tithi: Named?
    public val isPublicHoliday: Boolean
    public val events: List<Named>                            // file order, unmodifiable
}
public class Named internal constructor(public val nepali: String, public val english: String) {   // equals, hashCode, toString
    @JvmOverloads public fun name(lang: Lang = Lang.ENGLISH): String
}
// file PatroExtensions.kt, @file:JvmName("NepaliDatePatro")
public val NepaliDate.calendarInfo: CalendarInfo                                   // = Patro.info(this)
```

Notes:
- `of`, `ofGregorian`, `ofEpochDay`, `parse`, `parseLenient`, `plus*` and the range factories throw
  `NepDateException` (a subclass of `IllegalArgumentException`, the `java.time` habit), each marked
  `@Throws(NepDateException::class)` for Java. `ofOrNull` and `parseOrNull` are the Kotlin-idiomatic
  forms.
- `toEpochDay`/`ofEpochDay` use the `LocalDate` epoch, so Android code below API 26 can convert
  without `java.time` or desugaring.
- `NepaliDate` is a plain class, not a `data class` (no `copy` or `componentN` in the ABI) and not a
  `value class` (Java can't call it cleanly).
- `nepaliToday()` with no argument uses Nepal time, not the device zone. The `Clock` overload is for tests.
- In Java, the `java.time` extensions are static methods on `NepaliDateJavaTime`, for example
  `NepaliDateJavaTime.toLocalDate(date)` and `NepaliDateJavaTime.nepaliToday()`.
- Patro (`shared/spec/PATRO.md` §2): `Patro.info`, `Patro.coverage` and `Patro.publicHolidays` are
  the three queries; `date.calendarInfo` is the Kotlin shorthand for `Patro.info(date)` (import
  `io.github.sanamhub.nepdate.patro.calendarInfo`). In Java: `Patro.info(date)`,
  `Patro.getCoverage()`, `Patro.getPublicHolidays()`.
- `Patro.info` returns a shared, immutable `CalendarInfo` built once per day (ADR-0002 §5): the same
  day gives the same instance, and `events` can't be modified. Compare with `equals`, not `===`.
- Allocation contract (ADR-0002 §7, tested): conversion, arithmetic and parsing allocate only the
  returned `NepaliDate`; `Patro.info`, the `append*` functions and `formatTo` into a builder with
  enough capacity allocate nothing; the `String` formatters allocate the result.
