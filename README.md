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
formats and parses Nepali dates, does date arithmetic, fiscal years and date ranges, and ships the
patro (पात्रो): tithi, public holidays and events.

It is a port of [NepDate](https://github.com/RajuPrasai/NepDate) for .NET and gives the same
results, checked against the shared test vectors in [`shared/`](https://github.com/sanamhub/nepdate-kotlin/blob/main/shared/spec/README.md). Neither
`java.time`, ThreeTen-Extra, ICU4J nor `android.icu` has a Bikram Sambat calendar
([comparison](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/competitors.md)).

> **Status:** 0.1.0-rc.1, a release candidate. The API is listed in
> [docs/API.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/API.md) and may still change before 0.1.0. Patro data (tithi, holidays, events)
> is informational, not an official calendar.

Three artifacts, same version:

| Artifact | Contains | Depends on |
|---|---|---|
| `io.github.sanamhub:nepdate` | Conversion, formatting, parsing, arithmetic, fiscal year, ranges. BS 1901 to 2199 | `kotlin-stdlib` |
| `io.github.sanamhub:nepdate-patro` | Tithi, public holidays and events, BS 2077-01-01 to 2084-08-20 | `nepdate` |
| `io.github.sanamhub:nepdate-serialization` | kotlinx.serialization support, optional | `nepdate`, `kotlinx-serialization-core` |

Apps that only convert dates take `nepdate` alone and stay small. All three run on any JVM 11+ and
on Android API 21+ without desugaring.

## Install

Gradle (Kotlin DSL), from Maven Central:

```kotlin
dependencies {
    implementation("io.github.sanamhub:nepdate:0.1.0-rc.1")
    implementation("io.github.sanamhub:nepdate-patro:0.1.0-rc.1") // only for tithi, holidays, events
    implementation("io.github.sanamhub:nepdate-serialization:0.1.0-rc.1") // only for kotlinx.serialization
}
```

Maven:

```xml
<dependency>
  <groupId>io.github.sanamhub</groupId>
  <artifactId>nepdate</artifactId>
  <version>0.1.0-rc.1</version>
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

## Dates

A valid date is 1901-01-01 to 2199-12-30 BS, which is 1844-04-11 to 2143-04-15 AD. An invalid date
can't be built, so there is no zero value to check.

```kotlin
NepaliDate.of(2081, 4, 15)          // 2081/04/15
NepaliDate.of(2081, Month.SHRAWAN, 15)
NepaliDate.ofGregorian(2024, 7, 30) // 2081/04/15
NepaliDate.MIN                       // 1901-01-01
NepaliDate.MAX                       // 2199-12-30
nepaliToday(KATHMANDU)               // today in Nepal, then or now
NepaliDate.of(2081, 4, 15).yearLength()   // 366, the BS year's length
NepaliDate.fromYmd(20810415)              // the YYYYMMDD integer form
```

`year`, `monthValue`, `month`, `dayOfMonth`, `dayOfWeek`, `dayOfYear`, `lengthOfMonth()` and the
three `gregorian*` fields are the parts. `val (year, month, day) = date` destructures them.
`isToday(zone)`, `isYesterday(zone)` and `isTomorrow(zone)` take the zone explicitly, and each also
takes a `java.time.Clock`.

## Conversion and serialization

```kotlin
LocalDate.of(2024, 7, 30).toNepaliDate()          // 2081-04-15
NepaliDate.of(2081, 4, 15).toLocalDate()          // 2024-07-30
NepaliDate.MIN.toEpochDay()                       // -45920, days since 1970-01-01
NepaliDate.ofEpochDay(0)                          // 2026-09-17
toNepaliDates(listOf(LocalDate.of(2024, 7, 30)))  // [2081-04-15], in input order
toEnglishDates(listOf("2081-04-15"))              // [2024-07-30]
date.toIsoString()                                // "2081-04-15", the canonical text form
```

With the optional artifact, a date is one JSON string:

```kotlin
@Serializable
data class Festival(
    val name: String,
    @Serializable(with = NepaliDateSerializer::class) val date: NepaliDate,
)

Json.encodeToString(Festival("New Year", NepaliDate.of(2081, 1, 1)))
// {"name":"New Year","date":"2081-01-01"}
```

## Arithmetic

```kotlin
val date = NepaliDate.of(2081, 4, 32)
date.plusDays(1)                      // 2081-05-01
date.plusMonths(2)                    // 2081-06-30, clamped to the month end
date.plusMonths(2, MonthOverflow.SPILL) // 2081-07-02, the extra days carried on
date.plusYears(1)                     // 2082-04-30
date.minusDays(1)                     // 2081-04-31
date.daysUntil(NepaliDate.of(2081, 5, 1))   // 1, the named form
(NepaliDate.of(2081, 5, 1) - date).totalDays // 1, the operator keeps the C# sign
date.until(NepaliDate.of(2082, 5, 1))       // a DateDiff in years, months and days
```

`daysUntil(other)` is `other - this`; `this - other` is the opposite direction, as in C#. The two
names are not interchangeable. Month arithmetic takes whole months and an explicit overflow mode.

## Formatting

```kotlin
val date = NepaliDate.of(2081, 4, 15)
date.toString()                                  // 2081/04/15
date.toShortString(DateOrder.DMY, Separator.DASH, pad = false)  // 15-4-2081
date.toLongString(weekday = true)                // Tuesday, Shrawan 15, 2081
date.toLongString(lang = Lang.NEPALI)            // साउन १५, २०८१
date.format("yyyy-MM-dd")                        // 2081-04-15
date.format("dd.MM.yy")                          // 15.04.81
date.format("""yyyy\M\M""")                      // 2081MM
```

The six `DateOrder` values and the six `Separator` values combine freely, and `Lang.NEPALI` gives
Devanagari names and digits. The `append*String` and `formatTo` forms write into a `StringBuilder`
without allocating once it has room.

## Parsing

```kotlin
NepaliDate.parse("2081/04/15")                 // strict: year, month, day in that order
NepaliDate.parse("२०८१-०४-१५")                 // ASCII and Devanagari digits both work
NepaliDate.parseOrNull("2081/04/33")           // null
NepaliDate.parseLenient("15 Shrawan 2080")     // month names, era markers, day first
NepaliDate.parseLenientOrNull("not a date")    // null
```

A strict group has at most four digits, so no input can overflow, and there is no `autoAdjust`
overload. Failures are a `NepDateException` with a `kind`, never a made-up date.

## Ranges

```kotlin
val month = NepaliDateRange.ofMonth(2081, 4)      // 2081-04-01..2081-04-32
val week = NepaliDate.of(2081, 4, 10)..NepaliDate.of(2081, 4, 16)
month.containsRange(week)                          // true
month.overlaps(NepaliDateRange.ofMonth(2081, 5))   // false
month.intersect(NepaliDateRange.ofMonth(2081, 5))  // null
month.union(week)                                  // the smallest range covering both
month.minus(week)                                  // one or two ranges
month.splitByMonth()                               // one piece per month
week.workingDays(excludeSunday = true)
week.datesWithInterval(2)
month.toString()                                   // 2081/04/01 - 2081/04/32
```

A range is an inclusive `List<NepaliDate>`, never empty, and never throws at 2199-12-30. It
compares, hashes and prints its two bounds rather than every day.

## Fiscal year

```kotlin
val fy = NepaliDate.of(2081, 4, 15).fiscalYear   // FiscalYear(2081)
fy.start()                                        // 2081-04-01, 1 Shrawan
fy.end()                                          // 2082-03-32, the end of Ashad
fy.range(Quarter.Q2)                              // Kartik to Poush
fy.label()                                        // 2081/82
```

Quarters are `Q1` Shrawan to Ashoj, `Q2` Kartik to Poush, `Q3` Magh to Chaitra and `Q4` Baishakh to
Ashad of the next year.

## Errors

```kotlin
try {
    NepaliDate.of(2081, 4, 33)
} catch (e: NepDateException) {
    e.kind        // INVALID_DAY
    e.monthLength // 32
}
```

`NepDateException` extends `IllegalArgumentException` and carries a `kind`: `OUT_OF_RANGE`,
`INVALID_MONTH`, `INVALID_DAY`, `INVALID_GREGORIAN`, `INVALID_CHARACTER`, `WRONG_GROUP_COUNT`,
`NUMBER_TOO_LONG`, `AMBIGUOUS` or `UNRECOGNIZED`. Check `kind`, not the message.

## Patro

```kotlin
val info = NepaliDate.of(2081, 1, 1).calendarInfo
info.tithi?.english        // Panchami
info.isPublicHoliday       // true
info.events.map { it.english }  // [New Year, Mesh Sankranti, Bisket Jatra]
```

The data covers BS 2077-01-01 to 2084-08-20. A day outside it answers with empty values.

## More

| Topic | Where |
|---|---|
| Every public type and function, with an example | [docs/API.md](https://github.com/sanamhub/nepdate-kotlin/blob/main/docs/API.md) |
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
