# K7 mutation report

## Baseline (K7-01)

`./gradlew --console=plain :nepdate:pitest` on 2026-10-18, PIT 1.30.1, Gradle 9.6.0, Temurin 17
toolchain, four PIT threads. The generated `Calendar_generatedKt` is excluded, as are the fuzz,
random and class-initialisation stress tests.

| Metric | Baseline | Final |
|---|---|---|
| Mutations generated | 622 | 622 |
| Killed | 522 | 540 |
| Timed out (counted as detected) | 33 | 33 |
| Survived | 40 | 25 |
| No coverage | 27 | 24 |
| Mutation score | 555 / 622 = 89% | 573 / 622 = 92% |
| Test strength | 555 / 595 = 93% | 573 / 598 = 96% |

The final run used 89 test classes and ran 2,459 tests, 3.95 per mutation. Eighteen of the 67 open
baseline mutants became killed. No production line changed.

## Tests added

`MutationCoverageTest` (Kotlin) and `StaticBridgeTest` (Java) hold the tests written for this
phase. PIT records them as the killer for 47 mutants; some of those mutants had another killer
too. The new tests cover:

- the static `ofOrNull` and `parseOrNull` bridges and the `List` bridges of `NepaliDateRange` and
  `Quarter.months` from Java;
- every ASCII and Devanagari digit in `parse`, plus zero dates and trailing separators;
- dot and zero forms in `parseLenient`;
- `plusYears` range messages, Gregorian error messages and the padded month in an invalid-day
  message;
- `DateDiff.toString`, `FiscalYear.label` for every supported year, and `nepaliDigits(Long.MIN_VALUE,
  25)`;
- short forms with and without padding.

## Remaining mutants and dispositions

| File | Line | Method | Mutants | Status | Disposition |
|---|---|---|---|---|---|
| `DateDiff.kt` | 32-37 | `DateDiff::hashCode` | 10 | SURVIVED | Equivalent. The contract only requires equal values to have equal hashes. Every mutant is still a deterministic function of the five fields, so it satisfies the contract. Exact hash values are not API. |
| `NepaliDate.kt` | 218 | `NepaliDate::hashCode` | 1 | SURVIVED | Equivalent. A constant hash is legal; equal dates still collide as required. |
| `FiscalYear.kt` | 81 | `FiscalYear::hashCode` | 1 | SURVIVED | Equivalent, same reason as `NepaliDate.hashCode`. |
| `NepaliDate.kt` | 150 | `NepaliDate::monthsLaterSerial` | 2 | SURVIVED | Equivalent. At exactly `+/-MONTHS` the target index is already outside `0 until MONTHS`, so both the early `-1` and the later `-1` return the same result. |
| `Serial.kt` | 49 | `SerialKt::packedAdOf` | 1 | SURVIVED | Equivalent. Changing `serial - 40` to `serial + 40` shifts the year estimate by less than one, and the following `if (serial >= JAN1[i + 1]) i++` corrects it. `ExhaustiveTest` agrees with `LocalDate` for all 109,212 serials. |
| `Formatting.kt` | 93 | `FormattingKt::appendCustomPattern` | 1 | SURVIVED | Equivalent. The closing quote is searched from `i + 1`, so `close` is either `-1` or at least 1. `close == 0` is impossible; `< 0` and `<= 0` behave the same. |
| `Formatting.kt` | 20 | `FormattingKt::appendShort` | 1 | SURVIVED | Equivalent. The mutated branch is the year width. A BS year is always four digits (1901..2199), so padding the year width from 4 to 0 changes no output. The month and day widths are covered by the pad tests. |
| `MonthTokens.kt` | 184 | `MonthTokensKt::foldCase` | 1 | SURVIVED | Equivalent. The boundary jumps use the exclusive upper bound `'['`; only a `'['` character could tell the two versions apart. No month token contains `'['`, and `'['` is not a month letter. |
| `NepaliDate.kt` | 457 | `NepaliDate$Companion::parseOrNull` | 1 | SURVIVED | Equivalent. `scanStrictValid` returns 0 only for a date that its own validation rejects, and rejected dates are returned as a negative kind. The `>= 0` and `> 0` tests therefore see the same values. |
| `LenientParsing.kt` | 58 | `LenientParsingKt::nextToken` | 1 | SURVIVED | Equivalent. The mutated comparison is the upper `from < text.length` bound. At `from == length`, `nextNumber` returns `-1` and the loop below does not run, so the result is the same. |
| `LenientParsing.kt` | 60 | `LenientParsingKt::nextToken` | 1 | SURVIVED | Equivalent. A zero number is either read again by the loop below or leads to the same rejection; `lenient keeps a zero number in every position` exercises both orders. |
| `LenientParsing.kt` | 70 | `LenientParsingKt::nextToken` | 1 | SURVIVED | Equivalent, same reason as line 60, in the other branch. |
| `LenientParsing.kt` | 87 | `LenientParsingKt::scanLenient` | 1 | SURVIVED | Equivalent. `scanStrictValid` never returns 0: a valid date has a year of at least 1901, and an invalid one is negative. `>= 0` and `> 0` are the same test. |
| `LenientParsing.kt` | 101 | `LenientParsingKt::scanLenient` | 1 | SURVIVED | Equivalent. A token always has `end > start`, so the packed token is never 0. The `t < 0` and `t <= 0` tests are the same. |
| `FiscalYear.kt` | 68 | `FiscalYear::label` | 1 | SURVIVED | Equivalent. Kotlin compiles `c in '0'..'9'` with the exclusive upper bound `':'`. The surviving boundary changes `c >= ':'` to `c > ':'`, which only differs for `':'`; `label` builds digits and `/` only. The all-years label test covers every digit. |
| `NepaliDateRange.kt` | 9 | `NepaliDateRange::contains/indexOf/lastIndexOf` | 10 | NO_COVERAGE | PIT limitation. These are the synthetic `Object` bridges; PIT assigns them the class line and records no coverage even though `StaticBridgeTest.rangeObjectBridgesUseTheTypedOverrides` calls them. The Java test passes on the original. |
| `Quarter.kt` | 34 | `ThreeMonths::contains/indexOf/lastIndexOf` | 14 | NO_COVERAGE | PIT limitation, same reason. `StaticBridgeTest.quarterMonthsObjectBridgesUseTheTypedOverrides` calls every bridge and passes. |

