# Other Kotlin and Java libraries for the Bikram Sambat calendar

Checked 2026-10-07 (GitHub, `repo1.maven.org` metadata, JitPack build API). Used by [the benchmarks](benchmarks.md).

## Platform support: none

No JVM or Android platform library has Nepal's Bikram Sambat calendar, so this port builds it.

| Platform | What it has | Nepali BS? | Source |
|---|---|---|---|
| `java.time.chrono` (JDK 25) | `IsoChronology`, `HijrahChronology`, `JapaneseChronology`, `MinguoChronology`, `ThaiBuddhistChronology` | no | https://docs.oracle.com/en/java/javase/25/docs/api/java.base/java/time/chrono/package-summary.html |
| ThreeTen-Extra 1.10.0 (2026-06) | Accounting, BritishCutover, Coptic, Discordian, Ethiopic, InternationalFixed, Julian, Pax, Symmetry010, Symmetry454 | no | https://github.com/ThreeTen/threeten-extra/tree/main/src/main/java/org/threeten/extra/chrono |
| ICU4J 78.3 (`com.ibm.icu:icu4j`) | Buddhist, Chinese, Coptic, Dangi, Ethiopic, Gregorian, Hebrew, Indian, Islamic, Japanese, Persian, Taiwan | no; `IndianCalendar` is the Indian national (Saka) calendar | https://github.com/unicode-org/icu/tree/main/icu4j/main/core/src/main/java/com/ibm/icu/util |
| Android `android.icu.util` | the ICU4J classes above, at the ICU version of each Android release | no | https://developer.android.com/reference/android/icu/util/package-summary |
| CLDR `ca` keys | buddhist, chinese, coptic, dangi, ethioaa, ethiopic, gregory, hebrew, indian, islamic (5 variants), iso8601, japanese, persian, roc | no | https://github.com/unicode-org/cldr/blob/main/common/bcp47/calendar.xml |

A `java.time.chrono.Chronology` for BS would be a differentiator but is a large surface; it is a
candidate for a later optional artifact, not v0.1.

## Libraries

| Library | Coordinates | Latest | License | BS range | Approach | State |
|---|---|---|---|---|---|---|
| `shivathapaa/Nepali-Date-Picker` | `io.github.shivathapaa:nepali-date-picker-core` (+ `-ui`, `-serialization`), Maven Central | 3.3.0, 2026-09-21 | MPL-2.0 | 1970–2100 | Kotlin Multiplatform, depends on `kotlinx-datetime` (Android below API 26 needs desugaring); Devanagari digits, pattern formatting, working-day policy; no holiday data | active, the main alternative |
| `dhiraj-var/adbs` | `com.github.dhiraj-var.adbs:adbs-core` (+ `adbs-spring`), JitPack | v4.0.0, 2026-09 | MIT | 1970–2100 (AD 1913-04-13 to 2044-04-12 per its README) | Java 17, data bundled in the jar, Spring module with exception handler | active, JitPack only |
| `sujit-sharma/bikram-sambat` | `io.github.sujitsharma:bikram-sambat` per its README; not on Maven Central on 2026-10-07 | GitHub release 2.0.0, 2026-10 | MIT | 1970–2100 | `BsDate.of(...)`, `BsDate.from(LocalDate)`, JPA and Spring converters, Java 21; notes 364-day years 1974, 1990, 2096 | new, not published |
| `keyrunHORNET/date_picker_converter` | JitPack | 3.0.0, 2021-09 | Apache-2.0 | 1970–2090 | `DateConverter().getEnglishDate(...)`; its `@IntRange(from = 1913 - 2033)` evaluates to `from = -120` | unmaintained |
| `keyrunHORNET/composeNepaliDatePicker` | JitPack | v1.3, 2024-10 | none | 1970–2090 | `NepaliDateUtils.adToBs(...)` | no licence file, so not usable |
| `theGBguy/KomposeNepaliDatePicker` | `io.github.thegbguy:nepali-date-picker`, Maven Central | 1.0.2, 2024-10 | MIT | 1970–2090 | multiplatform port of the above | stale |
| `AagitoEx/nepdate` | `io.github.aagitoex:nepdate`, Maven Central | 0.0.1-beta01, 2022-09 | MIT | 1975–2100 | `java.time`-based, Java 8 | abandoned beta |
| `AagitoEx/compose-nepcal` | `io.github.aagitoex:compose-nepcal`, Maven Central | 1.0.0-beta03, 2023-01 | MIT | not checked | Compose date picker; needs core library desugaring for `java.time` | UI, not a date library |
| `bahadurbaniya/Date-Converter-Bikram-Sambat-to-English-Date` | not published | 0.0.1, 2019 | MIT | 1970–2100 | lookup table; README notes 364-day years 1974 and 1990 and 366-day years 1976, 1984, 1988, 2003 | not a library on Maven |

**Data quality:** every table above stops at BS 2090 or 2100, and the years they flag as 364 or 366
days differ between them. The benchmark's "agrees" column records where each one differs from
`shared/data`. None ships tithi or holiday data.

**Benchmark set:** shivathapaa core 3.3.0, `adbs-core` v4.0.0, AagitoEx `nepdate`
0.0.1-beta01 and `date_picker_converter` 3.0.0, plus `java.time.LocalDate` as a floor (it is not
a competitor, but no BS library can convert faster than `LocalDate.of(...).toEpochDay()` on the
same inputs). The method and results are in [the benchmarks](benchmarks.md).

Sources: https://github.com/shivathapaa/Nepali-Date-Picker, https://github.com/dhiraj-var/adbs,
https://jitpack.io/#dhiraj-var/adbs, https://github.com/sujit-sharma/bikram-sambat,
https://github.com/keyrunHORNET/date_picker_converter, https://github.com/AagitoEx/nepdate,
https://github.com/AagitoEx/compose-nepcal, https://github.com/theGBguy/KomposeNepaliDatePicker,
https://repo1.maven.org/maven2/io/github/shivathapaa/nepali-date-picker-core/maven-metadata.xml.
