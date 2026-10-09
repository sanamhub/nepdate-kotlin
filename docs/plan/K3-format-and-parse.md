# K3: Formatting and parsing

Read first: `shared/spec/FORMATTING.md`, `shared/spec/PARSING.md`, `docs/API.md` (names,
formatters, `nepaliDigits`, parse functions), ADR-0002 §6, PARITY D-04, D-05, D-06, D-11, D-15.

No `String.format`, no `java.text` (including `Normalizer`), no regular expressions, no
`split`, `substring`, `toInt()` or `String` concatenation on any path. Each formatter has one
implementation that appends to a caller's `StringBuilder` (`appendShortString`,
`appendLongString`, `formatTo`); the `String` forms call it with a new `StringBuilder` sized for
the longest output and return `toString()`. Parsers read the `CharSequence` by index.

The measured cost of this design against the naive one is in ADR-0002 §6.

## Tasks

### K3-01 Names and digits
- `Month.displayName`, `Month.shortName`, `Weekday.displayName`, `Weekday.shortName` from
  FORMATTING §1, stored as `private` arrays of string literals. `Month.shortName(NEPALI)` is the full
  name. `Month.ASHAD.shortName()` is `Asa`, `Month.ASHOJ.shortName()` is `Aso` (D-04).
- `nepaliDigits(n, minWidth)` (file `NepaliDigits.kt`, `@file:JvmName("NepaliDigits")`): the ASCII
  decimal form of `n`, zero-padded to at least `minWidth` digits (a leading `-` is not counted), with
  each digit `d` replaced by `(0x0966 + d).toChar()`. `minWidth` below 0 is treated as 0.

AC: `nepaliDigits(2081) == "२०८१"`; `nepaliDigits(5, 2) == "०५"`; `nepaliDigits(-12) == "-१२"`;
`nepaliDigits(Long.MIN_VALUE)` doesn't throw; every name in the FORMATTING §1 tables.

### K3-02 `toShortString`, `toLongString`, `toIsoString`
FORMATTING §3 and §4. `toIsoString()` is `YYYY-MM-DD` with ASCII digits (D-11).

`appendShortString` and `appendLongString` (docs/API.md) take the same options as the `String`
forms and append to `out`, returning `out`.

AC: all 1018 rows of `csharp-golden/dates.tsv`, columns `to_string` (`toString()`), `unicode`
(`toShortString(lang = NEPALI)`), `long_en`, `long_en_weekday`, `long_ne`, `long_ne_weekday`
(`toLongString` with `weekday` and `lang`), `dmy_dash_nopad`
(`toShortString(DateOrder.DMY, Separator.DASH, pad = false)`); the FORMATTING §3 and §4 examples;
`of(2081, 4, 15).toIsoString() == "2081-04-15"`.

### K3-03 `format(pattern, lang)` and `formatTo(out, pattern, lang)` (FORMATTING §5)
One left-to-right pass over the pattern; standard patterns first (the whole pattern equals one of
them). `format` is `formatTo(StringBuilder(pattern.length + 16), pattern, lang).toString()`.

AC: all 451 rows of `csharp-golden/format-pattern.tsv` except the 7 rows where the month of `bs`
is 3 or 6 and `pattern` contains a run of exactly three `M` (D-04), skipped by a filter with a
comment naming D-04; for those 7 rows, the result has `Asa` or `Aso` where the file has `Ash`. The
FORMATTING §5 examples.

### K3-04 Strict `parse` (PARSING §1)
`parse`, `parseOrNull`. A shared internal parser `parseStrictSerial(text): Int` returns the serial
(`>= 0`) or `-(kind.ordinal + 1)`, so it allocates nothing and `parseOrNull` creates no exception;
only the final `NepaliDate` is allocated. Kinds map by name: `ERR:InvalidCharacter` is
`INVALID_CHARACTER`, `ERR:WrongGroupCount` is `WRONG_GROUP_COUNT`, and so on for the eight kinds in
PARSING §4.

AC: every row of `shared/spec/vectors/parse.tsv` (57 rows), column `strict`, with the right kind for
each `ERR:` cell; `parse("२०८०/०५/१५") == of(2080, 5, 15)`.

### K3-05 `parseLenient` (PARSING §2 and §3)
- The month-name list of §3 as a private table of `(token, month)` pairs.
- Latin tokens compare with `CharSequence.regionMatches(..., ignoreCase = true)` against the input
  in place (locale-independent, no substring). Devanagari tokens compare exactly, char by char. No
  Unicode normalisation: the spec lists every accepted Devanagari spelling.
- The tokeniser records token start and end indexes in a small `IntArray` local to the call, not
  `String` pieces.
- Tokenise exactly as the "Implementation note" in §2 step 2 says, then steps 3 to 6.

AC: every row of `parse.tsv`, column `lenient`; `parseLenient("15 Shrawan 2080")`,
`parseLenient("Shrawan 15, 2080")` and `parseLenient("2080 Shrawan 15")` all give 2080-04-15.

### K3-06 Property test
`ParseRandomTest`: 200,000 strings from `Random(42)`, length 0..24, built from ASCII and Devanagari
digits, every separator in PARSING (plus `,`), ASCII letters, the Devanagari month names and era
markers, and random `Char` values (surrogates included).
- `parse` and `parseLenient` either return a date or throw `NepDateException`; any other throwable
  fails the test.
- `parseOrNull` never throws.
- When `parse(s)` succeeds, `parse(result.toString())` returns the same date.

AC: the test passes and runs in under 10 s on CI.

### K3-07 Fuzzing, properties, allocation, catalogue
- Jazzer `ParseFuzzTest` (`@FuzzTest`, ADR-0004): `parse`, `parseLenient`, `parseOrNull` on
  `FuzzedDataProvider.consumeRemainingAsString()`; same rules as K3-06. Commit the inputs that
  found bugs under `src/test/resources/.../ParseFuzzTestInputs/`; CI replays them.
- jqwik `FormatProperties`: for every date `d`, `parse(d.toString()) == d`,
  `parse(d.toIsoString().replace('-', '/')) == d`, `parseLenient(d.toLongString()) == d`, and
  `parseLenient(d.toShortString(lang = NEPALI)) == d`.
- `FormatAllocationTest` (ADR-0004): `appendShortString`, `appendLongString` and `formatTo`
  into a reused `StringBuilder` with enough capacity allocate 0 bytes per call; `parse` and
  `parseLenient` on valid input at most 24; `parseOrNull` on invalid input 0.
- Every catalogue row tagged `3` (`00-ROADMAP.md`, "C# test catalogue"), named after the row `id`,
  or a comment naming the deviation.

AC: the tests above pass.

## Definition of Done
Roadmap DoD + milestone M2 (with K2). Every new public symbol is called in `JavaApiTest.java`.

## Checkpoint (stop here for review)
The agent stops after K3 and posts the PR. The reviewer checks:
- [ ] `format-pattern.tsv` passes except exactly the 7 D-04 rows, and the filter names D-04.
- [ ] Every `ERR:` cell of `parse.tsv` maps to the right `NepDateException.Kind`.
- [ ] `grep -n "split\|substring\|toInt()\|String.format\|Regex" nepdate/src/main` finds nothing
      on a formatting or parsing path.
- [ ] `FormatAllocationTest` passes with the bounds above.
- [ ] Jazzer replays its inputs in `./scripts/ci.sh`; the 10-minute `fuzz.yml` run was started once
      and its result is linked in the PR.
- [ ] Every catalogue row tagged `3` has a test or a deviation comment.
