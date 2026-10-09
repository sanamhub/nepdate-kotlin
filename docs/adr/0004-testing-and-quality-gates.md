# ADR-0004: Testing and quality gates

- Status: Accepted
- Date: 2026-10-08 (first accepted 2026-10-06; tests section extended for the patro rename and the performance work, before K0)
- Deciders: Sanam Pakuwal

## Decision

`./scripts/ci.sh` runs these Gradle tasks in order, stopping at the first failure:

1. `ktlintCheck` (official Kotlin style) and `detekt`.
2. `build` with `allWarningsAsErrors = true`.
3. `test` and `koverVerify`: line coverage ≥ 90 % for `:nepdate` and `:nepdate-patro` (org target
   80 %).
4. `:codegen:run --args=--check`.
5. `checkKotlinAbi` (the Kotlin Gradle plugin's built-in `abiValidation`, still Experimental, opted in
   with `@OptIn(ExperimentalAbiValidation::class)`), one dump per published module in
   `nepdate/api/` and `nepdate-patro/api/`; any change to a dump is reviewed in the PR.
6. `:nepdate:javaApiTest` and `:nepdate-patro:javaApiTest`: a Java source set per module that calls
   every public function the way a Java user would, so a missing `@JvmStatic` or `@JvmOverloads`
   fails the build.
7. `checkCoreSplit` (hub ADR-0004, PORT-PLAN phase 4): fails if the core's generated POM names
   `nepdate-patro`, if any entry of the core jar contains the bytes `World Day Against Human Trafficking`, if the patro
   jar doesn't (so the check can't pass by accident), if a core class references `java/text/`, or
   if a core class other than `NepaliDateJavaTime` references `java/time/`. After it,
   `./gradlew :nepdate:build --dry-run` must list no `:nepdate-patro:` task, so the core builds
   and tests on its own. Task details: K4-04.
8. API level check: the `ru.vyarus.animalsniffer` Gradle plugin with the
   `net.sf.androidscents.signature:android-api-level-21` signature on both published modules, so a
   call to an API missing on Android 5.0 fails the build (backs the README's "API 21+ without
   desugaring"). `JavaTime.kt` is excluded, since it is documented as needing `java.time`.

## Tests

All tests are JUnit 5 (`kotlin.test` assertions). Core tests never use `:nepdate-patro`; patro
tests live in `:nepdate-patro`.

| Kind | Tool | What it covers |
|---|---|---|
| Vectors | `@ParameterizedTest` | every file in `shared/spec/vectors/` |
| C# catalogue | one test per row | every row of `shared/spec/vectors/csharp-tests/catalog.tsv` tagged with the phase, named after the row `id` (`00-ROADMAP.md`, "C# test catalogue") |
| Exhaustive | plain loops | all 109,212 serials round-trip; agreement with `java.time.LocalDate` for every serial |
| Properties | jqwik 1.10.1 (`net.jqwik:jqwik`, a JUnit Platform engine, fixed seed in `junit-platform.properties`) | round trips, arithmetic laws, format then parse; listed per phase |
| Fuzzing | Jazzer 0.30.0 (`com.code-intelligence:jazzer-junit`, `@FuzzTest`) | `parse`, `parseLenient`, `parseOrNull` (K3-07); in `./scripts/ci.sh` it replays the committed inputs only, a manual `fuzz.yml` job fuzzes for 10 minutes with `JAZZER_FUZZ=1` |
| Allocation | `com.sun.management.ThreadMXBean.getThreadAllocatedBytes(Thread.currentThread().id)` on HotSpot | bytes per call on the hot paths, ADR-0002 §7 |
| Thread safety | fresh `URLClassLoader` per trial, 16 threads behind a `CountDownLatch` | first use of the tables from many threads at once (K1-08, K4-06) |
| Java interop | the `javaApiTest` source sets (gate 6) | every public symbol called from Java |
| Time zones | `Clock.fixed` | `nepaliToday(clock)` across `Asia/Kathmandu`, `America/New_York` (DST) and UTC |

Allocation tests measure the average bytes over 100,000 calls after 10,000 warm-up calls. The
numbers are a property of the code, not of the JIT: interpreted code allocates the same objects, and
escape analysis can only lower them. So the tests are stable and assert upper bounds: at most 24
bytes per call where one `NepaliDate` is returned (16 with compact object headers), 0 where nothing
is returned. They are skipped with a message when the `com.sun.management` bean is missing.

CI: Ubuntu with Temurin 17 and 21 for the build, plus a test-only job on Temurin 11 (the
`jvmTarget`); actions pinned by SHA, `permissions: contents: read`, Gradle
wrapper validation, dependency verification metadata (`gradle/verification-metadata.xml`).
