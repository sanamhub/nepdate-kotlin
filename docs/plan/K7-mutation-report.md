# K7 mutation report

## Baseline (K7-01)

`./gradlew --console=plain :nepdate:pitest` on 2026-10-18, PIT 1.30.1, Gradle 9.6.0,
Temurin 17 toolchain, four PIT threads. The generated `Calendar_generatedKt` is excluded.

| Metric | Value |
|---|---|
| Mutations generated | 622 |
| Killed | 522 |
| Timed out (counted as detected) | 33 |
| Survived | 40 |
| No coverage | 27 |
| Mutation score | 555 / 622 = 89% |
| Test strength | 555 / (622 - 27) = 93% |

## Baseline survivors

| File | Line | Method | Mutant | Status |
|---|---|---|---|---|
| DateDiff.kt | 32 | `DateDiff::hashCode` | negated conditional | SURVIVED |
| DateDiff.kt | 33 | `DateDiff::hashCode` | Replaced integer addition with subtraction | SURVIVED |
| DateDiff.kt | 34 | `DateDiff::hashCode` | Replaced integer addition with subtraction | SURVIVED |
| DateDiff.kt | 35 | `DateDiff::hashCode` | Replaced integer addition with subtraction | SURVIVED |
| DateDiff.kt | 35 | `DateDiff::hashCode` | Replaced integer multiplication with division | SURVIVED |
| DateDiff.kt | 36 | `DateDiff::hashCode` | Replaced Unsigned Shift Right with Shift Left | SURVIVED |
| DateDiff.kt | 36 | `DateDiff::hashCode` | Replaced XOR with AND | SURVIVED |
| DateDiff.kt | 36 | `DateDiff::hashCode` | Replaced integer addition with subtraction | SURVIVED |
| DateDiff.kt | 36 | `DateDiff::hashCode` | Replaced integer multiplication with division | SURVIVED |
| DateDiff.kt | 37 | `DateDiff::hashCode` | replaced int return with 0 for io/github/sanamhub/nepdate/DateDiff::hashCode | SURVIVED |
| DateDiff.kt | 41 | `DateDiff::toString` | negated conditional | SURVIVED |
| FiscalYear.kt | 68 | `FiscalYear::label` | changed conditional boundary | SURVIVED |
| FiscalYear.kt | 81 | `FiscalYear::hashCode` | replaced int return with 0 for io/github/sanamhub/nepdate/FiscalYear::hashCode | SURVIVED |
| Formatting.kt | 20 | `FormattingKt::appendShort` | negated conditional | SURVIVED |
| Formatting.kt | 93 | `FormattingKt::appendCustomPattern` | changed conditional boundary | SURVIVED |
| LenientParsing.kt | 37 | `LenientParsingKt::nextNumber` | changed conditional boundary | SURVIVED |
| LenientParsing.kt | 58 | `LenientParsingKt::nextToken` | Replaced integer subtraction with addition | SURVIVED |
| LenientParsing.kt | 58 | `LenientParsingKt::nextToken` | changed conditional boundary | SURVIVED |
| LenientParsing.kt | 60 | `LenientParsingKt::nextToken` | changed conditional boundary | SURVIVED |
| LenientParsing.kt | 70 | `LenientParsingKt::nextToken` | changed conditional boundary | SURVIVED |
| LenientParsing.kt | 71 | `LenientParsingKt::nextToken` | Changed increment from 1 to -1 | NO_COVERAGE |
| LenientParsing.kt | 87 | `LenientParsingKt::scanLenient` | changed conditional boundary | SURVIVED |
| LenientParsing.kt | 101 | `LenientParsingKt::scanLenient` | changed conditional boundary | SURVIVED |
| MonthTokens.kt | 184 | `MonthTokensKt::foldCase` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 150 | `NepaliDate::monthsLaterSerial` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 150 | `NepaliDate::monthsLaterSerial` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 182 | `NepaliDate::plusYears` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 182 | `NepaliDate::plusYears` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 218 | `NepaliDate::hashCode` | replaced int return with 0 for io/github/sanamhub/nepdate/NepaliDate::hashCode | SURVIVED |
| NepaliDate.kt | 378 | `NepaliDate::ofOrNull` | replaced return value with null for io/github/sanamhub/nepdate/NepaliDate::ofOrNull | NO_COVERAGE |
| NepaliDate.kt | 457 | `NepaliDate$Companion::parseOrNull` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 459 | `NepaliDate::parseOrNull` | replaced return value with null for io/github/sanamhub/nepdate/NepaliDate::parseOrNull | NO_COVERAGE |
| NepaliDate.kt | 465 | `NepaliDate$Companion::fromScan` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 542 | `NepaliDateKt::adError` | negated conditional | SURVIVED |
| NepaliDate.kt | 556 | `NepaliDateKt::pad2` | changed conditional boundary | SURVIVED |
| NepaliDate.kt | 556 | `NepaliDateKt::pad2` | negated conditional | SURVIVED |
| NepaliDate.kt | 556 | `NepaliDateKt::pad2` | replaced return value with "" for io/github/sanamhub/nepdate/NepaliDateKt::pad2 | SURVIVED |
| NepaliDateRange.kt | 9 | `NepaliDateRange::contains` | negated conditional | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::contains` | replaced boolean return with false for io/github/sanamhub/nepdate/NepaliDateRange::contains | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::contains` | replaced boolean return with true for io/github/sanamhub/nepdate/NepaliDateRange::contains | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::contains` | replaced boolean return with true for io/github/sanamhub/nepdate/NepaliDateRange::contains | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::indexOf` | negated conditional | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::indexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/NepaliDateRange::indexOf | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::indexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/NepaliDateRange::indexOf | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::lastIndexOf` | negated conditional | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::lastIndexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/NepaliDateRange::lastIndexOf | NO_COVERAGE |
| NepaliDateRange.kt | 9 | `NepaliDateRange::lastIndexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/NepaliDateRange::lastIndexOf | NO_COVERAGE |
| NepaliDigits.kt | 21 | `NepaliDigits::nepaliDigits` | Replaced integer subtraction with addition | SURVIVED |
| Parsing.kt | 32 | `ParsingKt::digitValue` | changed conditional boundary | SURVIVED |
| Parsing.kt | 33 | `ParsingKt::digitValue` | changed conditional boundary | SURVIVED |
| Parsing.kt | 80 | `ParsingKt::scanStrict` | changed conditional boundary | SURVIVED |
| Parsing.kt | 89 | `ParsingKt::scanStrictValid` | changed conditional boundary | SURVIVED |
| Quarter.kt | 34 | `ThreeMonths::contains` | negated conditional | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::contains` | replaced boolean return with false for io/github/sanamhub/nepdate/ThreeMonths::contains | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::contains` | replaced boolean return with false for io/github/sanamhub/nepdate/ThreeMonths::contains | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::contains` | replaced boolean return with true for io/github/sanamhub/nepdate/ThreeMonths::contains | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::contains` | replaced boolean return with true for io/github/sanamhub/nepdate/ThreeMonths::contains | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::contains` | replaced boolean return with true for io/github/sanamhub/nepdate/ThreeMonths::contains | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::indexOf` | negated conditional | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::indexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/ThreeMonths::indexOf | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::indexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/ThreeMonths::indexOf | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::indexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/ThreeMonths::indexOf | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::lastIndexOf` | negated conditional | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::lastIndexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/ThreeMonths::lastIndexOf | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::lastIndexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/ThreeMonths::lastIndexOf | NO_COVERAGE |
| Quarter.kt | 34 | `ThreeMonths::lastIndexOf` | replaced int return with 0 for io/github/sanamhub/nepdate/ThreeMonths::lastIndexOf | NO_COVERAGE |
| Serial.kt | 49 | `SerialKt::packedAdOf` | Replaced integer subtraction with addition | SURVIVED |
