# K1: Core types and BS ↔ AD conversion

Read first: ADR-0002, ADR-0003, `docs/API.md` (`NepaliDate`, `Month`, `Weekday`, `Lang`,
`NepDateException`), `shared/spec/ALGORITHM.md` §1–7, `shared/spec/PARITY.md` D-01.

All code goes in package `io.github.sanamhub.nepdate` under `nepdate/src/main/kotlin/`. No
`java.*` import in any file of this phase (AGENTS.md §2).

## Tasks

### K1-01 Generated calendar table
Extend `:codegen` to add to `Calendar.generated.kt`:
- `internal const val MONTH_LENGTHS_DATA: String`: 3588 characters, one per month from 1901-01 to
  2199-12, each `'0' + (length - 29)`. Written as 299 string literals joined with `+`, one per year,
  each followed by a `// 1901` style comment.
- `internal const val EPOCH_UNIX_DAYS: Int = -45920` and `internal const val EPOCH_WEEKDAY: Int = 4`.

Hand-written `CalendarTables.kt` (an `internal object`, ADR-0002 §2) builds in its initialiser:
- `MONTH_START: IntArray` (3589): cumulative month lengths decoded from `MONTH_LENGTHS_DATA`,
  starting at 0;
- `JAN1: IntArray` (302): the serial of 1 January of AD `1844 + i`, starting at -101 and adding 365
  or 366 by the Gregorian leap rule;
- `CUM: IntArray` (26): days before each month, common year in 0..12, leap year in 13..25.

No `intArrayOf` with more than 26 values anywhere in the core (ADR-0002 §4).

AC: `MONTH_START[3588] == 109212`; `JAN1[0] == -101`; `JAN1[300] - JAN1[299] == 365` (AD 2144);
`./gradlew :codegen:run --args=--check` passes after `./gradlew :codegen:run`.

### K1-02 `Month`, `Weekday`, `Lang`, `NepDateException`
Per `docs/API.md`. `Month.value` is 1..12; `Month.of(13)` throws `NepDateException` with kind
`INVALID_MONTH`. `displayName` and `shortName` come in K3-01. `NepDateException.monthLength` is the
length of the month for `INVALID_DAY` and 0 for every other kind. Messages name the bad input
(`"day 33 is outside 1..32 for 2081-04"`); tests check `kind`, never the message.

AC: `Month.of(4) == Month.SHRAWAN`; `Month.of(0)` and `Month.of(13)` throw `INVALID_MONTH`.

### K1-03 Serial conversion (`Serial.kt`, internal)
Four internal functions, each O(1) with no loop and no allocation (ADR-0002 §3):
- `toSerial(year, month, day): Int` = `MONTH_START[(year - 1901) * 12 + month - 1] + day - 1`.
- `packedBsOf(serial): Int`: `mi = ((serial - 25) * 2153) shr 16`, then
  `if (serial >= MONTH_START[mi + 1]) mi++`; returns `year shl 10 or month shl 6 or day`.
- `packedAdOf(serial): Int`: `i = ((serial - 40) * 5730) shr 21`, `if (serial >= JAN1[i + 1]) i++`,
  `doy = serial - JAN1[i]`, `base = 13` in a leap year else 0, `m = ((doy - 6) * 1000) shr 15`,
  `if (doy >= CUM[base + m + 1]) m++`; returns the AD date packed the same way.
- `serialOfAd(y, m, d): Int` = `JAN1[y - 1844] + CUM[base + m - 1] + d - 1` (input already valid).

Copy the constants exactly; a changed constant must fail `ExhaustiveTest` (K1-06).

AC: `toSerial(1901, 1, 1) == 0`; `toSerial(2199, 12, 30) == 109211`; `packedBsOf(0)` is 1901-01-01;
`packedAdOf(0)` is 1844-04-11; `packedBsOf(109211)` is 2199-12-30.

### K1-04 `NepaliDate`, factories, validation (`NepaliDate.kt`)
Private constructor holding `serial: Int` and the packed BS date `ymd: Int` (ADR-0002 §1). `of` and
`ofOrNull` validate per ALGORITHM §3 with its error precedence (year `OUT_OF_RANGE`, then
`INVALID_MONTH`, then `INVALID_DAY`). `ofOrNull` must not create an exception (a shared internal
check returns a kind or null; `of` throws from it). `MIN`, `MAX`, `year`, `monthValue`, `month`,
`dayOfMonth`, `equals`, `hashCode` (the serial), `compareTo`, and `toString()` = `YYYY/MM/DD`
(FORMATTING §2).

AC (PORT-PLAN phase 1): `of(2081, 4, 32)` is valid; `of(2081, 4, 33)` throws `INVALID_DAY` with
`monthLength == 32`; `of(1900, 1, 1)` throws `OUT_OF_RANGE`; `ofOrNull(2081, 4, 33) == null`.

### K1-05 Gregorian and derived properties
- `gregorianYear`, `gregorianMonth` and `gregorianDay` read `packedAdOf(serial)`; `ofGregorian`
  uses `serialOfAd` then `packedBsOf`. No Hinnant code in `src/main` (ADR-0002 §3); it lives in
  `src/test` as the reference (`HinnantReference.kt`, ALGORITHM §5 on `Long`). No `Math.floorDiv`,
  `Math.floorMod` or `Math.*Exact` anywhere (Android added them in API 24).
