# K4: Patro artifact and `java.time` interop

Read first: hub ADR-0004 (patro in a separate package), `shared/spec/PATRO.md`, ADR-0001 §3,
ADR-0002 (§4 and §5 for the patro layout, §8 for the core boundary), ADR-0004 gate 7,
`docs/API.md` (`JavaTime.kt` and the `nepdate-patro` section), PARITY D-13.

The core never refers to `io.github.sanamhub.nepdate.patro`, and nothing from `shared/data/patro/`
goes into `:nepdate`, not even its tests.

## Tasks

### K4-01 Module `:nepdate-patro`
`include(":nepdate-patro")` in `settings.gradle.kts`. `nepdate-patro/build.gradle.kts` has the
same Kotlin, ABI, Kover, ktlint, detekt and publishing setup as `:nepdate` (move the shared part to
a convention plugin in `build-logic/` if it is more than a few lines), `api(project(":nepdate"))`,
artifact id `nepdate-patro`, POM description "Patro (Nepali almanac: tithi, public holidays,
events) for nepdate", and its own `javaApiTest` source set (ADR-0004 gate 6). Package
`io.github.sanamhub.nepdate.patro`.

AC: `./gradlew :nepdate-patro:generatePomFileForMavenPublication` writes a POM with exactly two
dependencies, `kotlin-stdlib` and `io.github.sanamhub:nepdate` at the version in
`gradle.properties`.

### K4-02 Generated patro tables (`Patro.generated.kt`)
Extend `:codegen` to read every `shared/data/patro/*.json` (ascending year) and write
`nepdate-patro/src/main/kotlin/io/github/sanamhub/nepdate/patro/Patro.generated.kt`. All values
are `internal`. Serials follow ALGORITHM §4; `BASE_SERIAL` is the serial of the first day with any
data (2077-01-01). Offsets are `serial - BASE_SERIAL`.

| Name | Content |
|---|---|
| `BASE_SERIAL: Int` | serial of 2077-01-01 (64285) |
| `SPAN: Int` | `LAST_SERIAL - BASE_SERIAL + 1`, where `LAST_SERIAL` is the last day with any data, 2084-08-20 (2793) |
| `TITHI_FIRST_OFFSET: Int` | offset of the first tithi day, 2078-01-02 |
| `TITHI_INDEX_DATA: String` | one char per day from `TITHI_FIRST_OFFSET`, `'0' + i`, where `i` is the pair index (0..31) in `NAMES` |
| `HOLIDAY_DATA: String` | one char per holiday, its offset, ascending |
| `EVENT_DAY_DATA: String` | one char per day with events, its offset, ascending |
| `EVENT_START_DATA: String` | event-day count + 1 chars: index of each day's first event in `EVENT_NAME_DATA`; the last is the total |
| `EVENT_NAME_DATA: String` | one char per event in file order, its pair index in `NAMES` |
| `NAMES: Array<String>` | the deduplicated (np, en) pairs flattened: `np0, en0, np1, en1, ...`; first the 32 tithi pairs in order of first appearance, then the event pairs not already listed, in order of first appearance |

Why strings and not `shortArrayOf(...)`: ADR-0002 §4 (measured).

Rules for the generator:
- Each `*_DATA` constant is written as string literals of at most 20 values joined with `+`, split
  only between values. Every character that isn't an ASCII letter or digit is written as `\uXXXX`.
  Every encoded value must be below `0xD800` (no surrogates); fail otherwise.
- In `NAMES`, Devanagari stays as UTF-8 text; only `\`, `"` and `$` are escaped. One string literal
  per element, so each name, `"World Day Against Human Trafficking"` included, is a separate
  constant in the class file (K4-04 depends on it).
- Fail if a tithi day is missing inside the tithi range (PATRO §3), or if there are more than 32
  distinct tithi pairs.

AC: T = 2190 tithi days, H = 294 holidays, E = 1671 event days, 3032 events, 753 distinct pairs
(PATRO §3), each asserted on the decoded tables; `BASE_SERIAL == 64285` and `SPAN == 2793`;
`./gradlew :codegen:run --args=--check` passes.

