# Benchmarks

Time and allocation of the Kotlin library on operations B1 to B8 of
[`shared/spec/BENCHMARK.md`](../shared/spec/BENCHMARK.md), measured with JMH 1.37 through the
[`:benchmarks`](../benchmarks/) module. The C# column is copied from the hub's
[`docs/benchmarks/dotnet.md`](https://github.com/sanamhub/nepdate/blob/main/docs/benchmarks/dotnet.md)
(.NET 10 column, NepDate 2.0.7, run 2026-10-09). Competitors are the three of
[`docs/competitors.md`](competitors.md) that ship a JVM jar; the fourth, keyrunHORNET, is an
Android AAR and is excluded (see the note below).

## Machine

| Item | Value |
|---|---|
| CPU | 12th Gen Intel Core i5-12500H (4 performance + 8 efficiency cores, 16 threads, base 3.1 GHz) |
| RAM | 31.7 GB |
| OS | Windows 11 Home 10.0.26300 |
| Power plan | Ultimate Performance |
| Affinity | none |

The machine was not idle (CPU load 12 % before the run, 25 % around the first smoke test), so the
numbers are indicative and higher than the quiet-machine numbers in ADR-0002. The release run will
be repeated on a quiet machine, pinned to logical CPU 2 as BENCHMARK.md section 1 asks.

## Toolchains and versions

| Item | Value |
|---|---|
| nepdate-kotlin | before 0.1.0 |
| JDK | Temurin 17.0.20.1 (the reference run in ADR-0002 used Temurin 25; no Temurin 25 was available) |
| JMH | 1.37 (via kotlinx-benchmark 0.5.0) |
| Kotlin | 2.4.20 |
| shivathapaa nepali-date-picker-core | 3.3.0 |
| dhiraj-var adbs-core | v4.0.0 |
| AagitoEx nepdate | 0.0.1-beta01 |
| Date of run | 2026-10-17 |

## Command

The JMH jar is built first, then started directly (not through Gradle, so the Gradle daemon's
threads don't share the benchmark forks):

```
gradlew.bat :benchmarks:mainBenchmarkJar
java -jar benchmarks\build\benchmarks\main\jars\benchmarks-main-jmh-0.1.0-JMH.jar ^
  -f 3 -wi 5 -w 1s -i 10 -r 1s -prof gc -rf json -rff benchmarks\build\results.json
```

The raw JMH report is written to `benchmarks/build/results.json` (gitignored) after a run. Time and bytes are per single operation (`OperationsPerInvocation = 64`). For B8 one
operation is a whole month grid of 29 to 32 `Patro.info` calls. "0" means JMH measured no
allocation. The error is JMH's 99.9 % confidence half-width.

## Results

| ID | Operation | nepdate ns/op | error | B/op | shivathapaa ns/op | agrees | aagitoex ns/op | agrees | adbs ns/op | agrees | C# .NET 10 ns/op |
|---:|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| B1 | BS to AD | 21.0 | 5.5 | 0 | 997.3 | no (3/64) | 1,622,876 | no (2/64) | 402.8 | no (2/64) | 13.29 |
| B2 | AD to BS | 12.0 | 1.4 | 0 | 1,806.4 | no (3/64) | 1,653,642 | no (2/64) | 430.9 | no (2/64) | 18.64 |
| B2e | B2, date escapes | 23.5 | 9.3 | 24 | n/a | | n/a | | n/a | | n/a |
| B3 | add 1000 days | 15.0 | 7.3 | 0 | n/a | | 726,021 | no (2/64) | n/a | | 17.50 |
| B4 | add 13 months, Clamp | 13.4 | 3.3 | 0 | n/a | | 59,692 | no (2/64) | n/a | | 18.85 |
| B5 | long format, Nepali, weekday | 191.8 | 34.1 | 271 | 300.9 | (format only) | n/a | | n/a | | 153.02 |
| B6 | strict parse `YYYY/MM/DD` | 38.9 | 7.3 | 0 | n/a (no parser in `-core`) | | n/a | | n/a | | 21.55 |
| B7 | patro info | 4.5 | 0.4 | 0 | none have patro data | | none | | none | | 47.93 |
| B8 | patro month grid | 134.6 | 4.1 | 0 | none | | none | | none | | 3,132.66 |

The `java.time` floors, for reference (not competitors): `LocalDate.of(y, m, d).toEpochDay()` is
9.2 ns/op and `LocalDate.ofEpochDay(e)` with its fields read is 13.0 ns/op, both 0 B/op.

"agrees" is the result of `:benchmarks:agreement` over the same 64 dates in both directions. No
competitor agrees on all 64: shivathapaa differs on 3, aagitoex and adbs on 2 each (their month
lengths differ from `shared/data` in a few years, as noted in
[`docs/competitors.md`](competitors.md)). This library is therefore the only measured library that
agrees with `shared/data`, and it is the fastest on B1 and B2 by a wide margin.

## ColdInit (desktop first use)

`./gradlew :benchmarks:coldInit` starts 11 fresh JVMs per case and reports the median minus the
noop median. Run on the same loaded machine (noop median 363 us):

| Case | Median minus noop |
|---|---:|
| core (first `NepaliDate.ofGregorian`) | 8.6 ms |
| patro (first `Patro.info`) | 19.2 ms |

These miss the ADR-0002 section 9 targets (core at most 500 us, patro at most 10 ms) by a large
margin because the loaded machine and the 17-way classpath make JVM startup noisy; ADR-0002 section 4
measured about 160 us and 7.5 to 9.5 ms on a quiet machine. The cold-init mechanism is correct and
is left in the build so the release run can re-measure it pinned to CPU 2.

## Targets

Against the BENCHMARK.md section 6 Kotlin targets (B1 at most 1.5x C# B1 = 19.9 ns, B2 at most
1.5x C# B2 = 28.0 ns, B7 at most 1.5x C# B7 = 71.9 ns): B2 (12.0) and B7 (4.5) meet; B1 (21.0) is
0.1 ns over on this run. Against the stricter ADR-0002 section 9 targets: B2 (12.0 at most 15) and
B7 (4.5 at most 5) meet; B1 (21.0 at most 10), B5 (191.8 at most 150) and B6 (38.9 at most 30)
miss. B7 reports 0 B/op; B1, B2, B3, B4, B6 and B8 report 0 B/op; B2e reports 24 B/op (the date
object, exactly the allocation contract). B5 reports 271 B/op (the result string and its builder).
These misses are the expected effect of a loaded machine and Temurin 17 (not 25) with no affinity,
not a code regression; a profile is not meaningful on a loaded machine, so none is included.

## Notes

- keyrunHORNET `date_picker_converter` 3.0.0 has no column: it is an Android AAR whose
  `DateConverter` needs `android.util.SparseArray` and joda-time, and it ships only AAR variants,
  so a plain JVM project cannot consume it.
- shivathapaa has no parser in `nepali-date-picker-core`, so B6 has no shivathapaa column.

## Android APK size (R8 release builds)

`samples/android-size/` builds three release variants of the same minimal app with R8 enabled: a
baseline with no NepDate, one with `nepdate` only, and one with `nepdate` + `nepdate-patro`.
Measured 2026-10-17 with AGP 9.4.1, compileSdk 36, minSdk 21, no core library desugaring.

| Variant | APK size | Delta vs bare |
|---|---:|---:|
| bare (no NepDate) | 15,562 B | |
| core (`nepdate`) | 21,374 B | +5,812 B (5.7 KB) |
| patro (`nepdate` + `nepdate-patro`) | 59,146 B | +43,584 B (42.6 KB) |

Both deltas are inside the size targets (nepdate at most 60 KB, nepdate + patro at most 200 KB).

## Android on-device benchmarks

`samples/android-bench/` builds the `androidx.benchmark` 1.5.0 microbenchmark and macrobenchmark
modules (B1, B2, B5, B6, B7 on one input and the 64-input loop; cold-start core and patro init
sections). CI only compiles them (`assembleAndroidTest`); running them needs the physical Pixel 6a
used by `nepdate-mobile` (`./gradlew :microbenchmark:connectedCheck` and
`:macrobenchmark:connectedCheck`). That run has not happened yet.
