# Changelog

All notable changes to `io.github.sanamhub:nepdate`, `io.github.sanamhub:nepdate-patro` and
`io.github.sanamhub:nepdate-serialization`. All three artifacts share one version. Format: [Keep a
Changelog](https://keepachangelog.com/en/1.1.0/), versions follow [Semantic
Versioning](https://semver.org/).

## [Unreleased]

## [0.2.0-rc.1] - 2026-11-03

The rest of the C# `NepDate` surface, so that every row of the hub's `spec/API.md` is present in
Kotlin. The public API grows and one toString and one format behaviour change; see "Breaking
changes" below.

### Added

- `NepaliDate.yearLength()`, the length of the BS year, 364 to 367. It replaces `IsLeapYear()`,
  which followed the Gregorian year of the date (PARITY D-12).
- `NepaliDate.fromYmd(Int)` and `NepaliDate.toYmd()`, the `YYYYMMDD` integer form that replaces the
  C# `TypeConverter` (PARITY D-11). The integer is split before it is validated, so no value wraps.
- Destructuring: `val (year, month, day) = date` through `component1`, `component2` and
  `component3`.
- `NepaliDate.isToday(zone)`, `isYesterday(zone)` and `isTomorrow(zone)`, each also taking a
  `java.time.Clock`, and the three `NepaliDateRange.currentMonth`, `currentFiscalYear` and
  `currentCalendarYear` factories, likewise.
- `NepaliDate.parseLenientOrNull(text)`, the non-throwing form of `parseLenient`.
- `NepaliDate.minus(other): DateDiff`, so `later - earlier` keeps the sign of the C# operator
  (PARITY D-07), and `NepaliDate.rangeTo`, so `start..end` is a `NepaliDateRange`.
- The full range surface: `singleDay`, `fromDayCount`, `currentMonth`, `currentFiscalYear`,
  `currentCalendarYear`, `containsRange`, `overlaps`, `isAdjacentTo`, `intersect`, `union`,
  `minus`, `splitByMonth`, `splitByFiscalQuarter`, `datesWithInterval`, `workingDays`,
  `weekendDays` and `toFormattedString`. Ranges reached through 2199-12-30 never throw (PARITY
  D-16), and `datesWithInterval` accepts any positive interval up to `Int.MAX_VALUE`.
- `toNepaliDates(dates)` and the two `toEnglishDates` forms: ordered batch conversion both ways,
  one function each rather than the five C# entry points.
- `io.github.sanamhub:nepdate-serialization`, an optional artifact with `NepaliDateSerializer` for
  `kotlinx.serialization`. The core keeps its `kotlin-stdlib`-only dependency.
- `docs/API.md` now documents every public item, grouped like the hub's spec, with an example each.
  The README covers dates, conversion and serialization, arithmetic, formatting, parsing, ranges,
  fiscal year and errors.

### Changed

- `NepaliDateRange.toString()` prints the two bounds, `2081/04/10 - 2081/04/20`, instead of the
  list's `[2081/04/10, ...]`. A range of years prints one line.
- `NepaliDateRange.equals` and `hashCode` compare the two bounds instead of walking every day, and
  a range is no longer equal to a plain `List` of the same days.
- `Month.value` is declared on each constant instead of derived from the declaration order, so Java
  callers and stored values keep their meaning if the constants are reordered.
- The tests read the hub's new golden vectors for the whole API: `day-diff`, `ymd-int`, `bulk`,
  `serialization`, `format-matrix`, `ranges`, `range-factories`, `range-splits`, `range-days`,
  `range-format` and `year-length`.

### Breaking changes

- A trailing backslash in a format pattern now writes a backslash, as `FORMATTING.md` section 5
  says, where it used to write nothing.
  Migration: remove the trailing `\` when the old output without it was intended.
- `NepaliDateRange.toString()` is no longer the list's `toString`.
  Migration: call `toList().toString()` where the old list text is expected.
- `NepaliDateRange` equality is no longer list equality.
  Migration: compare `range.toList()` with `==` where a plain list comparison was meant.
- `start..end` on two dates now returns a `NepaliDateRange` rather than a `ClosedRange<NepaliDate>`.
  Migration: declare the receiver as `NepaliDateRange`, or call `NepaliDateRange.of(start, end)`.

## [0.1.0-rc.1] - 2026-11-03

### Changed

- `verify-published.yml` also runs weekly with the newest version from Maven Central, once with
  Gradle (Kotlin DSL) and once with Maven, and daily against Central's metadata. A failure is one
  issue labelled `published-check`.

## [0.1.0-beta.1] - 2026-10-22

First public pre-release. The API may still change before 0.1.0.

### Added
- `NepaliDate` for BS 1901-01-01 to 2199-12-30: `of`, `ofOrNull`, `ofGregorian`, `ofEpochDay`,
  `MIN`, `MAX`, the BS and AD fields, `dayOfWeek`, `dayOfYear`, `lengthOfMonth()`,
  `firstDayOfMonth()`, `lastDayOfMonth()`, `toEpochDay()`, ordering and `toString()` as `YYYY/MM/DD`.
- `Month`, `Weekday`, `Lang` and `NepDateException` with its `kind`.
- Arithmetic: `plusDays`, `minusDays`, `daysUntil`, `plusMonths` and `plusYears` with
  `MonthOverflow.CLAMP` or `SPILL`, and `until` returning a `DateDiff` (years, months, days).
- `FiscalYear` (1 Shrawan to the end of Ashad) with quarters `Q1`..`Q4`, `NepaliDate.fiscalYear` and
  `NepaliDate.quarter`.
- `NepaliDateRange`: the days of a month, a year or any span, as a read-only `List<NepaliDate>`.
- Names and formatting: `Month` and `Weekday` `displayName` and `shortName` in English and Nepali,
  `nepaliDigits`, `toShortString`, `toLongString`, `toIsoString`, `format` with .NET-style patterns,
  and the `append*` and `formatTo` forms that write into a caller's `StringBuilder`.
- Parsing: strict `parse` and `parseOrNull` (ASCII and Devanagari digits), and `parseLenient` for
  human input with month names, era markers and day-month-year order.
- Patro (`nepdate-patro`): tithi, public holidays and events for BS 2077-01-01 to 2084-08-20 via
  `Patro.info`, `Patro.coverage`, `Patro.publicHolidays`, `CalendarInfo`, `Named`, and the
  `NepaliDate.calendarInfo` extension. The core `nepdate` artifact contains none of this data.
- `java.time` interop: `NepaliDate.toLocalDate`, `LocalDate.toNepaliDate`, `nepaliToday` (Nepal
  time by default) and the `KATHMANDU` zone.
