# Progress (Kotlin port)

Tasks from [`00-ROADMAP.md`](00-ROADMAP.md), in the format of the hub's `docs/handover.md` §3 step 7.
There is no GitHub remote yet: "CI last line" is `./scripts/ci.sh` run locally (JDK 25 from Android
Studio runs Gradle; the foojay resolver provides the JDK 17 toolchain).

| Task | Status | Commit | CI last line | Notes and open questions |
|---|---|---|---|---|
| K0-01 | done | 0466b2c | `ci: 6/6 gates passed` | Gradle 9.6.0 wrapper with `distributionSha256Sum`; Kotlin 2.4.20; detekt 2.0.0-alpha.6 (`dev.detekt`, the only detekt line built for Kotlin 2.4); JUnit 5.14.4 (the platform version jqwik 1.10.1 uses). The core POM lists exactly `org.jetbrains.kotlin:kotlin-stdlib`. `*.bat` kept CRLF in `.gitattributes`. |
| K0-02 | done | 31f2255 | `ci: 6/6 gates passed` | 11 tests in `:codegen`. `:codegen:run` twice gives the same SHA-1; `--args=--check` exits 1 after a hand edit and writes nothing. |
| K0-03 | done | 783c865 | `ci: 6/6 gates passed` | Gates: ktlint+detekt, build, test+koverVerify, codegen check, checkKotlinAbi, animalsniffer (API 21). An extra space in a `.kt` file fails gate 1 (ktlint "Unnecessary long whitespace"). animalsniffer was wired here because no later task owns gate 8; it fails on a `java.time` call. `kotlin.compiler.execution.strategy=in-process`: two Gradle daemons crashed out of memory on this shared machine. |
| K0-04 | done | bedf715 | `ci: 6/6 gates passed` | Workflows lint clean with actionlint 1.7.12, every action pinned by SHA; `shared-check` and `shared-sync` call hub `94946ac`. `verification-metadata.xml` was written from the CI gates, not only `help` (plan said `help`; that would fail `build` on unlisted checksums). Extra: a `test-java-11` job (ADR-0004 asks for it) with `-PtestJavaVersion=11`, not yet run locally since `:nepdate` has no tests until K1. Blocked on the human: create `sanamhub/nepdate-kotlin`, protect `main`, `production` environment, Central namespace; "CI green on the first PR" needs that remote. |
| K0 checkpoint | ready for review | 4273e14 | `ci: 6/6 gates passed` (clean clone) | Human tasks of K0-04 open (remote, branch protection). |
| K0 light check | ok | | | |
| K1-01 | done | c61d81f | `ci: 6/6 gates passed` | `MONTH_LENGTHS_DATA` (299 literals) and `EPOCH_*` generated; `CalendarTables` decodes in loops. `javap -c` shows 28 `iastore`: 26 for the `CUM` literal, 2 in the loops. detekt `MagicNumber` turned off and `MaxLineLength` set to ktlint's 140 (`config/detekt/detekt.yml`, reasons in the file). |
| K1-02 | done | f2beaa1 | `ci: 6/6 gates passed` | `Weekday.displayName` and `Month.displayName`/`shortName` wait for K3-01 as planned. |
| K1-03 | done | d6fc589 | `ci: 6/6 gates passed` | Constants as ADR-0002 §3. |
| K1-04 | done | 20be003 | `ci: 6/6 gates passed` | **Deviation from ADR-0002 §1:** the packed date is `year shl 10 or month shl 6 or day`, not `shl 9`/`shl 5`: day 32 needs six bits and set the month's lowest bit (2081-04-32 read back as day 0 of month 5). Question for the human: amend ADR-0002 §1 and the K1-03 text to the new shifts? |
| K1-05 | done | 9839be9 | `ci: 6/6 gates passed` | No `Math.*`, no `java.*` in `src/main`; `ofEpochDay` checks the `Long` before narrowing. |
| K1-06 | done | dfc06f6 | `ci: 6/6 gates passed` | ExhaustiveTest: 4 loops over all 109,212 serials, 0.15 s together on this machine. Catalogue phase 1: 113 rows = 90 tests, 14 skipped with a marker (`K3-04: needs NepaliDate.parse` 13, `K4-07: needs nepaliToday` 1, per PORT-PLAN), 9 named deviations (D-06: 7 auto-adjust rows, D-01: 2 `IsDefault` rows). String rows that expect a date build it with `of` and a `// K3-04` comment. `IsLeapYear` rows check the AD year's leap rule (no such API in `docs/API.md`). jqwik has no global seed setting, so each `@Property` has `seed = "20261010"` (ADR-0004 says the seed lives in `junit-platform.properties`). Allocation bounds: below 24 + 1 and 0 + 1 bytes per call, the 1 byte being the counter's own overhead (measured 24.00x for `of`). Kover line coverage 96.25 % (rule 90 %). |
| K1-07 | done | afbec75 | `ci: 7/7 gates passed` | Removing `@JvmStatic` from `NepaliDate.of(Int, Int, Int)` fails `compileJavaApiTestJava`. |
| K1-08 | done | 38f70f1 | `ci: 7/7 gates passed` | 50 of 50 trials, about 2 s. |
