# ADR-0003: API conventions, errors and parity mapping

- Status: Accepted
- Date: 2026-10-06
- Deciders: Sanam Pakuwal

## Decision

**Conventions** (full surface in `docs/API.md`): names follow `java.time.LocalDate` (`of`,
`plusDays`, `dayOfWeek`, `lengthOfMonth`, `toEpochDay`) so Java and Kotlin developers guess right.
For Java: `@JvmStatic` factories on the companion, `@JvmOverloads` on functions with default
arguments, `@JvmField` constants, `@file:JvmName` on files with top-level functions, `@Throws` on
throwing functions, enums for months and weekdays. No `value class`, `suspend`, `Sequence` or `Flow`
in the public API.

**Errors:** `NepDateException` extends `IllegalArgumentException` (what `LocalDate.of` callers
already catch) and carries a `kind` enum. Kotlin callers who prefer nulls use `ofOrNull` and
`parseOrNull`.

**Time zones:** `nepaliToday()` defaults to Nepal time. `LocalDate.toNepaliDate()` has no zone
question; for an `Instant` the caller picks the zone with `atZone(...)`; the library never assumes the
device zone.

**Parity** (`shared/spec/PARITY.md`, applies unchanged):

| ID | Kotlin |
|---|---|
| D-01 | private constructor; invalid dates throw or return `null` |
| D-02, D-03 | `plusMonths(n, overflow)`, `plusDays(n)`, integers only |
| D-04 | `Month.shortName` returns `Asa` / `Aso` |
| D-05, D-06 | `parse`, `parseOrNull` (strict), `parseLenient` |
| D-07 | `daysUntil(other): Long` |
| D-08 | `FiscalYear` with `start`, `end`, `range(quarter)` |
| D-09 | `NepaliDateRange` as a `List` only |
| D-10 | not ported |
| D-11 | `toIsoString()` gives `"YYYY-MM-DD"`; no serialization library dependency |
| D-12 | `lengthOfMonth()`; year length via `NepaliDateRange.ofYear(y).size` |
| D-13 | artifact `nepdate-patro`, package `io.github.sanamhub.nepdate.patro` (hub ADR-0004); `NepaliDate.calendarInfo` is an extension from that artifact only; `Patro` holds `coverage` and `publicHolidays` |
| D-14 | not ported; `nepaliToday()` and `plusDays(±1)` |
| D-15 | `lang` parameter on every formatter |
