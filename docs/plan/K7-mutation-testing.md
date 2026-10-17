# K7: Mutation testing

Read first: ADR-0004, `docs/API.md`, the K1 to K4 plans and their test files, and
`docs/plan/PROGRESS.md`.

PIT mutates a copy of the core classes and runs the core's tests against each mutant. A survivor is
a mutation the tests cannot see. Mutation testing is slower than every other gate, so it runs in its
own CI job and not in `scripts/ci.sh`.

The maintained Kotlin plugin for PIT (`com.groupcdg.pitest:pitest-kotlin-plugin`) needs a commercial
licence key. This repository has no secrets, so the task uses PIT's built-in Kotlin handling and
records the plugin gap as a doubtful point.

## Tasks

### K7-01 PIT for the core
Add `info.solidsoft.pitest` 1.19.0 to `:nepdate` only. Pin PIT 1.30.1 and
`org.pitest:pitest-junit5-plugin` 1.2.3 in `gradle/libs.versions.toml`. Mutate
`io.github.sanamhub.nepdate.*`, exclude the generated `Calendar_generatedKt` and Kotlin
`$WhenMappings` classes, use four threads, and write HTML and XML reports without timestamped
directories. Run `./gradlew :nepdate:pitest` once with the threshold off and record the score and
every survivor by file.

AC: `:nepdate:pitest` writes `nepdate/build/reports/pitest/index.html` and `mutations.xml`; the XML
names no `Calendar_generatedKt` mutant; the baseline score and the survivor table are in
`docs/plan/K7-mutation-testing.md`; the run uses four threads.

### K7-02 Kill the survivors
For each survivor, add the smallest test that fails on the mutant and passes on the original. A
survivor that no input can distinguish is recorded as equivalent with the reason. No production code
change, no weakened test, no deleted test.

AC: a second run has no survivor without a disposition; every non-equivalent survivor has a test
that PIT reports as its killer; `./gradlew :nepdate:test` passes; the score improves.

### K7-03 CI gate and negative control
Add `scripts/mutation.sh` (runs `:nepdate:pitest`, prints one `mutation:` line with the score and
threshold) and a `mutation` job in `.github/workflows/ci.yml`. Set `mutationThreshold` just below
the final score. Leave `scripts/ci.sh` without PIT because a run takes several minutes.

AC: `./scripts/mutation.sh` exits 0 at the final score; deleting one killing test makes it exit 1
with the score under the threshold; restoring the test makes it pass; the workflow has the
`mutation` job; `scripts/ci.sh` keeps PIT out with a comment saying why.

### K7-04 Report and checkpoint
Put the before and after scores, the survivor table with dispositions, the CI line, the negative
control and the doubtful points in this file. Add the K7 rows to `PROGRESS.md`.

AC: `./scripts/ci.sh` passes; this file answers every item in the report; no publishing, tag or
repository setting changed.

## Baseline (K7-01)

Command: `./gradlew --console=plain :nepdate:pitest` on 2026-10-18, PIT 1.30.1, Gradle 9.6.0,
Temurin 17 toolchain, four PIT threads. Intrinsics calls are filtered with `avoidCallsTo`, and the
fuzz, random and class-initialisation stress tests are excluded from PIT (they run once in the
normal test gate). The generated `Calendar_generatedKt` is not mutated.

| Metric | Value |
|---|---|
| Mutations generated | 622 |
| Killed | 522 |
| Timed out (counted as detected) | 33 |
| Survived | 40 |
| No coverage | 27 |
| Mutation score | 555 / 622 = 89% |
| Test strength | 555 / (622 - 27) = 93% |

Survivors by file: DateDiff 11, LenientParsing 8, NepaliDate 13, Parsing 4, FiscalYear 3,
Formatting 2, NepaliDateRange 10 (all no coverage), Quarter 14 (all no coverage), NepaliDigits 1,
Serial 1, MonthTokens 1. The full table with dispositions is in
[`K7-mutation-report.md`](K7-mutation-report.md).

## Result (K7-02 to K7-04)

Final score 573/622 (92%), test strength 96%. The new tests kill 18 of the 67 open baseline
mutants. The remaining 49 are 25 survived and 24 no coverage; each has a disposition in the report.
`scripts/mutation.sh` prints `mutation: score 92% (threshold 92%, max surviving 49)`. Deleting the
`plusYears` test raises the survivor count to 51, above the gate, and the script exits 1. The full
CI run ends with `ci: 11/11 gates passed`.
