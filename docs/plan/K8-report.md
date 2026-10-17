# K8 report

## Commits

| Commit | Task |
|---|---|
| `a14af83` | K8 plan |
| `5ad2f82` | K8-01 sync `shared/` to hub c6cc327 |
| `213c5db` | K8-02 lenient input matched as given |
| `0d1a93c` | K8-03 API dump check in `build` |
| this commit | K8-04 report and checkpoint |

## Shared sync

`shared/VERSION` is `c6cc32784f4cae375085158ba2c88b7c4e67baa2`. `shared/spec/PARSING.md` now has
the no-normalisation rule, and `shared/spec/vectors/parse.tsv` has 69 rows including the U+200C and
U+200D rows. A byte comparison of `shared/spec` and `shared/data` against the hub at c6cc327, with
CRLF folded to LF, reports no difference. The two joiner rows are:

```text
15 सा‍उन 2080	ERR:InvalidCharacter	ERR:Unrecognized
15 सा‌उन 2080	ERR:InvalidCharacter	ERR:Unrecognized
```

`ParseTest` now asserts 69 rows. The test count change is in the sync commit because the vector file
and its row count move together.

## Public-API gate

The Kotlin 2.4.20 Gradle plugin's built-in `abiValidation` is the gate. The version is pinned by
`kotlin = "2.4.20"` in `gradle/libs.versions.toml`; there is no separate validator plugin.

Committed dumps:

- `nepdate/api/nepdate.api`
- `nepdate-patro/api/nepdate-patro.api`

The dumps already existed from the K3 and K4 phases and were current: `updateKotlinAbi` produced no
diff. This phase wires `checkKotlinAbi` into the `check` task, so `./gradlew build` fails on a public
signature change as well as `scripts/ci.sh` gate 5.

## Verification

```text
ci: 11/11 gates passed
mutation: score 92% (threshold 92%, max surviving 49)
```

`scripts/ci.sh` exited 0. `scripts/mutation.sh` exited 0 after 4m 18s. The mutation score is
unchanged from K7.

## Negative controls

1. A temporary joiner strip in `scanLenient` (`replace("\u200C", "").replace("\u200D", "")`) made
   `ParseTest.zero-width joiners inside a month name are not removed` fail with
   `AssertionFailedError` at `ParseTest.kt:85`; the vector test failed too. Removing the strip made
   both pass.
2. Renaming public `NepaliDate.toIsoString` to `toIsoStringX` made `:nepdate:checkKotlinAbi` exit 1
   with this diff line:

```text
-	public final fun toIsoString ()Ljava/lang/String;
+	public final fun toIsoStringX ()Ljava/lang/String;
```

Restoring the name made `:nepdate:checkKotlinAbi` and `:nepdate-patro:checkKotlinAbi` pass.

## Doubtful points

1. The Kotlin parser had no NFC, NFKC or joiner-strip code to remove. K8-02 therefore adds the guard
   test and the KDoc sentence, not a production fix. The negative control shows the test catches a
   future strip step.
2. The `.api` dumps were already committed in K3/K4 and already current. K8-03 does not add dump
   content; it makes the existing gate run in `build` and proves it with the rename. If the intent
   was to create the dumps from scratch, that was done earlier.
3. `checkKotlinAbi` is still marked Experimental in Kotlin 2.4.20. The project already opted in with
   `@OptIn(ExperimentalAbiValidation::class)` in ADR-0004 gate 5.
4. The hub sync touches only `VERSION`, `PARSING.md` and `parse.tsv`. The hub's assets and tools
   changes since 1d7ca17 are not part of `shared/`.
5. The mutation score stayed at 92% and 49 survivors. The two new vector rows and the joiner test
   did not change PIT's result.