- `ofGregorian`: a date that doesn't exist in the Gregorian calendar (month outside 1..12, or day
  outside the month, leap years by the Gregorian rule) throws `INVALID_GREGORIAN`; a real date
  outside AD 1844-04-11..2143-04-15 throws `OUT_OF_RANGE`.
- `toEpochDay()` = serial + `EPOCH_UNIX_DAYS`. `ofEpochDay` checks `-45920..63291` before any
  arithmetic, then `OUT_OF_RANGE`.
- `gregorianYear`, `gregorianMonth`, `gregorianDay`, `dayOfWeek` (ALGORITHM §7, `Weekday` index 0 =
  `SUNDAY`), `dayOfYear`, `lengthOfMonth()`, `firstDayOfMonth()`, `lastDayOfMonth()`.

AC: `ofGregorian(1844, 4, 10)` and `ofGregorian(2143, 4, 16)` throw `OUT_OF_RANGE`;
`ofGregorian(2023, 2, 29)` throws `INVALID_GREGORIAN`; every row of ALGORITHM §9.

### K1-06 Tests
- Shared files are found through the system property `nepdate.shared`, set in
  `nepdate/build.gradle.kts` as `systemProperty("nepdate.shared", rootProject.file("shared").absolutePath)`.
  The TSV and CSV readers split on the separator only; they never trim cells (some `parse.tsv`
  inputs have spaces).
- `MonthBoundariesTest`: all 7176 rows of `shared/spec/vectors/month-boundaries.csv` (BS, AD,
  weekday, month length).
- `GoldenDatesTest`: all 1018 rows of `shared/spec/vectors/csharp-golden/dates.tsv`, columns `ad`,
  `weekday`, `day_of_year`, `month_length`.
- `ExhaustiveTest`: every serial 0..109211 round-trips through `ofEpochDay(toEpochDay())` and
  through `of(year, monthValue, dayOfMonth)`; for every serial, `java.time.LocalDate.ofEpochDay(d.toEpochDay())`
  has the same year, month and day as `gregorianYear`, `gregorianMonth`, `gregorianDay` (tests may
  use `java.time`).
- `ExhaustiveTest` also checks, for every serial, `packedAdOf` against `HinnantReference` and
  `serialOfAd(packedAdOf(s)) == s` (this proves the multiply-shift constants of K1-03).
- C# catalogue: every row tagged `1` (`00-ROADMAP.md`, "C# test catalogue":
  `NepaliDateConstructionTests`, `NepaliDatePropertiesTests`, `NepaliDateComparableTests`,
  `DictionaryIntegrityTests`, and the K1 prefixes of `OptimizationVerificationTests`), named after
  the row `id`. Rows about C#'s dictionaries or struct size assert the equivalent fact on
  `CalendarTables` with a comment saying so; rows that don't apply name the PARITY deviation.
- jqwik `ConversionProperties`: for any valid (y, m, d), `of(y, m, d).toSerial` round-trips; for any
  two dates, `compareTo` agrees with `toEpochDay` order and `equals` agrees with `hashCode`;
  `ofOrNull` returns null exactly when `of` throws.
- `ConversionAllocationTest` (ADR-0004): `of`, `ofGregorian` and `ofEpochDay` allocate at most 24
  bytes per call; `year`, `gregorianYear`, `dayOfWeek`, `lengthOfMonth()` and `toEpochDay()`
  allocate 0; `ofOrNull` on an invalid day allocates 0 (no exception built).
- Switch on Kover's rule: line coverage ≥ 90 % for `:nepdate`.

AC: the files above pass; `./scripts/ci.sh` passes.

### K1-07 `javaApiTest` source set
`nepdate/src/javaApiTest/java/io/github/sanamhub/nepdate/JavaApiTest.java`: JUnit 5, calls every
public symbol of this phase the way Java would (`NepaliDate.of(2081, 4, 15)`, `NepaliDate.MIN`,
`Month.of(4)`, catching `NepDateException` and reading `getKind()`). Register the source set and a
`Test` task `javaApiTest` that `check` depends on, and add the gate to `scripts/ci.sh`. Every later
task that adds a public symbol extends this file.

AC: removing `@JvmStatic` from `NepaliDate.of` makes `./gradlew :nepdate:javaApiTest` fail to compile.

### K1-08 Concurrent first use
`CalendarConcurrentInitTest`: 50 trials, each loading the `:nepdate` classes in a fresh
`URLClassLoader` and calling `NepaliDate.ofGregorian(2024, 7, 30)` from 16 threads released together
by a `CountDownLatch`; every thread gets 2081-04-15.

AC: the test passes 50 of 50 trials.

## Definition of Done
Roadmap DoD + milestone M1.

## Checkpoint (stop here for review)
The agent stops after K1 and posts the PR. The reviewer checks:
- [ ] `CalendarTables` builds its three tables from the string and loops only; no large array literal
      (`javap -c` on `CalendarTables` shows no long run of `iastore`).
- [ ] K1-03 functions contain no loop and match ADR-0002 §3 constant for constant.
- [ ] `HinnantReference` matches ALGORITHM §5 line for line and is only under `src/test`.
- [ ] `ExhaustiveTest` covers all 109,212 serials; note how long it takes.
- [ ] Every catalogue row tagged `1` has a test or a deviation comment (count them against
      `catalog.tsv`).
- [ ] `ConversionAllocationTest` bounds are the ones above, not looser.
- [ ] `JavaApiTest.java` calls every public symbol of this phase; `checkKotlinAbi` dump has only
      this phase's symbols.
