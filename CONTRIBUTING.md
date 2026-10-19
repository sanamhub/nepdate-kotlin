# Contributing

Bug reports and fixes are welcome. For a new feature or an API change, open an issue first so the
design can be agreed before you write code. Report security problems as described in
[SECURITY.md](SECURITY.md).

## Scope

Calendar data and the conversion rules come from the [NepDate hub](https://github.com/sanamhub/nepdate),
copied into `shared/`. A wrong date, tithi or holiday is a data problem: report it there. Never
edit `shared/` here; CI checks it against `tools/shared.sha256`.

## Setup

JDK 17 or later. Gradle provisions the JDK 17 toolchain if it is missing. Before a pull request:

```bash
./scripts/ci.sh
```

It runs every check CI runs and ends with `ci: N/N gates passed`.

## Rules the build enforces

- `nepdate` depends only on `kotlin-stdlib`, and `nepdate-patro` only on `kotlin-stdlib` and
  `nepdate`. The core holds no patro data, not even in tests.
- `explicitApi()` is on and the committed `api/*.api` dumps are the public surface. A new public
  symbol also goes in [docs/API.md](docs/API.md).
- The core imports `java.*` only in `JavaTime.kt`. No reflection, no `android.*`, no coroutines or
  value classes in the public API.
- Conversion, arithmetic and parsing allocate nothing besides the returned `NepaliDate`; patro
  lookups allocate nothing. The allocation tests fail otherwise.
- `*.generated.kt` files come from `:codegen`. Change the generator and run `./gradlew :codegen:run`.
- A C# parity test may be skipped only for a deviation listed in `shared/spec/PARITY.md`, with a
  comment naming it.

Every public symbol needs KDoc that says what it returns and what it throws. A dependency of the
published artifacts beyond `kotlin-stdlib` needs an ADR first.

## Pull requests

- One change per pull request, with tests for any behaviour change.
- A line in the `Unreleased` section of [CHANGELOG.md](CHANGELOG.md) for anything a user notices.
- A significant design decision gets an ADR in [docs/adr/](docs/adr/README.md).
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/): `type(scope):
  summary`, imperative, lower case, no trailing period. The body says why.

## Dependencies

Dependabot updates GitHub Actions only. Gradle dependencies are updated by hand, because each
update needs the checksums in `gradle/verification-metadata.xml` regenerated:

```bash
./gradlew --write-verification-metadata sha256 build :codegen:run --args=--check animalsnifferMain
```

Check the new checksums in the diff.

## Syncing the shared data

Run the hub's `tools/shared_sync.py` on this checkout, then
`./scripts/shared-sums.sh --write <hub checkout>` and `./gradlew :codegen:run`.
