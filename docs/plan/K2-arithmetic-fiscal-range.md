# K2: Arithmetic, diff, fiscal year, range

Read first: `shared/spec/ALGORITHM.md` §8–8b, `docs/API.md` (arithmetic, `DateDiff`, `FiscalYear`,
`Quarter`, `NepaliDateRange`), ADR-0002 (every operation goes through the serial and
`MONTH_START`; no loops over days or months), `shared/spec/PARITY.md` D-02, D-03, D-07, D-08, D-09,
D-12.

In the golden files, `ERR` means the call throws `NepDateException` with kind `OUT_OF_RANGE`.

## Tasks

### K2-01 `plusDays`, `minusDays`, `daysUntil`
Through the serial. A `days` argument outside `-109211..109211` throws `OUT_OF_RANGE` before any
addition, so `Long.MAX_VALUE` can't overflow. `minusDays(n)` is `plusDays(-n)` except for
`Long.MIN_VALUE`, which throws `OUT_OF_RANGE`. `daysUntil(other)` = `other` serial minus this serial.

AC: all 9162 rows of `csharp-golden/add-days.tsv`; `MAX.plusDays(1)` and `MIN.minusDays(1)` throw
`OUT_OF_RANGE`; `MIN.daysUntil(MAX) == 109211`; `MAX.daysUntil(MIN) == -109211`.

### K2-02 `plusMonths`, `plusYears` with `MonthOverflow`
ALGORITHM §8, with the month index `(year - 1901) * 12 + month - 1` and `MONTH_START`, so the
result is one table read and no loop. `CLAMP` is the spec's `Clamp`, `SPILL` is its `Overflow`. Check `months` against
`-3588..3588` (and `years` against `-299..299`) before multiplying, then `OUT_OF_RANGE`.

AC: all 10180 rows of `csharp-golden/add-months.tsv`, column `clamp` with `CLAMP` and column
`overflow` with `SPILL`; the §8 examples (2081-04-32 +2 months: `CLAMP` 2081-06-30, `SPILL`
2081-07-02; +5 months: 2081-09-29 and 2081-10-03); negative amounts (2081-06-30 −2 months =
2081-04-30).

### K2-03 `until` and `DateDiff` (ALGORITHM §8a)
`years`, `months` and `days` are magnitudes (never negative); `isNegative` is true when `other` is
before this date; `totalDays` is signed and equals `daysUntil(other)`. `equals` and `hashCode` use all
five values. `toString` is for debugging only and not part of the contract.

Use the six examples in ALGORITHM §8a, plus these four (2081-04 has 32 days):

| this | other | isNegative | years | months | days |
|---|---|---|---|---|---|
| 2081-01-01 | 2082-01-01 | false | 1 | 0 | 0 |
| 2081-04-32 | 2081-06-30 | false | 0 | 2 | 0 |
| 2081-04-15 | 2081-05-14 | false | 0 | 0 | 31 |
| 2081-05-14 | 2081-04-15 | true | 0 | 0 | 31 |

AC: the §8a examples and the table above; for 1000 pairs from a seeded `Random(42)`, `a.until(b).totalDays == a.daysUntil(b)`
and `a.until(b)` has the same magnitudes as `b.until(a)` with the opposite sign.

### K2-04 `FiscalYear`, `Quarter`, `NepaliDate.fiscalYear`, `NepaliDate.quarter` (ALGORITHM §8b)
`FiscalYear(startYear)` accepts any `Int`; only methods whose result leaves the supported range
throw `OUT_OF_RANGE`. `range(quarter)` doesn't need `start()` to be valid: `FiscalYear(1900).range(Q4)`
is 1901-01-01..1901-03-32 while `FiscalYear(1900).start()` throws. `Quarter.months`: Q1 =
`SHRAWAN, BHADRA, ASHOJ`, Q2 = `KARTIK, MANGSIR, POUSH`, Q3 = `MAGH, FALGUN, CHAITRA`, Q4 =
`BAISHAKH, JESTHA, ASHAD`. `label(NEPALI)` uses Devanagari digits (written here directly; `nepaliDigits`
comes in K3).

AC: `dates.tsv` columns `fy_start`, `fy_end`, `quarter_start`, `quarter_end` for all 1018 rows
(`containing(date).start()`, `.end()`, `.range(date.quarter).start`, `.endInclusive`);
`FiscalYear(2082).label(Lang.NEPALI) == "२०८२/८३"`; `FiscalYear(2082).label() == "2082/83"`;
`FiscalYear(2199).end()` throws `OUT_OF_RANGE`.

### K2-05 `NepaliDateRange`
Holds the first and last serial. `size`, `get(index)` and `contains(element)` are O(1) (no
iteration); `get` outside `0 until size` throws `IndexOutOfBoundsException` (the `List` contract).
`of(start, endInclusive)` with `start > endInclusive` throws `NepDateException` with kind
`OUT_OF_RANGE`. `ofMonth` and `ofYear` validate like `NepaliDate.of`.

AC: for every year 1901..2199, `ofYear(y).size` equals the sum of its 12 `lengthOfMonth()` values
and lies in 364..367; `ofMonth(2081, 4).size == 32`; `ofMonth(2081, 4).last() == of(2081, 4, 32)`;
`contains(MAX)` is false for `ofYear(2081)`.

### K2-06 C# catalogue, property and allocation tests
- Every catalogue row tagged `2` (files listed in `00-ROADMAP.md`, "C# test catalogue"), named after
  the row `id`, or a comment naming the PARITY deviation (`NepaliDateIsRelativeTests` is D-14).
- jqwik `ArithmeticProperties`: for random serials `s` and offsets `n` with `s + n` in range,
  `ofEpochDay(e(s)).plusDays(n).toEpochDay() == e(s + n)`; `a.plusDays(n).daysUntil(a) == -n`;
  `plusMonths(n, CLAMP).dayOfMonth <= dayOfMonth`; `a.plusMonths(n).plusMonths(-n)` equals `a` when
  no clamping happened; `NepaliDateRange.of(a, b).size == a.daysUntil(b) + 1`.
- `ArithmeticAllocationTest` (ADR-0004): `plusDays`, `plusMonths`, `plusYears` and
  `NepaliDateRange.get` allocate at most 24 bytes per call; `daysUntil`, `NepaliDateRange.contains`
  and `NepaliDateRange.size` allocate 0.

AC: the tests above pass.

## Definition of Done
Roadmap DoD. Every new public symbol is called in `JavaApiTest.java`.

## Checkpoint (stop here for review)
The agent stops after K2 and posts the PR. The reviewer checks:
- [ ] `add-days.tsv` (9162 rows) and `add-months.tsv` (10180 rows, both columns) pass, with no
      skipped rows.
- [ ] `plusMonths` and `until` contain no loop over days; `until` steps down at most once.
- [ ] `FiscalYear(1900).range(Q4)` works while `FiscalYear(1900).start()` throws.
- [ ] Every catalogue row tagged `2` has a test or a deviation comment (count them against
      `catalog.tsv`).
- [ ] `ArithmeticAllocationTest` passes and its bounds are the ones above, not looser.
- [ ] `checkKotlinAbi` dump: only the symbols of this phase were added.
