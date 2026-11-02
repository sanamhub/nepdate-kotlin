# Benchmarks

Time and allocation of the Kotlin library on operations B1 to B8 of
[`shared/spec/BENCHMARK.md`](../shared/spec/BENCHMARK.md), measured with JMH 1.37 through the
[`:benchmarks`](../benchmarks/) module. The C# column is copied from the hub's
[`docs/benchmarks/dotnet.md`](https://github.com/sanamhub/nepdate/blob/main/docs/benchmarks/dotnet.md)
(.NET 10 column, NepDate 2.0.7, run 2026-11-02). The only competitor is
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
| CPU load before | 10, 8 and 13 percent, sampled 2 s apart |
| CPU load after | 7, 1 and 13 percent, sampled 2 s apart |

The machine had the owner's normal desktop applications open. No other build, test suite or
benchmark ran during the run.

## Toolchains and versions

| Item | Value |
|---|---|
| nepdate-kotlin | 0.1.0-rc.1 |
| JDK | Temurin 17.0.20.1 |
| JMH | 1.37 (via kotlinx-benchmark 0.5.0) |
| Kotlin | 2.4.20 |
| shivathapaa nepali-date-picker-core | 3.3.0 |
| Date of run | 2026-11-02 |

## Command

The JMH jar is built first. The fat jar contains JMH and the dependency jars but not the project's
own classes, so the run adds the three class directories to the classpath:

```
gradlew.bat :benchmarks:mainBenchmarkJar
java -cp "benchmarks\build\classes\kotlin\main;nepdate\build\classes\kotlin\main;nepdate-patro\build\classes\kotlin\main;benchmarks\build\benchmarks\main\jars\benchmarks-main-jmh-0.1.0-rc.1-JMH.jar" ^
  org.openjdk.jmh.Main -f 1 -wi 5 -w 1s -i 10 -r 1s -prof gc -rf json -rff docs\raw\2026-11-02\jmh.json
```

The process is pinned to logical CPU 2 with `start /affinity 4`. Time and bytes are per single
operation (`OperationsPerInvocation = 64`). For B8 one operation is a whole month grid of 29 to 32
`Patro.info` calls. The error is JMH's 99.9 % confidence half-width. The raw files are
[`raw/2026-11-02/jmh.json`](raw/2026-11-02/jmh.json), [`raw/2026-11-02/jmh.txt`](raw/2026-11-02/jmh.txt)
and [`raw/2026-11-02/jmh.log`](raw/2026-11-02/jmh.log).

## Results

| ID | Operation | nepdate ns/op | error | B/op | shivathapaa ns/op | agrees | C# .NET 10 ns/op |
|---|---:|---:|---:|---:|---:|---|---:|
| B1 | BS to AD | 15.63 | 0.34 | 0 | 696.95 | no (61 of 64) | 13.50 |
| B2 | AD to BS | 6.93 | 0.11 | 0 | 694.70 | no (61 of 64) | 20.10 |
| B3 | add 1000 days | 3.33 | 0.11 | 0 | n/a | | 19.05 |
| B4 | add 13 months, Clamp | 5.08 | 0.06 | 0 | n/a | | 20.19 |
| B5 | long format, Nepali, weekday | 111.42 | 61.04 | 279 | n/a | | 150.41 |
| B6 | strict parse `YYYY/MM/DD` | 26.40 | 1.42 | 0 | n/a (no parser in `-core`) | | 22.42 |
| B7 | patro info | 2.30 | 0.04 | 0 | none | | 48.21 |
| B8 | patro month grid | 72.99 | 1.70 | 0 | none | | 3,112.76 |

The `java.time` floors, for reference (not competitors): `LocalDate.of(y, m, d).toEpochDay()` is
5.05 ns/op and `LocalDate.ofEpochDay(e)` with its fields read is 7.43 ns/op, both 0 B/op.

"agrees" is the result of `:benchmarks:agreement` over the same 64 dates in both directions.
shivathapaa differs on 3 of 64 in each direction: BS 2086-07-23, 2088-01-04 and 2089-06-10. This
library is the only measured library that agrees with `shared/data`, and it is faster on B1, B2 and
B7.

## Targets

BENCHMARK section 6 asks for B1, B2 and B7 within 1.5 times C# NepDate, with the date object as the
only allowed allocation. B1 is 1.16 times C#, B2 is 0.34 times and B7 is 0.05 times. B1 to B4 and B6
to B8 allocate 0 B/op. Passes.

## Notes

- The extra `b2eAdToBsEscapes` row is a copy of B2 that lets the returned date escape; it is not
  part of B1 to B8 and is not in the table.
- Cold start (`:benchmarks:coldInit`) was not re-run on this quiet machine.
