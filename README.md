# nepdate-kotlin

[![CI](https://github.com/sanamhub/nepdate-kotlin/actions/workflows/ci.yml/badge.svg)](https://github.com/sanamhub/nepdate-kotlin/actions/workflows/ci.yml)
[![License](https://img.shields.io/badge/license-MIT-blue)](LICENSE)

Bikram Sambat (the Nepali calendar) for Kotlin, Java and Android. Converts between BS and AD,
formats and parses Nepali dates, does date arithmetic and fiscal years, and ships the patro
(पात्रो): tithi, public holidays and events.

It is a port of [NepDate](https://github.com/RajuPrasai/NepDate) for .NET and gives the same
results, checked against the shared test vectors in [`shared/`](shared/spec/README.md). Neither
`java.time`, ThreeTen-Extra, ICU4J nor `android.icu` has a Bikram Sambat calendar
([comparison](docs/competitors.md)).

> **Status:** not yet on Maven Central. The API is settled and fully tested; 0.1.0 is the first
> release.

Two artifacts, same version:

| Artifact | Contains | Depends on |
|---|---|---|
| `io.github.sanamhub:nepdate` | Conversion, formatting, parsing, arithmetic, fiscal year. BS 1901 to 2199 | `kotlin-stdlib` |
| `io.github.sanamhub:nepdate-patro` | Tithi, public holidays and events, BS 2077-01-01 to 2084-08-20 | `nepdate` |

Apps that only convert dates take `nepdate` alone and stay small. Both run on any JVM 11+ and on
Android API 21+ without desugaring.

## Quick start

Once 0.1.0 is published:

```gradle
dependencies {
    implementation("io.github.sanamhub:nepdate:0.1.0")
    implementation("io.github.sanamhub:nepdate-patro:0.1.0")
}
```

Kotlin:

```kotlin
val date = NepaliDate.of(2081, 4, 15)
val fromAd = NepaliDate.ofGregorian(2024, 7, 30)
val longForm = date.toLongString(lang = Lang.NEPALI)
val parsed = NepaliDate.parseLenient("15 Shrawan 2080")
val tithi = date.calendarInfo.tithi
```

Java:

```java
NepaliDate date = NepaliDate.of(2081, 4, 15);
NepaliDate fromAd = NepaliDate.ofGregorian(2024, 7, 30);
String longForm = date.toLongString(false, true, true, Lang.NEPALI);
NepaliDate parsed = NepaliDate.parseLenient("15 Shrawan 2080");
Named tithi = Patro.info(date).getTithi();
```

`NepaliDate.ofGregorian(2024, 7, 30)` is BS 2081-04-15. `toLongString` in Nepali prints `साउन
१५, २०८१`; `parseLenient` accepts month names such as `15 Shrawan 2080`. The `calendarInfo`
Kotlin extension is `Patro.info` in Java.

## Documentation

- [API reference](docs/API.md)
- [Design decisions](docs/adr/README.md)
- [Benchmarks](docs/benchmarks.md)
- [Algorithm and data](shared/spec/README.md). The data and spec come from the
  [NepDate hub](https://github.com/sanamhub/nepdate); report data problems there.

## License

MIT ([LICENSE](LICENSE)). Calendar data from NepDate by Raju Prasai and contributors, MIT
([shared/data/NOTICE](shared/data/NOTICE)). Not affiliated with the Government of Nepal.
