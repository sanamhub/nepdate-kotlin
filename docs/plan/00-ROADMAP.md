# Roadmap (Kotlin port)

Each phase is one PR (or a short series). Task IDs `K<phase>-<n>`; branch `feat/K1-03-serial`; commit
`feat(nepdate): K1-03 serial conversion`. The structure matches the other ports (`nepdate-go`
G0–G5, `nepdate-swift` S0–S5), so a reviewer who knows one knows all.

```
K0 build + CI ─► K1 core + conversion ─► K2 arithmetic/fiscal/range ─┐
                           └───────────► K3 format + parse ───────────┴─► K4 patro + java.time interop ─► K5 docs, bench, 0.1.0
```

Preconditions: hub task H0 merged (first `shared/VERSION`; it still says `pending`), hub H3 before
K0-04. K2 and K3 may run in parallel. `nepdate-mobile` phase A1 needs K4 (it can use this build as
a Gradle included build before 0.1.0).

| Phase | File | Output |
|---|---|---|
| K0 | [`K0-build-and-ci.md`](K0-build-and-ci.md) | Gradle build, version catalog, `:codegen` skeleton, `scripts/ci.sh`, CI, community files |
| K1 | [`K1-core-and-conversion.md`](K1-core-and-conversion.md) | `NepaliDate`, `Month`, `Weekday`, `Lang`, `NepDateException`, generated tables, BS↔AD, epoch days, vectors, exhaustive tests, `javaApiTest` |
| K2 | [`K2-arithmetic-fiscal-range.md`](K2-arithmetic-fiscal-range.md) | `plusDays/Months/Years`, `daysUntil`, `until`, `FiscalYear`, `Quarter`, `NepaliDateRange` |
| K3 | [`K3-format-and-parse.md`](K3-format-and-parse.md) | names, `nepaliDigits`, `toShortString`, `toLongString`, `format`, `toIsoString`, strict and lenient parse |
| K4 | [`K4-patro-and-interop.md`](K4-patro-and-interop.md) | `:nepdate-patro` (tables per PATRO §3, `calendarInfo`, `Patro`), `checkCoreSplit`, `JavaTime.kt` |
| K5 | [`K5-docs-bench-release.md`](K5-docs-bench-release.md) | KDoc and Dokka, README quick start tested, benchmarks, R8 size check, release workflow, 0.1.0 |

Each plan file maps the bullets and acceptance criteria of `shared/spec/PORT-PLAN.md` for that
phase to Kotlin tasks. If a plan file and PORT-PLAN disagree, PORT-PLAN wins; report it.

## Definition of Done (every task)

1. Every acceptance criterion has a test (or a line in the PR saying why it can't).
2. `./scripts/ci.sh` passes.
3. No public symbol beyond `docs/API.md` (`checkKotlinAbi`). `nepdate` depends only on
   `kotlin-stdlib`, `nepdate-patro` only on `kotlin-stdlib` and `nepdate`.
4. `CHANGELOG.md` `[Unreleased]` updated for anything a user would notice.
5. Prose follows the writing-style skill. PR has "What to look at first".

## C# test catalogue

`shared/spec/vectors/csharp-tests/catalog.tsv` (hub task H5, in `shared/` since 2026-10-09) lists every test
case of C# NepDate's suite at `cb05cb65` (395 `[Fact]` and 147 `[InlineData]` rows) with a `phase`
column. Each phase implements its rows as tests named after the row `id` `<File>.<Method>[#n]`: a
test class `<File>` (for example `NepaliDateConstructionTests`) with a test function `<Method>`; the
`#n` rows of a theory become one `@ParameterizedTest` with one argument row per `#n`, in order (JVM
method names can't contain `.` or `#`). Rows that don't apply get a comment naming the PARITY
deviation. The catalogue's `phase` column wins; this is the same mapping by
file (counts are `[Fact]` plus `[InlineData]` lines in each file):

| C# test file | Rows | Kotlin phase |
|---|---|---|
| `Core/NepaliDateConstructionTests.cs`, `Core/NepaliDatePropertiesTests.cs`, `Abilities/NepaliDateComparableTests.cs`, `Core/DictionaryIntegrityTests.cs` | 29, 9, 11, 11 | K1 |
| `Core/OptimizationVerificationTests.cs` | 133 | split by method prefix: `Constructor`, `CompareTo`, `Equals`, `GetHashCode`, `Operator`, `DayOfWeek`, `EnglishDate`, `MonthEndDay`, `IsDefault`, `IsLeapYear` in K1; `Subtraction` in K2; `ToString`, `ToLongDateString`, `ToUnicodeString`, `UnicodeConversion`, `SmartDateParser` in K3; `BulkConvert` n/a |
| `Core/NepaliDateArithmeticEdgeCaseTests.cs`, `Core/NepaliDateManipulationTests.cs`, `Core/FiscalYearTests.cs`, `Core/FiscalYearInstanceMethodTests.cs`, `Core/NepaliDateRangeTests.cs`, `Core/NepaliDateRangeExtendedTests.cs` | 25, 12, 11, 45, 19, 64 | K2 |
| `Core/NepaliDateIsRelativeTests.cs` | 17 | D-14 (not ported) |
| `Core/NepaliDateMonthNameTests.cs`, `Core/SmartDateParserTests.cs`, `Abilities/NepaliDateFormattableTests.cs`, `Abilities/NepaliDateParsableTests.cs`, `Extensions/StringExtensionsTests.cs` | 31, 14, 20, 5, 5 | K3 |
| `Core/CalendarDataTests.cs` | 35 | K4 (patro); the `CalendarOffsets_*` rows test C#'s internal layout and map to the K4-02 table assertions |
| `Extensions/DateTimeExtensionsTests.cs` | 5 | K4 (`JavaTime.kt`) |
| `Core/BulkConvertTests.cs`, `Abilities/NepaliDateTypeConverterTests.cs`, `Serialization/NepaliDateSerializationTests.cs`, `Integration/MemoryUsageTests.cs`, `Integration/MemoryOptimizationBenchmark.cs` | 7, 18, 9, 4, 3 | n/a (C#-only API, D-11; memory rows are replaced by the allocation tests of ADR-0004) |

If this table and `catalog.tsv` disagree, the catalogue wins and the PR says so.

## Milestones

| Milestone | Phases | Exit |
|---|---|---|
| M1 converts | K0–K1 | month-boundary vectors + exhaustive round-trip pass |
| M2 parity | K2–K3 | all C# golden rows pass except `PARITY.md` deviations |
| M3 usable by the Android app | K4 | patro and `java.time` interop merged; `checkCoreSplit` green |
| M4 0.1.0 | K5 | tagged, both artifacts on Maven Central |
