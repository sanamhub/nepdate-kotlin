<p align="center">
  <img src="https://raw.githubusercontent.com/sanamhub/nepdate-kotlin/main/docs/assets/nepdate-kotlin-128.png" width="128" alt="NepDate for Kotlin logo">
</p>

<h1 align="center">nepdate-kotlin</h1>

<p align="center">
  <a href="https://github.com/sanamhub/nepdate-kotlin/actions/workflows/ci.yml"><img src="https://github.com/sanamhub/nepdate-kotlin/actions/workflows/ci.yml/badge.svg" alt="CI"></a>
  <a href="https://central.sonatype.com/artifact/io.github.sanamhub/nepdate"><img src="https://img.shields.io/maven-central/v/io.github.sanamhub/nepdate?label=nepdate&logo=apachemaven" alt="Maven Central: nepdate"></a>
  <a href="https://central.sonatype.com/artifact/io.github.sanamhub/nepdate-patro"><img src="https://img.shields.io/maven-central/v/io.github.sanamhub/nepdate-patro?label=nepdate-patro&logo=apachemaven" alt="Maven Central: nepdate-patro"></a>
  <img src="https://img.shields.io/badge/kotlin-2.4-7F52FF?logo=kotlin" alt="Kotlin 2.4">
  <a href="https://github.com/sanamhub/nepdate-kotlin/blob/main/LICENSE"><img src="https://img.shields.io/badge/license-MIT-blue" alt="License: MIT"></a>
</p>

Bikram Sambat (the Nepali calendar) for Kotlin, Java and Android. Converts between BS and AD,
formats and parses Nepali dates, does date arithmetic and fiscal years, and ships the patro
(पात्रो): tithi, public holidays and events.

It is a port of [NepDate](https://github.com/RajuPrasai/NepDate) for .NET and gives the same
results, checked against the shared test vectors in [`shared/`](https://github.com/sanamhub/nepdate-kotlin/blob/main/shared/spec/README.md). Neither
`java.time`, ThreeTen-Extra, ICU4J nor `android.icu` has a Bikram Sambat calendar
([comparison](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/competitors.md)).

> **Status:** 0.1.0-beta.1, the first public pre-release. The API is listed in
> [docs/API.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/API.md) and may still change before 0.1.0. Patro data (tithi, holidays, events)
> is informational, not an official calendar.

Two artifacts, same version:

| Artifact | Contains | Depends on |
|---|---|---|
| `io.github.sanamhub:nepdate` | Conversion, formatting, parsing, arithmetic, fiscal year. BS 1901 to 2199 | `kotlin-stdlib` |
| `io.github.sanamhub:nepdate-patro` | Tithi, public holidays and events, BS 2077-01-01 to 2084-08-20 | `nepdate` |

Apps that only convert dates take `nepdate` alone and stay small. Both run on any JVM 11+ and on
Android API 21+ without desugaring.

## Install

Gradle (Kotlin DSL), from Maven Central:

```kotlin
dependencies {
    implementation("io.github.sanamhub:nepdate:0.1.0-beta.1")
    implementation("io.github.sanamhub:nepdate-patro:0.1.0-beta.1") // only for tithi, holidays, events
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.sanamhub</groupId>
  <artifactId>nepdate</artifactId>
  <version>0.1.0-beta.1</version>
</dependency>
```

Every file on Maven Central is signed by the release key `44ED6E4129D97536CAE086175B979A307721A17B`.

## Quick start

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

## More

| Topic | Where |
|---|---|
| Every public type and function | [docs/API.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/API.md) |
| Speed, allocations and size, with the C# baseline | [docs/benchmarks.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/benchmarks.md) |
| What changed in each version | [CHANGELOG.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/CHANGELOG.md) |
| Design decisions | [docs/adr/README.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/adr/README.md) |
| Building, gates and pull requests | [CONTRIBUTING.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/CONTRIBUTING.md) |
| Reporting a vulnerability | [SECURITY.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/SECURITY.md) |
| Algorithm and data | [shared/spec/README.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/shared/spec/README.md), from the [NepDate hub](https://github.com/sanamhub/nepdate) |
| Guides and examples | [nepdate.com/docs/kotlin](https://nepdate.com/docs/kotlin/) |
| A wrong date, holiday or tithi | the hub's [data error form](https://github.com/sanamhub/nepdate/issues/new/choose) |
| The other languages | [sanamhub/nepdate](https://github.com/sanamhub/nepdate#packages) |

## License

MIT ([LICENSE](https://github.com/sanamhub/nepdate-kotlin/blob/main/LICENSE)). Calendar data from NepDate by Raju Prasai and contributors, MIT
([shared/data/NOTICE](https://github.com/sanamhub/nepdate-kotlin/blob/main/shared/data/NOTICE)). Not affiliated with the Government of Nepal.
