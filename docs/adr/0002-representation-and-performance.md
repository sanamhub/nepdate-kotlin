# ADR-0002: Representation, tables and performance

- Status: Accepted
- Date: 2026-10-06, revised 2026-10-08 (measured design, patro rename per hub ADR-0004)
- Deciders: Sanam Pakuwal

## Context

Performance is the first priority for every NepDate port (hub `shared/spec/BENCHMARK.md` §6). C#
NepDate converts in 13 to 20 ns with no allocation because it reads precomputed tables
(hub `docs/research/dotnet-internals.md`, "Lessons for the ports"). The Kotlin library also runs in
Glance widget updates and in app cold start, where class loading and table decoding show up.

Every number below was measured on the BENCHMARK §1 reference machine (i5-12500H, Windows 11),
OpenJDK 25.0.2 (Android Studio JBR), JMH 1.37, `-prof gc`, pinned with `start /affinity 4`
(logical CPU 2), on 2026-10-07 and 2026-10-08. The experiment code lived in a scratch folder, not
in this repo; K5-02 rebuilds the same cases as the real `:benchmarks` module. Inputs are the 64
BENCHMARK §2 dates. Numbers are ns per operation; compare rows of one table only (separate runs
differ by up to 2× on this laptop).

## Decision

### 1. Date representation

