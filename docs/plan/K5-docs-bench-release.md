# K5: Docs, benchmarks, release 0.1.0

Read first: ADR-0001 §7, ADR-0002 (targets in §9), ADR-0005, `docs/competitors.md`,
`docs/runbooks/release.md`, `shared/spec/BENCHMARK.md`, `shared/spec/PORT-PLAN.md` phase 5, the
hub's `docs/benchmarks/dotnet.md` (the C# column) and `tools/dotnet-bench/Benchmarks.cs` (the
shape every port's B1 to B8 copies).

## Tasks

### K5-01 KDoc, Dokka, README quick start
- KDoc on every public symbol of both artifacts: a summary, `@return`, and `@throws
  NepDateException` naming the kinds (AGENTS.md §4). Turn on Dokka's
  `reportUndocumented` and fail the build on its warnings.
- Dokka 2 HTML for both modules into one site (`./gradlew :dokkaGenerate`), published to GitHub
  Pages by a `docs.yml` workflow on push to `main`; the javadoc jar comes from
  `org.jetbrains.dokka-javadoc` through the publishing plugin.
- README `## Quick start`: a ` ```gradle ` block with the dependency lines for both artifacts (not
  checked), then one ` ```kotlin ` and one ` ```java ` block using `NepaliDate.of`, `ofGregorian`,
  `toLongString(lang = Lang.NEPALI)`, `parseLenient` and `Patro.info` (Kotlin: `calendarInfo`). The
  same code lives in `ReadmeExamplesTest.kt` (in `:nepdate-patro`, since it uses both artifacts) and
  `ReadmeExamples.java` (its `javaApiTest`) between `// README-START` and `// README-END`. A test
  reads `README.md` and fails if the first `kotlin` or `java` block under `## Quick start` differs
  from the marked region. The README explains "patro" in one line (hub ADR-0004).

AC: `./scripts/ci.sh` passes; removing the KDoc from one public function fails the build; changing
one character in the README Kotlin block fails `ReadmeExamplesTest`.

### K5-02 JVM benchmarks (`:benchmarks`, not published)
Module `:benchmarks` with the `org.jetbrains.kotlinx.benchmark` Gradle plugin 0.5.0, the
`kotlin("plugin.allopen")` plugin for `@State` classes (`allOpen { annotation("org.openjdk.jmh.annotations.State") }`),
JMH 1.37, `implementation(project(":nepdate-patro"))`, and the competitors pinned exactly:
`io.github.shivathapaa:nepali-date-picker-core:3.3.0`, `com.github.dhiraj-var.adbs:adbs-core:v4.0.0`
and `com.github.keyrunHORNET:date_picker_converter:3.0.0` (JitPack, declared with
`exclusiveContent` so nothing else resolves from JitPack), `io.github.aagitoex:nepdate:0.0.1-beta01`.

Inputs (`Inputs.kt`, BENCHMARK §2): `s0 = NepaliDate.of(2000, 1, 1).toEpochDay()`,
`s1 = NepaliDate.of(2090, 12, 30).toEpochDay()`, date `i` = `ofEpochDay(s0 + i * ((s1 - s0) / 63))`;
the patro inputs use `Patro.coverage.start` and `endInclusive` the same way. The text inputs are
`toString()` of the 64 dates.

One `@State(Scope.Benchmark)` class per library. Every benchmark loops over the 64 inputs,
is annotated `@OperationsPerInvocation(64)` and returns the sum of what it read, exactly like the
hub's `Benchmarks.cs`:

