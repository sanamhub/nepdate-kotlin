# Other Kotlin and Java libraries for the Bikram Sambat calendar

Checked 2026-11-02 against Maven Central, deps.dev, JitPack, GitHub and the sources. The selection
follows [`shared/spec/BENCHMARK.md`](../shared/spec/BENCHMARK.md) section 7. Usage is the deps.dev
dependent count, read on 2026-11-02. The bar is 1 dependent, first release at least 12 months before
2026-11-02, a release or commit in the last 36 months, a license, not archived or deprecated, and a
build on the current toolchain.

## Candidate table

| Library | Coordinates | Version | Dependents | First release | Last release | License | Verdict |
|---|---|---|---:|---|---|---|---|
| `shivathapaa/Nepali-Date-Picker` | `io.github.shivathapaa:nepali-date-picker-core` | 3.3.0 | 1 | 2024-09-14 | 2026-09-22 | MPL-2.0 | selected |
| `dhiraj-var/adbs` | JitPack `com.github.dhiraj-var.adbs:adbs-core` | v4.0.0 | not on Maven Central | 2026 | 2026-09 | MIT | fails the 12-month bar and has no Maven Central dependent count |
| `AagitoEx/nepdate` | `io.github.aagitoex:nepdate` | 0.0.1-beta01 | 0 | 2022-07-04 | 2022-09-05 | MIT | fails 1 dependent and is an abandoned beta |
| `theGBguy/KomposeNepaliDatePicker` | `io.github.thegbguy:nepali-date-picker` | 1.0.2 | 0 | 2024-07-01 | 2024-10-26 | MIT | fails 1 dependent |
| `keyrunHORNET/date_picker_converter` | JitPack only | 3.0.0 | not on Maven Central | 2016 | 2021-10 | Apache-2.0 | fails the 36-month bar and has no Maven Central dependent count |
| `io.github.rongaru:nepali-calender` | `io.github.rongaru:nepali-calender` | 1.0.1 | 0 | 2024-04-05 | 2024-04-05 | MIT | fails 1 dependent |
| `sujit-sharma/bikram-sambat` | not published | 2.0.0 | none | 2026-09 | 2026-10 | MIT | fails the 12-month bar and is not on Maven Central |

`io.github.aagitoex:compose-nepcal` is a Compose date picker with no conversion API, so it is not a
candidate. The platform libraries (`java.time.chrono`, ThreeTen-Extra and ICU4J) have no Bikram
Sambat calendar.

## Selected library

`io.github.shivathapaa:nepali-date-picker-core` 3.3.0 is the only candidate that passes. The port
calls `NepaliDateConverter.convertNepaliToEnglish(y, m, d)` for B1 and
`NepaliDateConverter.convertEnglishToNepali(y, m, d)` for B2. It returns a date object with `year`,
`month` and `dayOfMonth`. The harness excludes the Compose runtime from the dependency because the
published jar does not need it at runtime.

## Correctness

shivathapaa differs from `shared/data` by one day at BS 2086-07-23, 2088-01-04 and 2089-06-10, in
both directions. It agrees on 61 of the 64 dates. A fast wrong answer is listed, never ranked first.

## Sources

https://repo1.maven.org/maven2/io/github/shivathapaa/nepali-date-picker-core/maven-metadata.xml,
https://api.deps.dev/v3alpha/systems/MAVEN/packages/io.github.shivathapaa%3Anepali-date-picker-core/versions/3.3.0:dependents,
https://github.com/shivathapaa/Nepali-Date-Picker, https://github.com/dhiraj-var/adbs,
https://github.com/AagitoEx/nepdate, https://github.com/theGBguy/KomposeNepaliDatePicker,
https://github.com/keyrunHORNET/date_picker_converter, https://github.com/sujit-sharma/bikram-sambat.
