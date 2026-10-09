# Contributing

Rules for code, tests and structure are in [`AGENTS.md`](AGENTS.md); they apply to people too.
Prose, comments and commit messages follow
[`.claude/skills/writing-style/SKILL.md`](.claude/skills/writing-style/SKILL.md), with no AI
attribution lines.

## Checks

JDK 17 or later (Gradle provisions the JDK 17 toolchain if it is missing). Before a PR:

```bash
./scripts/ci.sh
```

The last line reads `ci: N/N gates passed`.

## Data and spec

`shared/` is a copy of the hub [`sanamhub/nepdate`](https://github.com/sanamhub/nepdate). Never edit
it: report data or spec problems there. A weekly PR syncs it.

## Dependencies

Dependabot only updates GitHub Actions. Gradle dependencies are updated by hand, because every
update needs the checksums in `gradle/verification-metadata.xml` regenerated:

```bash
./gradlew --write-verification-metadata sha256 build :codegen:run --args=--check animalsnifferMain
```

Review the new checksums in the PR. A dependency of the published artifacts beyond `kotlin-stdlib`
needs an ADR first.