| ID | Kotlin body per input | Competitors |
|---|---|---|
| B1 | `NepaliDate.of(y, m, d)`, add `gregorianYear + gregorianMonth + gregorianDay` | all four |
| B2 | `NepaliDate.ofGregorian(y, m, d)`, add `year + monthValue + dayOfMonth` | all four |
| B3 | `date.plusDays(1000)`, add its fields | where the library has it |
| B4 | `date.plusMonths(13)`, add its fields | where the library has it |
| B5 | `date.toLongString(weekday = true, lang = Lang.NEPALI).length` | shivathapaa (its Nepali long format) |
| B6 | `NepaliDate.parseOrNull(text)`, add its fields | shivathapaa (its parser) |
| B7 | `Patro.info(date)`, add `tithi` name lengths + `events.size` + holiday flag (the hub's `Consume`) | none have patro data |
| B8 | for `d` in `1..lengthOfMonth()`: `Patro.info(NepaliDate.of(y, m, d))`, consumed the same way | none |
| B2e | `NepaliDate.ofGregorian(y, m, d)` passed to `Blackhole.consume` (the date escapes, so its 24 bytes show in `-prof gc`) | none |

Also two floors that aren't competitors, `LocalDate.of(y, m, d).toEpochDay()` and
`LocalDate.ofEpochDay(e)` fields, so a reader sees how close B1 and B2 are to the JDK.

`ColdInit.kt` (a `main` in `:benchmarks`, run by `./gradlew :benchmarks:coldInit`): starts 11
fresh JVMs per case (`noop`, `core` = first `NepaliDate.ofGregorian`, `patro` = first `Patro.info`
after the core is loaded), each timing first touch to result with `System.nanoTime()`, and prints
the median minus the `noop` median in µs. This is the desktop version of ADR-0002 §4.

`AgreementCheck.kt` (a `main` in `:benchmarks`, run by `./gradlew :benchmarks:agreement`): for each
competitor, converts the 64 dates both ways and prints `yes` or `no (n of 64 differ: first ...)`
against this library.

Run on the reference machine (BENCHMARK §1), Temurin 25, from `cmd`. Build the JMH jar first and
start `java` directly, because a Gradle daemon started earlier would not carry the affinity to the
JMH forks:

```
gradlew.bat :benchmarks:mainBenchmarkJar
start /affinity 4 /wait /b java -jar benchmarks\build\benchmarks\main\jars\benchmarks-main-jmh-JMH.jar ^
  -f 3 -wi 5 -w 1s -i 10 -r 1s -prof gc -rf json -rff benchmarks\build\results.json
```

(Check the jar name `mainBenchmarkJar` prints and use it.) Close other applications first and
check in Task Manager that logical CPU 2 is idle; another pinned process on CPU 2 doubles the
numbers. Results go to `docs/benchmarks.md`: the BENCHMARK §1 machine table, JDK build, library and
competitor versions, the exact command, the date, and one table per BENCHMARK §5 with mean ns/op,
JMH error, bytes/op (`gc.alloc.rate.norm`), each competitor's ns/op and "agrees", and the C# NepDate
.NET 10 column copied from the hub's `docs/benchmarks/dotnet.md` (with its date).

AC:
- The published artifacts' POMs are unchanged by this task (no benchmark dependency leaks).
- `docs/benchmarks.md` has rows B1 to B8 and B2e, a column per competitor, the "agrees" column and
  the C# column.
- BENCHMARK §6 targets for Kotlin, checked against the C# .NET 10 column: B1 ≤ 1.5 × C# B1, B2 ≤
  1.5 × C# B2, B7 ≤ 1.5 × C# B7 (today 19.9, 28.0 and 71.9 ns); B7 reports 0 bytes/op; B1, B2,
  B3, B4, B6 and B2e at most 24 (the date; escape analysis usually removes it when only fields are
  read); B8 at most 24 per day of the grid; B5 only the result `String` and its builder.
- ADR-0002 §9 targets (this port's own, stricter): B1 ≤ 10 ns, B2 ≤ 15 ns, B7 ≤ 5 ns, B6 ≤ 30 ns,
  B5 ≤ 150 ns.
- `docs/benchmarks.md` has the `ColdInit` medians: core ≤ 500 µs and patro ≤ 10 ms on the
  reference machine (ADR-0002 §4 measured about 160 µs and 7.5 to 9.5 ms).
- Among the competitors that agree with `shared/data`, this library is the fastest on B1 and B2.
- Any missed target is reported to the human with the JMH output and a `-prof perfasm` or
  async-profiler flame graph, not guess-optimised.

### K5-03 Android: size, start-up and on-device speed
Separate Gradle builds under `samples/` (each with its own `settings.gradle.kts` and
`includeBuild("../..")`, not part of the root build), `minSdk 21`, no core library desugaring,
AGP and `androidx.benchmark` 1.5.0 versions in their own version catalog.

1. `samples/android-size/`: three release variants built with R8: no NepDate, `nepdate` only,
   `nepdate` + `nepdate-patro`. Each calls `NepaliDate.ofGregorian`, `toLongString` and (third
   variant) `Patro.info`. A CI job `android-size` in `ci.yml` builds them and prints the three APK
   sizes. Targets: `nepdate` adds at most 60 KB and `nepdate` + `nepdate-patro` at most 200 KB to
   the release APK.
2. `samples/android-bench/microbenchmark/`: `androidx.benchmark:benchmark-junit4` 1.5.0,
   `BenchmarkRule` tests for B1, B2, B5, B6 and B7 on one input each and on the 64-input loop.
   The report gives ns and allocation counts per run.
3. `samples/android-bench/macrobenchmark/`: `androidx.benchmark:benchmark-macro-junit4` 1.5.0 and
   a minimal target app whose `Application.onCreate` wraps the first `NepaliDate.ofGregorian` in
   `Trace.beginSection("nepdate-core-init")` and the first `Patro.info` in
   `Trace.beginSection("nepdate-patro-init")`. `StartupBenchmark` measures cold start with
   `StartupTimingMetric()` and `TraceSectionMetric("nepdate-core-init")`,
   `TraceSectionMetric("nepdate-patro-init")`, 10 iterations, for `CompilationMode.None()` and
   `CompilationMode.Partial(BaselineProfileMode.Disable, warmupIterations = 3)`.

Items 2 and 3 need a physical device; CI only builds them (`assembleAndroidTest`). The human runs
them on the Pixel 6a used by `nepdate-mobile` (`./gradlew :macrobenchmark:connectedCheck`) and the
agent copies the medians into `docs/benchmarks.md` with the device, Android version and the
`androidx.benchmark` JSON file.

AC: the three size variants build in CI and `docs/benchmarks.md` records the APK size deltas; the
two benchmark modules build in CI; after the device run, `docs/benchmarks.md` has the
`nepdate-core-init` and `nepdate-patro-init` medians for both compilation modes. Targets with
`CompilationMode.None()`: core init ≤ 1 ms, patro init ≤ 10 ms (ADR-0002 §9). Over a target:
report the numbers to the human before release, don't trim features.

### K5-04 Release workflow and runbook
`.github/workflows/release.yml` per ADR-0005: preflight (Gradle `version`, tag and `CHANGELOG.md`
section agree), `./scripts/ci.sh`, `environment: production`, `publishAndReleaseToMavenCentral` for
both artifacts, GitHub Release with the changelog section, then a wait until `repo1.maven.org`
serves both POMs. Keep `docs/runbooks/release.md` in step.

AC: a `workflow_dispatch` dry run on a test tag passes every job except publishing and creating
the release.

### K5-05 0.1.0
Precondition: the hub's open question on the provenance of the patro data
(`shared/data/README.md`, "Known data questions") is answered, or the human decides to ship anyway.
Set `version=0.1.0`, move `[Unreleased]` to `## [0.1.0] - <date>`. **Agent stops here.** The human
tags and approves (`docs/runbooks/release.md`).

## Checkpoint (stop here for review)
The agent stops after K5-04 (before K5-05) and posts the PR. The reviewer checks:
- [ ] `docs/benchmarks.md` records the exact command, the JDK build and that CPU 2 was idle; the
      error column is under 10 % of the mean for B1, B2 and B7 (rerun if not).
- [ ] Every BENCHMARK §6 and ADR-0002 §9 target is met, or the miss is reported with a profile.
- [ ] `ColdInit` medians are recorded and within the K5-02 limits, or reported.
- [ ] The "agrees" column is filled for every competitor, and a competitor that disagrees is not
      ranked above this library.
- [ ] The two Android size deltas are within 60 KB and 200 KB, or reported.
- [ ] The device run of K5-03 items 2 and 3 is done and recorded (human task), or explicitly
      deferred by the human.
- [ ] README quick start compiles and runs in CI; Dokka builds with no undocumented warnings.