## Doubtful points

1. The free Kotlin plugin for PIT has no release. `com.groupcdg.pitest:pitest-kotlin-plugin` needs a
   commercial licence key, and the Apache `pitest/pitest-kotlin` repository is archived with no
   published artifact. The build therefore uses PIT's own Kotlin support plus
   `avoidCallsTo = kotlin.jvm.internal.Intrinsics`. A reviewer with an Arcmutate licence can swap in
   the plugin without changing the tests.
2. The three excluded test classes are stress tests, not behaviour tests. `ParseFuzzTest` starts a
   fuzzing runtime (6 s), `ParseRandomTest` generates 200,000 strings, and
   `CalendarConcurrentInitTest` creates 50 class loaders. PIT repeats each per mutant. They still run
   in `:nepdate:test`.
3. PIT records 24 no-coverage mutants in synthetic `List` bridges. The Java bridge tests call them,
   but PIT cannot instrument a bridge without a line number. They stay in the score as survivors.
4. The score counts 33 timeouts as detected. Each timeout is a mutation that made a test hang or run
   too long; PIT counts a timeout as killed. The report does not claim each timeout was inspected.
5. `FiscalYear.label` has one equivalent boundary mutant as explained above. The first run also had
   a second, real boundary; the all-years test killed it.

## CI gate (K7-03)

`scripts/mutation.sh` runs `:nepdate:pitest` and prints one line:

```
mutation: score 92% (threshold 92%, max surviving 49)
```

The two numbers live in `gradle.properties`: `nepdateMutationThreshold=92` and
`nepdateMaxSurviving=49`. The score gate is just below the measured 92.1% (573/622). The surviving
gate is the final count, 25 survived plus 24 no coverage. It exists because PIT rounds the score:
one deleted test can drop 571/622 (91.8%) and still print 92%. `.github/workflows/ci.yml` runs the
script in its own `mutation` job. `scripts/ci.sh` has a comment saying why PIT is not in the
per-change gate.

## Negative control

Deleting `MutationCoverageTest.plusYears reports the month amount it really passed on` makes the
script exit 1:

```
Exception in thread "main" java.lang.RuntimeException: Had 51 surviving mutants, but only 49 survivors allowed
...
mutation: score 92% (threshold 92%, max surviving 49)
```

The score stays 92% because 571/622 rounds to 92, so the score gate alone would pass. The surviving
gate fails as intended. Restoring the test makes the script pass again with the line above.