`NepaliDate` is a final class with two `Int` fields: `serial` (days since BS 1901-01-01,
ALGORITHM §4) and `ymd`, the BS date packed as `year shl 9 or month shl 5 or day`. `year`,
`monthValue` and `dayOfMonth` are shifts and masks. `equals`, `hashCode` and `compareTo` use the
serial. The object is 24 bytes on 64-bit HotSpot with compressed oops (12-byte header, two ints,
padding). Not a `value class` (Java can't call it cleanly, ADR-0003) and not a `data class` (ABI).

| Variant (one JMH run) | B1 ns | B2 ns, date escapes | B2 then read y/m/d |
|---|---|---|---|
| serial + packed `ymd` (chosen) | 10.5 | 11.9 (24 B) | 13.5 |
| serial only, y/m/d computed when read | 21.9 | 8.5 (16 B) | 19.4 |
| `value class` over the serial | 17.0 | not run | not run |

Caching `ymd` costs 4 bytes and one store, and every caller that reads fields gains.

### 2. Core tables

| Table | Size | Built from | Used by |
|---|---|---|---|
| `MONTH_START: IntArray` | 3589 | `MONTH_LENGTHS_DATA` string (3588 chars, K1-01), cumulative sums | BS to serial, serial to BS, month length = `MONTH_START[i + 1] - MONTH_START[i]` |
| `JAN1: IntArray` | 302 | computed in the initialiser: serial of 1 January of AD 1844 + i | serial to AD, AD to serial |
| `CUM: IntArray` | 26 | literal: days before each month, common year then leap year | serial to AD, AD to serial |

All three live in one `internal object CalendarTables`, so they are built together on first use
(JVM class initialisation is lazy and thread-safe, JLS §12.4.2). There is no `MONTH_LENGTHS` byte
array and no `YEAR_START`: the month estimate in §3 replaces the year search.

### 3. BS to AD and AD to BS

- BS (y, m, d) to serial: `MONTH_START[(y - 1901) * 12 + m - 1] + d - 1`, after validation.
- Serial to BS: month index `mi = ((s - 25) * 2153) shr 16`, then `if (s >= MONTH_START[mi + 1]) mi++`.
  One multiply, one shift, one compare. The estimate is never more than one month low; this was
  checked for all 109,212 serials and K1-06 `ExhaustiveTest` keeps checking it.
- Serial to AD: year index `i = ((s - 40) * 5730) shr 21`, `if (s >= JAN1[i + 1]) i++`, then month
  `m = ((doy - 6) * 1000) shr 15` with one compare against `CUM`. Checked for all serials against
  Hinnant's `civil_from_days`.
- AD to serial: `JAN1[y - 1844] + CUM[base + m - 1] + d - 1`, after validating the Gregorian date.
- `toEpochDay()` is `serial + EPOCH_UNIX_DAYS` (-45920); `ofEpochDay` is the reverse.

Hinnant's algorithms (ALGORITHM §5) stay in the test sources as the reference, not in the core.

| B1 variant (one run) | ns | B/op |
|---|---|---|
| `JAN1` + `CUM` estimate (chosen) | 5.35 ± 0.02 | 0 |
| C#-style table of the AD date of each BS month's day 1, plus a roll | 9.5 ± 7.7 | 0 |
| Hinnant `civil_from_days` each time a field is read | 33.9 ± 29.0 | 0 |
| Hinnant once, fields from the packed result | 24.2 ± 21.3 | 0 |

| B2 variant (one run) | ns, fields read | ns, date escapes (24 B) |
|---|---|---|
| C#-style table of the BS date of each AD month's day 1, plus a roll | 3.78 | 4.45 |
| `JAN1` + `CUM`, then the §3 month estimate (chosen) | 4.27 | 5.63 |
| Hinnant `days_from_civil`, then the month estimate | 6.54 | 6.73 |

From an earlier run (escaping B2): binary search over a 300-entry `YEAR_START` then a month walk
22.5 ns; a per-16-day block index 11.8 ns; the month estimate 11.9 ns; `java.time.LocalDate.of` 4.4 ns.

The C#-style AD to BS table is 0.5 to 1.2 ns faster on B2 but needs a fourth 3589-entry table
(14 KB of heap and its initialiser). Both are three to four times faster than C# NepDate (B2 18.64 ns), so
the smaller tables win. Revisit only if K5-02 misses the §9 B2 target.

### 4. Table storage and class-initialisation cost

Large tables are decoded from `const val` strings in a static initialiser, never written as
`intArrayOf(...)` or `shortArrayOf(...)` literals. A JVM array literal compiles to one
load-and-store bytecode sequence per element in `<clinit>`, which costs class verification and
interpretation time, and large ones hit the 64 KB method limit.

Cost from first touch to table ready, median of 11 fresh JVMs (`-Xshare:auto`), minus an empty
probe (694 µs):

| Tables | Literal arrays | Decoded from strings |
|---|---|---|
| `MONTH_START` (3589 ints) | +1246 µs | **+161 µs** |
| `MONTH_START` + a 6826-entry block index | +2364 µs | +519 µs |
| packed months + `YEAR_START` (599 ints) | +766 µs | not run |
| patro tables (K4-02) | +7466 µs | **+5501 µs** |
| patro tables + every day's `CalendarInfo` prebuilt (§5) | +9428 µs | not run |

With `-Xint` the numbers are within 10 %, so the cost is class loading and the initialiser, not JIT.
On Android, D8 turns array literals into `fill-array-data`, which may change this ranking; K5-03
measures both inits on a device.

### 5. Patro layout (`nepdate-patro`)

Tables per hub `shared/spec/PATRO.md` §3, decoded from strings (K4-02). The initialiser of
`PatroTables` (K4-03) then builds one `CalendarInfo` per day of the 2793-day span, sharing objects:
one per tithi for days with only a tithi, one `EMPTY`, and a read-only index view (`EventList`)
over a shared `Array<Named>` for days with events. PATRO §3 allows this dense per-day table because
the benchmark shows it is faster:

| Patro lookup (one run) | B7 ns | B7 B/op | B8, 30 days, ns | B8 B/op |
|---|---|---|---|---|
| `infoByDay[offset]`, prebuilt (chosen) | 0.48 | 0 | 22.9 | 0 |
| binary search, new `CalendarInfo` and list view per call | 34.6 | 39 | 2236 | 1136 |
| C# NepDate .NET 10 (hub `docs/benchmarks/dotnet.md`) | 47.93 | 51 | 3133 | 1429 |

The price is about 2 ms more at first use (§4 table). It is paid once per process, and a month grid
then costs 23 ns instead of 2.2 µs on every widget update. If K5-03 shows patro init over the §9
limit on the device, build `infoByDay` per BS year on first touch of that year instead.

### 6. Formatting and parsing

Every formatter appends to a caller's `StringBuilder`; the `String` forms create one builder sized
for the longest output (K3). `StringBuilder` and not `Appendable`: `Appendable.append` throws
`IOException`, which Java callers must catch, and an interface call can be megamorphic.
Devanagari digits are `(0x0966 + d).toChar()`. No `String.format`, no `java.text`.

Parsers take `CharSequence` and read it by index into the serial; only the final `NepaliDate` is
allocated. No `split`, `substring` or `toInt()`.

| Operation (one run) | ns | B/op |
|---|---|---|
| B5 append to a reused builder | 52.3 | 0 |
| B5 `toLongString`, sized builder (chosen `String` form) | 91.5 | 236 |
| B5 naive (`toString`, `map`, `joinToString`, templates) | 285.5 | 630 |
| B6 parse `CharSequence` by index (chosen) | 24.0 | 24 |
| B6 parse to the serial only | 16.7 | 0 |
| B6 naive (`split`, `toInt`) | 98.1 | 272 |
| B3 `plusDays(1000)` | 4.8 | 24 |

C# NepDate: B5 153.02 ns and 435 B, B6 21.55 ns.

### 7. Allocation contract

Conversion, arithmetic and parsing allocate only the returned `NepaliDate` (24 B, often removed by
escape analysis when the caller only reads fields). `Patro.info`, `CalendarInfo.events`, the
`append*` functions and `formatTo` into a builder with enough capacity allocate nothing. The
`String` formatters allocate the builder and the result. ADR-0004 tests this per phase.

### 8. Core boundary

`nepdate-patro` reads the core only through its public API, because Kotlin `internal` doesn't cross
modules: serial = `date.toEpochDay().toInt() + 45920` (ALGORITHM §2, `EPOCH_UNIX_DAYS = -45920`),
and dates are built with `NepaliDate.ofEpochDay(serial - 45920L)`. The core gets no public `serial`
and no `-Xfriend-paths` trick. Apps that don't depend on `nepdate-patro` carry none of its tables.

### 9. Targets

JMH through kotlinx-benchmark (K5-02), reference machine, Temurin 25:

| ID | Target | Measured in the experiments above | C# NepDate |
|---|---|---|---|
| B1 | ≤ 10 ns | 5.35 | 13.29 |
| B2 | ≤ 15 ns | 4.27 to 5.63 | 18.64 |
| B5 (`String` form) | ≤ 150 ns | 91.5 | 153.02 |
| B6 | ≤ 30 ns | 24.0 | 21.55 |
| B7 | ≤ 5 ns, 0 B | 0.48 | 47.93 |

Android, `CompilationMode.None()`, Pixel 6a (K5-03): core init ≤ 1 ms, patro init ≤ 10 ms.

## Alternatives rejected

- Hinnant's algorithms on the hot path: 4 to 6× slower than the `JAN1` table on B1 (§3).
- `YEAR_START` binary search (the 2026-10-06 version of this ADR): 22.5 ns against 11.9 ns for the
  month estimate in the same run.
- A C#-style AD to BS table: faster by about 1 ns but one more 14 KB table (§3).
- Array literals: 1.4 to 8× slower class initialisation (§4).
- Allocating patro answers per call: 72× slower B7 and 39 B per call (§5).

## Consequences

- One 24-byte allocation per returned `NepaliDate`; a widget month grid creates 42.
- About 15 KB of heap for the core tables, built in about 0.2 ms on desktop.
- `nepdate-patro` first use costs about 7.5 to 9.5 ms on desktop (to be measured on a device in
  K5-03) and then answers without allocating.
- R8 removes unused formatting code from apps.
