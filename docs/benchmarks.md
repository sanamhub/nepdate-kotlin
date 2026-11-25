# Benchmarks

Time and allocation of the Kotlin library on operations B1 to B8 of
[`shared/spec/BENCHMARK.md`](../shared/spec/BENCHMARK.md), measured with JMH 1.37 through the
[`:benchmarks`](../benchmarks/) module. The only competitor is
`io.github.shivathapaa:nepali-date-picker-core` 3.3.0, selected by
[`docs/competitors.md`](competitors.md).

## Machine

| Item | Value |
|---|---|
| CPU | 12th Gen Intel Core i5-12500H (4 performance + 8 efficiency cores, 16 threads, base 3.1 GHz) |
| RAM | 31.7 GB |
| OS | Windows 11 Home 10.0.26300 |
| Power plan | Ultimate Performance |
| Affinity | logical CPU 2 (mask `0x4`), set with `start /affinity 4` |
| Idle gate before the run | 5.5 percent average, 17.4 peak (30 one-second samples) |
| Load during the run | 10.1 percent average, 29.4 peak over 205 one-second samples; 4.2 and 25.0 on the CPUs other than CPU 2 |

The machine was idle: no other build, test suite or benchmark ran during the run, and the Gradle
daemon was stopped after the build. A first run was discarded because a burst of about 10 s above 30
percent load fell inside its first benchmarks; the table is from the second run. The samples are in
[`raw/2026-11-16/cpu-samples.txt`](raw/2026-11-16/cpu-samples.txt).

## Toolchains and versions

| Item | Value |
|---|---|
| nepdate-kotlin | 1.0.0-rc.1 |
| JDK | Temurin 17.0.21+9 |
| JMH | 1.37 (via kotlinx-benchmark 0.5.0) |
| Kotlin | 2.4.20 |
| shivathapaa nepali-date-picker-core | 3.3.0 |
| Date of run | 2026-11-16 |

## Command

The JMH jar is built first. The fat jar contains JMH and the dependency jars but not the project's
own classes, so the run adds the three class directories to the classpath:

```
gradlew.bat :benchmarks:mainBenchmarkJar
java -cp "benchmarks\build\classes\kotlin\main;nepdate\build\classes\kotlin\main;nepdate-patro\build\classes\kotlin\main;benchmarks\build\benchmarks\main\jars\benchmarks-main-jmh-1.0.0-rc.1-JMH.jar" ^
  org.openjdk.jmh.Main -f 1 -wi 5 -w 1s -i 10 -r 1s -prof gc -rf json -rff docs\raw\2026-11-16\jmh.json
```

The process is pinned to logical CPU 2 with `start /affinity 4`. Time and bytes are per single
operation (`OperationsPerInvocation = 64`). For B8 one operation is a whole month grid of 29 to 32
`Patro.info` calls. The error is JMH's 99.9 % confidence half-width. The raw files are
[`raw/2026-11-16/jmh.json`](raw/2026-11-16/jmh.json), [`raw/2026-11-16/jmh.txt`](raw/2026-11-16/jmh.txt)
and [`raw/2026-11-16/jmh.log`](raw/2026-11-16/jmh.log).

## Results

The comparison with C# NepDate, the original library, and the target check from `spec/BENCHMARK.md` section 6 are in the hub: [performance page](https://nepdate.com/docs/project/performance/).

| ID | Operation | nepdate ns/op | error | B/op | shivathapaa ns/op | agrees |
|---|---:|---:|---:|---:|---:|---|
| B1 | BS to AD | 14.36 | 0.06 | 0 | 649.40 | no (61 of 64) |
| B2 | AD to BS | 6.48 | 0.14 | 0 | 628.51 | no (61 of 64) |
| B3 | add 1000 days | 3.15 | 0.08 | 0 | n/a | |
| B4 | add 13 months, Clamp | 4.78 | 0.01 | 0 | n/a | |
| B5 | long format, Nepali, weekday | 29.72 | 0.14 | 199 | n/a | |
| B6 | strict parse `YYYY/MM/DD` | 23.75 | 0.12 | 0 | n/a (no parser in `-core`) | |
| B7 | patro info | 2.13 | 0.06 | 0 | none | |
| B8 | patro month grid | 67.91 | 0.67 | 0 | none | |

The `java.time` floors, for reference (not competitors): `LocalDate.of(y, m, d).toEpochDay()` is
4.66 ns/op and `LocalDate.ofEpochDay(e)` with its fields read is 6.88 ns/op, both 0 B/op.

"agrees" is the result of `:benchmarks:agreement` over the same 64 dates in both directions.
shivathapaa differs on 3 of 64 in each direction: BS 2086-07-23, 2088-01-04 and 2089-06-10. NepDate
agrees with `shared/data` on all 64 dates and is faster on B1 and B2.

## Targets

BENCHMARK section 6 allows the date object as the only allocation. B1 to B4 and B6 to B8 allocate
0 B/op.

## Notes

- The extra `b2eAdToBsEscapes` row is a copy of B2 that lets the returned date escape; it is not
  part of B1 to B8 and is not in the table.
- B5 is 73 percent faster than the 2026-11-02 run (111.42 ns, with an error of 61.04 ns, so that run
  was unstable). Two runs of this rerun gave 30.22 and 29.72 ns.
- Cold start (`:benchmarks:coldInit`) was not re-run on this quiet machine.
