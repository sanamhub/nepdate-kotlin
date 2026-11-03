# ADR-0003: API conventions, errors and parity mapping

- **Status:** accepted
- **Date:** 2026-10-06, parity table revised 2026-11-03 for the hub decisions of 2026-11-02
- **Approved by:** Sanam

## Decision

**Conventions** (full surface in `docs/API.md`): names follow `java.time.LocalDate` (`of`,
`plusDays`, `dayOfWeek`, `lengthOfMonth`, `toEpochDay`) so Java and Kotlin developers guess right.
For Java: `@JvmStatic` factories on the companion, `@JvmOverloads` on functions with default
arguments, `@JvmField` constants, `@file:JvmName` on files with top-level functions, `@Throws` on
throwing functions, enums for months and weekdays. No `value class`, `suspend`, `Sequence` or `Flow`
in the public API.

**Errors:** `NepDateException` extends `IllegalArgumentException` (what `LocalDate.of` callers
already catch) and carries a `kind` enum. Kotlin callers who prefer nulls use `ofOrNull`,
`parseOrNull` and `parseLenientOrNull`.

**Time zones:** `nepaliToday()` defaults to Nepal time, and every clock member also takes a
`java.time.Clock` so tests and callers can fix the date. `LocalDate.toNepaliDate()` has no zone
question; for an `Instant` the caller picks the zone with `atZone(...)`; the library never assumes
the device zone. Every `java.time` call lives in `JavaTime.kt`, which `checkCoreSplit` enforces.

**Parity** (`shared/spec/PARITY.md`, applies unchanged):

| ID | Kotlin |
|---|---|
| D-01 | private constructor; invalid dates throw or return `null`; a range is never empty |
| D-02, D-03 | `plusMonths(n, overflow)`, `plusDays(n)`, integers only |
| D-04 | `Month.shortName` returns `Asa` / `Aso` |
| D-05, D-06 | `parse`, `parseOrNull` (strict), `parseLenient`, `parseLenientOrNull` |
| D-07 | `daysUntil(other): Long` is `other - this`; `minus(other): DateDiff` is `this - other` |
| D-08 | `FiscalYear` with `start`, `end`, `range()`, `range(quarter)` |
| D-11 | `toIsoString()` gives `"YYYY-MM-DD"`; `fromYmd`/`toYmd` for the integer; `NepaliDateSerializer` in the optional `nepdate-serialization` artifact; the core stays dependency free |
| D-12 | `yearLength()` gives the BS year length; `lengthOfMonth()` the month |
| D-13 | artifact `nepdate-patro`, package `io.github.sanamhub.nepdate.patro` (hub ADR-0004); `NepaliDate.calendarInfo` is an extension from that artifact only; `Patro` holds `coverage` and `publicHolidays` |
| D-15 | `lang` parameter on every formatter, including the range string form |
| D-16 | ranges and their helpers never throw at 2199-12-30 |
