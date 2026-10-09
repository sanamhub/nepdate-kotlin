# Progress (Kotlin port)

Tasks from [`00-ROADMAP.md`](00-ROADMAP.md), in the format of the hub's `docs/handover.md` §3 step 7.
There is no GitHub remote yet: "CI last line" is `./scripts/ci.sh` run locally (JDK 25 from Android
Studio runs Gradle; the foojay resolver provides the JDK 17 toolchain).

| Task | Status | Commit | CI last line | Notes and open questions |
|---|---|---|---|---|
| K0-01 | done | 0466b2c | `ci: 6/6 gates passed` | Gradle 9.6.0 wrapper with `distributionSha256Sum`; Kotlin 2.4.20; detekt 2.0.0-alpha.6 (`dev.detekt`, the only detekt line built for Kotlin 2.4); JUnit 5.14.4 (the platform version jqwik 1.10.1 uses). The core POM lists exactly `org.jetbrains.kotlin:kotlin-stdlib`. `*.bat` kept CRLF in `.gitattributes`. |
| K0-02 | done | 31f2255 | `ci: 6/6 gates passed` | 11 tests in `:codegen`. `:codegen:run` twice gives the same SHA-1; `--args=--check` exits 1 after a hand edit and writes nothing. |
| K0-03 | done | 783c865 | `ci: 6/6 gates passed` | Gates: ktlint+detekt, build, test+koverVerify, codegen check, checkKotlinAbi, animalsniffer (API 21). An extra space in a `.kt` file fails gate 1 (ktlint "Unnecessary long whitespace"). animalsniffer was wired here because no later task owns gate 8; it fails on a `java.time` call. `kotlin.compiler.execution.strategy=in-process`: two Gradle daemons crashed out of memory on this shared machine. |
| K0-04 | done | bedf715 | `ci: 6/6 gates passed` | Workflows lint clean with actionlint 1.7.12, every action pinned by SHA; `shared-check` and `shared-sync` call hub `94946ac`. `verification-metadata.xml` was written from the CI gates, not only `help` (plan said `help`; that would fail `build` on unlisted checksums). Extra: a `test-java-11` job (ADR-0004 asks for it) with `-PtestJavaVersion=11`, not yet run locally since `:nepdate` has no tests until K1. Blocked on the human: create `sanamhub/nepdate-kotlin`, protect `main`, `production` environment, Central namespace; "CI green on the first PR" needs that remote. |