### K4-03 `PatroTables.kt`: decode once, build every answer once
Hand-written `internal object PatroTables`, initialised on first use (JVM class initialisation is
lazy and thread-safe, JLS §12.4.2). Its initialiser, in this order:
1. Builds `pairs: Array<Named>` (753) from `NAMES`.
2. Builds `events: Array<Named>` (3032) from `EVENT_NAME_DATA`, sharing the `pairs` objects.
3. Builds `infoByDay: Array<CalendarInfo>` of `SPAN` entries in one pass over the offsets, walking
   `HOLIDAY_DATA` and `EVENT_DAY_DATA` with two cursors (no binary search):
   - a day with events gets its own `CalendarInfo` whose `events` is an `EventList` (below);
   - a holiday without events gets its own `CalendarInfo` with `emptyList()`;
   - a day with only a tithi shares one `CalendarInfo` per tithi pair (32 at most);
   - a day with nothing shares `CalendarInfo.EMPTY` (`tithi = null`, `false`, `emptyList()`).

`EventList` is an `internal class EventList(all: Array<Named>, from: Int, to: Int) :
AbstractList<Named>(), RandomAccess`: a read-only view over a slice of `events`, so no event array
is copied. `get` outside `0 until size` throws `IndexOutOfBoundsException`.

`Patro.publicHolidays` lives in a separate `private object HolidayList` holder, so the 294
`NepaliDate` objects are only created when someone asks for them; it is built once, ascending, as an
unmodifiable list (`java.util.Collections.unmodifiableList` over an `ArrayList`).

AC: `PatroTables.infoByDay.size == 2793`; the number of distinct `CalendarInfo` instances in it is
at most 1671 + 294 + 32 + 1 (checked with an identity set); the structure tests of
`CalendarDataTests` in the catalogue pass against these tables.

### K4-04 `Patro`, `CalendarInfo`, `Named`, `calendarInfo`
Per `docs/API.md` and PATRO §2. The serial comes from the public API (ADR-0002 §8):
`serial = date.toEpochDay().toInt() + 45920`. `Patro.info(date)` is:

```kotlin
val offset = date.toEpochDay().toInt() + 45920 - BASE_SERIAL
return if (offset < 0 || offset >= SPAN) CalendarInfo.EMPTY else PatroTables.infoByDay[offset]
```

`NepaliDate.calendarInfo` (file `PatroExtensions.kt`, `@file:JvmName("NepaliDatePatro")`) returns
`Patro.info(this)`. `Patro.coverage` is `NepaliDateRange.of(first, last)` built once.

AC (PORT-PLAN phase 4, PATRO §4): every row of the PATRO §4 anchor table (tithi English name,
holiday flag, event English names in order), including the empty results for 2070-06-15 and
2090-01-01; `Patro.coverage` is 2077-01-01..2084-08-20; `Patro.publicHolidays.size == 294`,
ascending, first 2077-01-01, last 2084-08-18; `Patro.info` on `NepaliDate.MIN` and
`NepaliDate.MAX` returns `CalendarInfo.EMPTY` contents without throwing; `Named.name(NEPALI)`
returns `nepali`; `Patro.info(d) === Patro.info(d)` for every day in coverage (same instance);
`(Patro.info(d).events as MutableList<Named>).add(x)` throws `UnsupportedOperationException`.

### K4-05 `checkCoreSplit` and the CI gate
A task `checkCoreSplit` in the root `build.gradle.kts`, depending on `:nepdate:jar`,
`:nepdate-patro:jar` and `:nepdate:generatePomFileForMavenPublication`. Using
`java.util.zip.ZipFile` on the built jars, it fails when:
1. the core POM contains `nepdate-patro`;
2. any entry of the core jar contains the UTF-8 bytes of `World Day Against Human Trafficking`;
3. no entry of the patro jar contains them (the check must not pass because the strings moved);
4. any core class contains `java/text/`, or a core class other than
   `io/github/sanamhub/nepdate/NepaliDateJavaTime.class` contains `java/time/`.

Add two gates to `scripts/ci.sh` after `javaApiTest`: `./gradlew checkCoreSplit`, then
`./gradlew :nepdate:build --dry-run`, failing if its output contains `:nepdate-patro:`.

AC:
- `./scripts/ci.sh` passes.
- On a scratch branch, adding the literal `"World Day Against Human Trafficking"` to any core file
  makes `checkCoreSplit` fail; the PR says this was tried.
