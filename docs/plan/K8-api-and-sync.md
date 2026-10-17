# K8: Shared sync and public-API gate

Read first: PARSING §2 at hub `c6cc327`, `docs/API.md`, ADR-0004 gate 5, K7's report, and the TS
port's `7a2709d` (it removed NFC and joiner removal).

## Tasks

### K8-01 Sync `shared/` to hub c6cc327
Copy the hub's `spec/` and `data/` into `shared/` and set `shared/VERSION` to
`c6cc32784f4cae375085158ba2c88b7c4e67baa2`. This brings PARSING §2's rule that input is matched as
given: no Unicode normalisation and no removal of U+200C or U+200D. `parse.tsv` grows from 67 to 69
rows.

AC: `shared/` matches the hub at c6cc327 except for line endings (the repo keeps LF); `VERSION` is
the full hash; `parse.tsv` has 69 rows; `./gradlew :nepdate:test` passes.

### K8-02 Lenient parsing matches input as given
Check `parseLenient` for NFC, NFKC or U+200C/U+200D stripping. If any exists, remove it. Add a
unit test for a month name with each joiner inside it: strict reports `INVALID_CHARACTER`, lenient
reports `UNRECOGNIZED`. Negative control: add a temporary joiner-strip step, show the test fails,
then remove it.

AC: the new test fails when the suspected normalisation is added and passes without it; the two
new `parse.tsv` rows pass; no production code is weakened.

### K8-03 Public-API dump and gate
Use the Kotlin 2.4.20 Gradle plugin's built-in `abiValidation`, which is pinned in
`gradle/libs.versions.toml`. Generate and commit `nepdate/api/nepdate.api` and
`nepdate-patro/api/nepdate-patro.api`. Keep `checkKotlinAbi` in `scripts/ci.sh`; it now compares
against a real dump instead of doing nothing.

AC: both dumps are committed; `checkKotlinAbi` passes on the clean tree; renaming the public
`NepaliDate.toIsoString` makes `checkKotlinAbi` fail; restoring the name passes again.

### K8-04 Verification and report
Run `./scripts/ci.sh` and `./scripts/mutation.sh`, record both last lines, and update
`docs/plan/PROGRESS.md` and the K8 report.

AC: both scripts exit 0 and their last lines are in the report; no tag, publishing or repository
setting changed.

## Result (K8-04)

Shared is at hub `c6cc327`: PARSING §2 forbids normalisation, and `parse.tsv` has 69 rows. The
Kotlin parser already matched input as given, so the guard test and KDoc changed, not the parser.
The API dumps already existed and were current; `checkKotlinAbi` now runs from `check` too.

```text
ci: 11/11 gates passed
mutation: score 92% (threshold 92%, max surviving 49)
```

The two negative controls (joiner strip, API rename) and the doubtful points are in
[`K8-report.md`](K8-report.md). No tag, publishing or repository setting changed.
