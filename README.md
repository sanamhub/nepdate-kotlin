# nepdate-kotlin

Bikram Sambat (Nepali calendar) for Kotlin, Java and Android. Part of the NepDate family: shared data
and spec live in the hub [`sanamhub/nepdate`](https://github.com/sanamhub/nepdate); the C# original
is [NepDate](https://github.com/RajuPrasai/NepDate). The Android app in
[`nepdate-mobile`](https://github.com/sanamhub/nepdate-mobile) is its first user.

- Two Maven Central artifacts under `io.github.sanamhub`, same version, released together:
  `nepdate` (conversion, formatting, parsing, arithmetic, fiscal year) and `nepdate-patro`, the
  patro (पात्रो, the Nepali almanac: tithi, public holidays and events, BS 2077-01-01 to 2084-08-20
  today). `nepdate-patro` depends on `nepdate`; `nepdate` contains no patro data, so apps that only
  convert dates stay small.
- Plain Kotlin/JVM jars, no dependencies besides the Kotlin standard library (and `nepdate` for
  `nepdate-patro`). Work on any JVM 11+
  and on Android API 21+ without desugaring; `java.time` interop is a separate file that only loads
  when you call it.
- Java-friendly: static factories, overloads for default arguments, enums for months and weekdays.
- BS 1901-01-01 to 2199-12-30 (AD 1844-04-11 to 2143-04-15). Same results as C# NepDate and every
  other port, proved by the shared vectors.

Neither `java.time`, ThreeTen-Extra, ICU4J nor Android's `android.icu` has a Bikram Sambat
calendar (sources in [docs/competitors.md](docs/competitors.md)). Use this library for Nepali dates.

**Status:** planning complete, implementation not started (next: phase K0).

## Where to start

| You are | Read |
|---|---|
| an AI agent implementing a phase | [AGENTS.md](AGENTS.md) → [docs/plan/00-ROADMAP.md](docs/plan/00-ROADMAP.md) |
| reviewing the API | [docs/API.md](docs/API.md) |
| comparing with other Kotlin and Java libraries | [docs/competitors.md](docs/competitors.md) |
| wondering "why is it like this?" | [docs/adr/README.md](docs/adr/README.md) |
| looking for the algorithm or data | [shared/spec/README.md](shared/spec/README.md) (changes go to the hub) |

## Layout

```
shared/                  hub copy: data/, spec/, VERSION
settings.gradle.kts      modules :nepdate, :nepdate-patro, :codegen, :benchmarks
gradle/libs.versions.toml
nepdate/                 core artifact                       (phase K0)
nepdate-patro/           patro artifact                      (phase K4)
codegen/                 JVM app: generates the Kotlin tables from shared/data (not published)
benchmarks/              kotlinx-benchmark (JMH) vs competitors (phase K5, not published)
docs/                    API.md, competitors.md, adr/, plan/, runbooks/
```

## License

MIT ([LICENSE](LICENSE)). Calendar data from NepDate by Raju Prasai and contributors, MIT
([shared/data/NOTICE](shared/data/NOTICE)). Not affiliated with the Government of Nepal.