- `./gradlew :nepdate:test` and `./gradlew :nepdate-patro:test` each pass when run alone.

### K4-06 Patro tests: catalogue, allocation, thread safety
- Every catalogue row tagged `4` from `CalendarDataTests` (`00-ROADMAP.md`, "C# test catalogue"),
  named after the row `id`. Rows about C#'s `CalendarOffsets` layout assert the equivalent fact on
  `PatroTables` (for example `CalendarOffsets_TotalDays_MatchesExpectedCount` becomes
  `SPAN == 2793`) with a comment saying so.
- `PatroAllocationTest` (ADR-0004): `Patro.info`, `NepaliDate.calendarInfo`, `CalendarInfo.events`,
  `events[i]` and `events.size` allocate 0 bytes per call, for the 64 B7 dates of
  `shared/spec/BENCHMARK.md` §3.
- `PatroConcurrentInitTest`: 50 trials, each loading both jars in a fresh `URLClassLoader` and
  calling `Patro.info` from 16 threads released together; every thread sees the PATRO §4 anchors.
- jqwik `PatroProperties`: for any date, `Patro.info` never throws; for a date outside coverage
  the result is empty; `info(d).isPublicHoliday == (d in Patro.publicHolidays)`.

AC: the tests above pass.

### K4-07 `JavaTime.kt`
File `nepdate/src/main/kotlin/io/github/sanamhub/nepdate/JavaTime.kt`, `@file:JvmName("NepaliDateJavaTime")`,
per `docs/API.md`. `toLocalDate()` and `toNepaliDate()` go through `toEpochDay`/`ofEpochDay`.
`KATHMANDU` is `ZoneId.of("Asia/Kathmandu")`, or `ZoneOffset.ofHoursMinutes(5, 45)` if that throws
`ZoneRulesException`. `nepaliToday(clock)` is `LocalDate.now(clock).toNepaliDate()`, so a clock
outside AD 1844-04-11..2143-04-15 throws `NepDateException` with kind `OUT_OF_RANGE` (PORT-PLAN
phase 4); the KDoc says so. The catalogue rows of `DateTimeExtensionsTests` are tested here.

AC:
- With `Clock.fixed(Instant.parse("2024-07-30T18:30:00Z"), ZoneOffset.UTC)`, `nepaliToday(clock)`
  is 2081-04-15; with the same instant in `KATHMANDU`, it is 2081-04-16 (PORT-PLAN phase 4).
- In `America/New_York`, for the instants `2024-03-10T06:30:00Z` and `2024-11-03T05:30:00Z`
  (around the DST changes), `nepaliToday(clock)` equals `LocalDate.now(clock).toNepaliDate()`.
- `KATHMANDU.id == "Asia/Kathmandu"` on the CI JDKs.
- `LocalDate.of(1844, 4, 10).toNepaliDate()` throws `OUT_OF_RANGE`; a clock at
  `2143-04-16T00:00:00Z` in UTC makes `nepaliToday(clock)` throw `OUT_OF_RANGE`; for every serial,
  `d.toLocalDate().toNepaliDate() == d`.

## Definition of Done
Roadmap DoD + milestone M3. Every new public symbol is called in a `JavaApiTest.java` of its module.

## Checkpoint (stop here for review)
The agent stops after K4 and posts the PR. The reviewer checks:
- [ ] `nepdate-patro/api/` and `nepdate/api/`: every public symbol is in `docs/API.md`, and nothing
      else.
- [ ] `unzip -l nepdate/build/libs/nepdate-*.jar` lists no patro class; the scratch-branch
      `checkCoreSplit` failure is described in the PR.
- [ ] `Patro.info` is the four lines of K4-04 (no binary search, no allocation) and
      `PatroAllocationTest` asserts 0.
- [ ] The T, H, E, event and pair counts are asserted on the decoded tables, not on the JSON.
- [ ] Every catalogue row tagged `4` has a test or a deviation comment.
- [ ] The cold first-use time of `PatroTables` printed by the K5 probe is noted in the PR if K5 has
      run; otherwise `./gradlew :nepdate-patro:test --tests '*PatroConcurrentInitTest*'` passes 3
      times in a row.
