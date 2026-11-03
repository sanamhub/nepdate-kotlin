# API reference

The complete public surface of `nepdate`, `nepdate-patro` and `nepdate-serialization`. A pull
request that changes it updates this file too. Behaviour is specified in `shared/spec/`,
conventions in ADR-0003. Names follow `java.time.LocalDate` where a counterpart exists. Every type
is immutable and thread-safe.

The first column of each table is the C# member the port answers to. The second column is the
Kotlin name and signature; the third is a short example.

```kotlin
import io.github.sanamhub.nepdate.NepaliDate
```

Errors are one type. `NepDateException` extends `IllegalArgumentException`, the exception
`java.time.LocalDate.of` callers already catch, and carries a `kind`. Functions that can fail are
marked `@Throws(NepDateException::class)` for Java.

## 1. Dates, identity and comparison

```kotlin
public class NepaliDate private constructor(/* serial: Int, ymd: Int */) : Comparable<NepaliDate>
```

| C# member | Kotlin | Example |
|---|---|---|
| `NepaliDate(int yearBs, int monthBs, int dayBs)` | `NepaliDate.of(year: Int, month: Int, day: Int): NepaliDate` | `NepaliDate.of(2081, 4, 15)` |
| `NepaliDate(int yearBs, NepaliMonths monthBs, int dayBs)` | `NepaliDate.of(year: Int, month: Month, day: Int)` | `NepaliDate.of(2081, Month.SHRAWAN, 15)` |
| `NepaliDate(string rawNepaliDate)` | `NepaliDate.parse(text: CharSequence)` | `NepaliDate.parse("2081-04-15")` |
| `NepaliDate(DateTime engDate)` | `NepaliDate.ofGregorian(year: Int, month: Int, day: Int)` | `NepaliDate.ofGregorian(2024, 7, 30)` |
| `int Year { get; }` | `year: Int` | `NepaliDate.of(2081, 4, 15).year` is `2081` |
| `int Month { get; }` | `monthValue: Int` | `NepaliDate.of(2081, 4, 15).monthValue` is `4` |
| `NepaliMonths MonthName { get; }` | `month: Month` | `NepaliDate.of(2081, 4, 15).month` is `Month.SHRAWAN` |
| `int Day { get; }` | `dayOfMonth: Int` | `NepaliDate.of(2081, 4, 15).dayOfMonth` is `15` |
| `DayOfWeek DayOfWeek { get; }` | `dayOfWeek: Weekday` | `NepaliDate.of(2081, 4, 15).dayOfWeek` is `Weekday.TUESDAY` |
| `int DayOfYear { get; }` | `dayOfYear: Int` | `NepaliDate.of(2081, 4, 15).dayOfYear` is `109` |
| `int MonthEndDay { get; }` | `lengthOfMonth(): Int` | `NepaliDate.of(2081, 4, 15).lengthOfMonth()` is `32` |
| `NepaliDate MonthEndDate()` | `lastDayOfMonth(): NepaliDate` | `NepaliDate.of(2081, 4, 15).lastDayOfMonth()` is `2081-04-32` |
| none (Kotlin extra) | `firstDayOfMonth(): NepaliDate` | `NepaliDate.of(2081, 4, 15).firstDayOfMonth()` is `2081-04-01` |
| none (Kotlin extra) | `quarter: Quarter` | `NepaliDate.of(2081, 4, 15).quarter` is `Quarter.Q1` |
| none (Kotlin extra) | `fiscalYear: FiscalYear` | `NepaliDate.of(2081, 4, 15).fiscalYear` is `FiscalYear(2081)` |
| `DateTime EnglishDate { get; }` | `gregorianYear`, `gregorianMonth`, `gregorianDay: Int` | `NepaliDate.of(2081, 4, 15).gregorianYear` is `2024` |
| `static NepaliDate Today { get; }` | `nepaliToday(zone: ZoneId = KATHMANDU): NepaliDate` | `nepaliToday(KATHMANDU)` |
| `static NepaliDate Now { get; }` | the same `nepaliToday(zone)` (a date has no time of day) | `nepaliToday()` |
| `static readonly NepaliDate MinValue` | `NepaliDate.MIN` | `NepaliDate.MIN` is `1901/01/01` |
| `static readonly NepaliDate MaxValue` | `NepaliDate.MAX` | `NepaliDate.MAX` is `2199/12/30` |
| `bool IsToday()` | `isToday(zone: ZoneId): Boolean` | `nepaliToday().isToday(KATHMANDU)` |
| `bool IsYesterday()` | `isYesterday(zone: ZoneId): Boolean` | `nepaliToday().minusDays(1).isYesterday(KATHMANDU)` |
| `bool IsTomorrow()` | `isTomorrow(zone: ZoneId): Boolean` | `nepaliToday().plusDays(1).isTomorrow(KATHMANDU)` |
| `int CompareTo(NepaliDate obj)` | `compareTo(other: NepaliDate): Int` | `NepaliDate.MIN < NepaliDate.MAX` |
| `bool Equals(NepaliDate other)`, `override bool Equals(object obj)` | `equals(other: Any?): Boolean` | `NepaliDate.of(2081, 4, 15) == NepaliDate.ofGregorian(2024, 7, 30)` |
| `override int GetHashCode()` | `hashCode(): Int` | `NepaliDate.of(2081, 4, 15).hashCode()` |
| `bool IsLeapYear()` | `yearLength(): Int` (PARITY D-12) | `NepaliDate.of(2081, 1, 1).yearLength()` is `366` |
| `void Deconstruct(out int, out int, out int)` | `component1()`, `component2()`, `component3()` | `val (year, month, day) = NepaliDate.of(2081, 4, 15)` |
| `static bool operator ==`, `!=`, `<`, `<=`, `>`, `>=` | the same six operators through `equals` and `compareTo` | `NepaliDate.of(2081, 1, 1) <= NepaliDate.of(2081, 4, 15)` |

A valid date is 1901-01-01 to 2199-12-30 BS, which is 1844-04-11 to 2143-04-15 AD. `of` reports the
first failure in the order year, month, day. The month length comes from the BS table, not a fixed
30 or 31, so `2081-04-32` is a real date.

```kotlin
val (year, month, day) = NepaliDate.of(2081, 4, 15)
NepaliDate.of(2081, 4, 15).yearLength()          // 366: the BS year, not the Gregorian one
NepaliDate.fromYmd(20810415)                     // 2081-04-15
NepaliDate.of(2081, 4, 15).toYmd()               // 20810415
```

`IsLeapYear` is not ported. It followed the Gregorian year of the date and so changed in the middle
of a BS year; `yearLength()` gives the length of the BS year instead, 364 to 367 (PARITY D-12).

There is no `IsDefault`: a date can't be invalid, so a zero value has nothing to mean (PARITY
D-01). `ofOrNull` is the Kotlin form of a factory that may fail.

## 2. Conversion and serialization

| C# member | Kotlin | Example |
|---|---|---|
| `static NepaliDate DateTimeExtensions.ToNepaliDate(this DateTime)` | `LocalDate.toNepaliDate(): NepaliDate` | `LocalDate.of(2024, 7, 30).toNepaliDate()` |
| `static NepaliDate SmartParsingExtensions.ToNepaliDate(this string)` | `NepaliDate.parseLenient(text: CharSequence)` | `NepaliDate.parseLenient("15 Shrawan 2080")` |
| `static bool SmartParsingExtensions.TryToNepaliDate(this string, out NepaliDate)` | `NepaliDate.parseLenientOrNull(text: CharSequence): NepaliDate?` | `NepaliDate.parseLenientOrNull("15 Shrawan 2080")` |
| `static IEnumerable<NepaliDate> BulkConvert.ToNepaliDates(IEnumerable<DateTime>, bool)` | `toNepaliDates(dates: Iterable<LocalDate>): List<NepaliDate>` | `toNepaliDates(listOf(LocalDate.of(2024, 7, 30)))` |
| `static IEnumerable<DateTime> BulkConvert.ToEnglishDates(IEnumerable<string>, bool)` | `toEnglishDates(strings: Iterable<String>): List<LocalDate>` | `toEnglishDates(listOf("2081-04-15"))` |
| `static IEnumerable<DateTime> BulkConvert.ToEnglishDates(IEnumerable<NepaliDate>, bool)` | `toEnglishDates(dates: Iterable<NepaliDate>): List<LocalDate>` | `toEnglishDates(listOf(NepaliDate.of(2081, 4, 15)))` |
| `static IEnumerable<NepaliDate> BatchProcessToNepaliDates(IEnumerable<DateTime>, int)` | the same `toNepaliDates` | `toNepaliDates(dates)` |
| `static IEnumerable<DateTime> BatchProcessToEnglishDates(IEnumerable<NepaliDate>, int)` | the same `toEnglishDates` | `toEnglishDates(dates)` |
| `sealed class NepaliDateTypeConverter` | `NepaliDate.fromYmd(value: Int)` and `toYmd(): Int` | `NepaliDate.fromYmd(20810432)` |
| `NepaliDateJsonConverter` (System.Text.Json) | `NepaliDateSerializer`, from the optional `nepdate-serialization` artifact | `Json.encodeToString(NepaliDateSerializer, date)` |

The date part of a `java.time.LocalDate` is what converts; a time of day is discarded. Batch
conversion is ordered, unlike the C# parallel path, and there is one function each way, not the
five C# entry points. The `useParallel` and `batchSize` arguments do not cross into the port.

```kotlin
LocalDate.of(2024, 7, 30).toNepaliDate()                       // 2081-04-15
NepaliDate.of(2081, 4, 15).toLocalDate()                       // 2024-07-30
NepaliDate.MIN.toEpochDay()                                    // -45920
NepaliDate.ofEpochDay(0)                                       // 2026-09-17
toEnglishDates(listOf("2081-04-15", "1901-01-01"))             // [2024-07-30, 1844-04-11]
```

The canonical text form is `YYYY-MM-DD`, from `toIsoString()`. The object form, the Newtonsoft and
XML adapters and the `TypeConverter` type are not ported (PARITY D-11). The `YYYYMMDD` integer is
part of the contract and does not wrap: `fromYmd` splits the integer into year, month and day
before validating, so `Int.MAX_VALUE` is an out-of-range year rather than a truncated date.

```kotlin
@Serializable
data class Festival(
    val name: String,
    @Serializable(with = NepaliDateSerializer::class) val date: NepaliDate,
)

Json.encodeToString(Festival("New Year", NepaliDate.of(2081, 1, 1)))
// {"name":"New Year","date":"2081-01-01"}
```

`NepaliDateSerializer` is a `KSerializer<NepaliDate>`: `serialize` writes `toIsoString()` and
`deserialize` reads it back, throwing `NepDateException` for text that is not a date and
`SerializationException` for a JSON null. Register it once with
`SerializersModule { contextual(NepaliDate::class, NepaliDateSerializer) }` and mark a field
`@Contextual` if it should be the default for plain `NepaliDate` fields. The artifact is optional, so
a project that never serializes a date does not resolve `kotlinx-serialization`.

## 3. Arithmetic

| C# member | Kotlin | Example |
|---|---|---|
| `NepaliDate AddMonths(double months, bool awayFromMonthEnd = false)` | `plusMonths(months: Long, overflow: MonthOverflow = MonthOverflow.CLAMP)` | `NepaliDate.of(2081, 4, 32).plusMonths(2)` is `2081-06-30` |
| `NepaliDate AddDays(double days)` | `plusDays(days: Long)` | `NepaliDate.of(2081, 4, 15).plusDays(1)` is `2081-04-16` |
| `NepaliDate AddYears(int years, bool awayFromMonthEnd = false)` | `plusYears(years: Long, overflow: MonthOverflow = MonthOverflow.CLAMP)` | `NepaliDate.of(2081, 4, 15).plusYears(1)` |
| `TimeSpan Subtract(NepaliDate nepDateTo)` | `daysUntil(other: NepaliDate): Long` and `until(other: NepaliDate): DateDiff` | `NepaliDate.of(2081, 4, 15).daysUntil(NepaliDate.of(2081, 4, 20))` is `5` |
| `static TimeSpan operator -(NepaliDate d1, NepaliDate d2)` | `operator fun minus(other: NepaliDate): DateDiff` | `NepaliDate.of(2081, 4, 20) - NepaliDate.of(2081, 4, 15)` |

`plusMonths` and `plusYears` take a whole number of months or years and an explicit overflow mode.
`MonthOverflow.CLAMP` uses the last day of the target month (`2081-04-32` plus two months is
`2081-06-30`); `MonthOverflow.SPILL` carries the extra days into the next month (`2081-07-02`).
Fractional months and days are not ported (PARITY D-02 and D-03).

The two day differences point in opposite directions and the names are not interchangeable
(PARITY D-07):

- `daysUntil(other)` is `other - this`, positive when `other` is later. `until(other)` is the same
  distance in years, months and days.
- `this - other` keeps the C# direction of `operator -`, positive when `this` is later.

```kotlin
val earlier = NepaliDate.of(2081, 4, 15)
val later = NepaliDate.of(2081, 4, 20)
earlier.daysUntil(later)      // 5
(later - earlier).totalDays   // 5
earlier.minusDays(1)          // 2081-04-14
earlier.until(later).days     // 5
```

`until` returns a `DateDiff` with `years`, `months`, `days`, `totalDays` and `isNegative`, the
calendar distance of `ALGORITHM.md` section 8a.

## 4. Formatting

| C# member | Kotlin | Example |
|---|---|---|
| `override string ToString()` | `toString()` | `NepaliDate.of(2081, 4, 15).toString()` is `2081/04/15` |
| `string ToString(DateFormats, Separators = ForwardSlash, bool = true)` | `toShortString(order: DateOrder = YMD, separator: Separator = SLASH, pad: Boolean = true, lang: Lang = ENGLISH)` | `toShortString(DateOrder.DMY, Separator.DASH, pad = false)` is `15-4-2081` |
| `string ToLongDateString(bool = true, bool = false, bool = true)` | `toLongString(weekday: Boolean = false, year: Boolean = true, pad: Boolean = true, lang: Lang = ENGLISH)` | `toLongString()` is `Shrawan 15, 2081` |
| `string ToUnicodeString(DateFormats = YearMonthDay, Separators = ForwardSlash, bool = true)` | the same `toShortString` with `Lang.NEPALI` | `toShortString(lang = Lang.NEPALI)` is `२०८१/०४/१५` |
| `string ToLongDateUnicodeString(bool = true, bool = false, bool = true)` | the same `toLongString` with `Lang.NEPALI` | `toLongString(lang = Lang.NEPALI)` is `साउन १५, २०८१` |
| `string ToString(string format, IFormatProvider = null)` | `format(pattern: String, lang: Lang = ENGLISH)` | `format("dd.MM.yy")` is `15.04.81` |
| `bool ISpanFormattable.TryFormat(...)` | `appendShortString`, `appendLongString` and `formatTo` into a `StringBuilder` | `date.formatTo(builder, "yyyy-MM-dd")` |
| `enum DateFormats` | `DateOrder` | `DateOrder.DMY` |
| `enum Separators` | `Separator` | `Separator.DASH` |
| `enum NepaliMonths` | `Month` | `Month.SHRAWAN` |

`DateOrder` has the six orders `YMD`, `YDM`, `MYD`, `MDY`, `DYM` and `DMY`. `Separator` has the six
separators `SLASH`, `BACKSLASH`, `DOT`, `UNDERSCORE`, `DASH` and `SPACE`, with the character in
`Separator.char`. `Lang` has `ENGLISH` and `NEPALI`.

`format` reads the standard patterns `""`, `G`, `g` and `d` as `YYYY/MM/DD`, `D` as the long form and
`s` as `YYYY-MM-DD`; anything else is a custom pattern. In a custom pattern:

- `yyyy` and longer runs write the four-digit year, `y`, `yy` and `yyy` the last two digits.
- `MMMM` writes the month name, `MMM` its abbreviation, `MM` two digits, `M` the number.
- `dd` writes two digits, any other run of `d` the number.
- A backslash escapes the next character, and a backslash at the very end writes a backslash.
- Text inside single quotes is literal, and an unterminated quote runs to the end.

`MMM` gives `Asa` for Ashad and `Aso` for Ashoj, where C# writes `Ash` for both (PARITY D-04).

```kotlin
val date = NepaliDate.of(2081, 4, 15)
date.toLongString(weekday = true)                 // Tuesday, Shrawan 15, 2081
date.format("yyyy-MM-dd")                         // 2081-04-15
date.format("""yyyy\M\M""")                       // 2081MM
date.format("""yyyy\""")                          // 2081\ (the final backslash is literal)
date.toShortString(DateOrder.DMY, Separator.DASH, lang = Lang.NEPALI)   // १५-०४-२०८१
```

The three short forms, the long form and the pattern form each also have an `append*` twin that
writes into a `StringBuilder` and returns it. They allocate nothing once the builder has room,
which is the port's form of `TryFormat`: a builder grows instead of reporting a short buffer.

Names and digits on their own, for a calendar grid or a label:

| C# member | Kotlin | Example |
|---|---|---|
| `NepaliMonths` names | `Month.displayName(lang: Lang = ENGLISH)` | `Month.SHRAWAN.displayName()` is `Shrawan` |
| the month abbreviation of PARITY D-04 | `Month.shortName(lang: Lang = ENGLISH)` | `Month.ASHAD.shortName()` is `Asa` |
| `DayOfWeek` names | `Weekday.displayName(lang: Lang = ENGLISH)` | `Weekday.TUESDAY.displayName()` is `Tuesday` |
| none (Kotlin extra) | `Weekday.shortName(lang: Lang = ENGLISH)` | `Weekday.TUESDAY.shortName()` is `Tue` |
| the digits of `ToUnicodeString` | `nepaliDigits(n: Long, minWidth: Int = 0): String` | `nepaliDigits(2081)` is `२०८१` |

```kotlin
Month.SHRAWAN.displayName(Lang.NEPALI)     // साउन
Weekday.TUESDAY.shortName(Lang.NEPALI)     // मङ्गल
nepaliDigits(5, 2)                          // ०५
```

## 5. Parsing

| C# member | Kotlin | Example |
|---|---|---|
| `static NepaliDate Parse(string rawNepaliDate)` | `NepaliDate.parse(text: CharSequence)` | `NepaliDate.parse("2081/04/15")` |
| `static bool TryParse(string rawNepDate, out NepaliDate result)` | `NepaliDate.parseOrNull(text: CharSequence): NepaliDate?` | `NepaliDate.parseOrNull("2081/04/15")` |
| `static NepaliDate SmartDateParser.Parse(string input)` | `NepaliDate.parseLenient(text: CharSequence)` | `NepaliDate.parseLenient("15 Shrawan 2080")` |
| `static bool SmartDateParser.TryParse(string input, out NepaliDate result)` | `NepaliDate.parseLenientOrNull(text: CharSequence): NepaliDate?` | `NepaliDate.parseLenientOrNull("15 Shrawan 2080")` |
| `sealed class InvalidNepaliDateFormatException : FormatException` | `NepDateException` and its `kind` | `NepaliDate.parse("2081/05")` throws `WRONG_GROUP_COUNT` |

Strict parsing takes exactly year, month and day in that order, in ASCII or Devanagari digits, with
`-`, `/`, `.`, `_`, `\`, space, `|` or `।` between them. Leading, trailing and repeated separators
are ignored; a fourth group, fewer than three groups or any other character fails. A group has at
most four digits, so no input can overflow (PARITY D-05).

Lenient parsing tries the strict form first, then month names (`15 Shrawan 2080`, `Shrawan 15, 2080`,
`१५ साउन २०८०`), the era markers `BS` and `वि.सं.`, and day-month-year order (`15/04/2080`). It
rejects rather than guesses: a two-digit year is `AMBIGUOUS` (PARITY D-06). There is no
`autoAdjust` overload.

```kotlin
NepaliDate.parse("2081/04/15")                 // 2081-04-15
NepaliDate.parse("२०८१-०४-१५")                 // the same date in Devanagari digits
NepaliDate.parseOrNull("2081/04/33")           // null, no exception built
NepaliDate.parseLenient("15 Shrawan 2080")     // 2080-04-15
NepaliDate.parseLenientOrNull("not a date")    // null
```

`NepDateException.kind` is one of `OUT_OF_RANGE`, `INVALID_MONTH`, `INVALID_DAY`,
`INVALID_GREGORIAN`, `INVALID_CHARACTER`, `WRONG_GROUP_COUNT`, `NUMBER_TOO_LONG`, `AMBIGUOUS` or
`UNRECOGNIZED`. `monthLength` is set for `INVALID_DAY` and is 0 otherwise. Check `kind`, not the
message.

## 6. Ranges

```kotlin
public class NepaliDateRange internal constructor(/* first and last serial */) :
    AbstractList<NepaliDate>(), RandomAccess
```

| C# member | Kotlin | Example |
|---|---|---|
| `readonly struct NepaliDateRange` | `NepaliDateRange`, a read-only `List<NepaliDate>` | `NepaliDateRange.ofMonth(2081, 4)` |
| `NepaliDateRange(NepaliDate start, NepaliDate end)` | `NepaliDateRange.of(start: NepaliDate, endInclusive: NepaliDate)` | `NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20))` |
| `NepaliDate Start { get; }` | `start: NepaliDate` | `range.start` |
| `NepaliDate End { get; }` | `endInclusive: NepaliDate` | `range.endInclusive` |
| `int Length { get; }` | `size: Int` | `NepaliDateRange.ofYear(2081).size` is `366` |
| `static NepaliDateRange SingleDay(NepaliDate date)` | `NepaliDateRange.singleDay(date: NepaliDate)` | `NepaliDateRange.singleDay(NepaliDate.of(2081, 4, 15))` |
| `static NepaliDateRange FromDayCount(NepaliDate start, int days)` | `NepaliDateRange.fromDayCount(start: NepaliDate, days: Int)` | `NepaliDateRange.fromDayCount(NepaliDate.of(2081, 4, 15), 5)` |
| `static NepaliDateRange ForMonth(int year, int month)` | `NepaliDateRange.ofMonth(year: Int, month: Int)` | `NepaliDateRange.ofMonth(2081, 4)` |
| `static NepaliDateRange ForFiscalYear(int fiscalYear)` | `FiscalYear.range()` | `FiscalYear(2081).range()` |
| `static NepaliDateRange ForCalendarYear(int year)` | `NepaliDateRange.ofYear(year: Int)` | `NepaliDateRange.ofYear(2081)` |
| `static NepaliDateRange CurrentMonth()` | `NepaliDateRange.currentMonth(zone: ZoneId = KATHMANDU)` | `NepaliDateRange.currentMonth(KATHMANDU)` |
| `static NepaliDateRange CurrentFiscalYear()` | `NepaliDateRange.currentFiscalYear(zone: ZoneId = KATHMANDU)` | `NepaliDateRange.currentFiscalYear(KATHMANDU)` |
| `static NepaliDateRange CurrentCalendarYear()` | `NepaliDateRange.currentCalendarYear(zone: ZoneId = KATHMANDU)` | `NepaliDateRange.currentCalendarYear(KATHMANDU)` |
| `bool Contains(NepaliDate date)` | `contains(element: NepaliDate)` | `NepaliDateRange.ofMonth(2081, 4).contains(NepaliDate.of(2081, 4, 15))` |
| `bool Contains(NepaliDateRange other)` | `containsRange(other: NepaliDateRange): Boolean` | `range.containsRange(NepaliDateRange.singleDay(date))` |
| `bool Overlaps(NepaliDateRange other)` | `overlaps(other: NepaliDateRange): Boolean` | `first.overlaps(second)` |
| `bool IsAdjacentTo(NepaliDateRange other)` | `isAdjacentTo(other: NepaliDateRange): Boolean` | `first.isAdjacentTo(second)` |
| `NepaliDateRange Intersect(NepaliDateRange other)` | `intersect(other: NepaliDateRange): NepaliDateRange?` | `first.intersect(second)` |
| `NepaliDateRange Union(NepaliDateRange other)` | `union(other: NepaliDateRange): NepaliDateRange` | `first.union(second)` |
| `NepaliDateRange[] Except(NepaliDateRange other)` | `minus(other: NepaliDateRange): List<NepaliDateRange>` | `month.minus(week)` |
| `NepaliDateRange[] SplitByMonth()` | `splitByMonth(): List<NepaliDateRange>` | `range.splitByMonth()` |
| `NepaliDateRange[] SplitByFiscalQuarter()` | `splitByFiscalQuarter(): List<NepaliDateRange>` | `range.splitByFiscalQuarter()` |
| `IEnumerable<NepaliDate> DatesWithInterval(int interval)` | `datesWithInterval(interval: Int): List<NepaliDate>` | `range.datesWithInterval(3)` |
| `IEnumerable<NepaliDate> WorkingDays(bool excludeSunday = false)` | `workingDays(excludeSunday: Boolean = false): List<NepaliDate>` | `range.workingDays(excludeSunday = true)` |
| `IEnumerable<NepaliDate> WeekendDays(bool includeSunday = true)` | `weekendDays(includeSunday: Boolean = true): List<NepaliDate>` | `range.weekendDays(includeSunday = false)` |
| `IEnumerator<NepaliDate> GetEnumerator()` | the `List` iterator, and `for (day in range)` | `range.forEach { println(it) }` |
| `override string ToString()` | `toString()` | `"2081/04/10 - 2081/04/20"` |
| `string ToString(DateFormats format, Separators separator = ForwardSlash)` | `toFormattedString(order: DateOrder = YMD, separator: Separator = SLASH, lang: Lang = ENGLISH)` | `range.toFormattedString(DateOrder.DMY, Separator.DASH)` |
| `Equals`, `GetHashCode`, `operator ==`, `operator !=` | `equals`, `hashCode`, `==`, `!=` over the two bounds | `first == second` |

A range is inclusive at both ends and is never empty: `of` refuses a start after the end, so the C#
empty range does not exist here and a disjoint `intersect` returns null (PARITY D-01).
`equals`, `hashCode` and `toString` describe the two bounds, so a range of years prints one line and
hashes in constant time.

Members that reach the data ends never throw (PARITY D-16). The splits return every piece with the
last one ending at the range end, the enumerator and the three day helpers return every date up to
and including 2199-12-30, and `isAdjacentTo` answers `false` when the other range would have to
start after 2199-12-30 or end before 1901-01-01. `datesWithInterval` accepts any positive interval,
including `Int.MAX_VALUE`.

`start..end` is the same factory as `NepaliDateRange.of(start, end)`.

```kotlin
val month = NepaliDateRange.ofMonth(2081, 4)          // 2081-04-01..2081-04-32
val week = NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 16))
month.containsRange(week)                             // true
month.minus(week).size                                // 2
month.splitByMonth().size                             // 1
NepaliDate.of(2081, 4, 1)..NepaliDate.of(2081, 4, 7)  // 2081/04/01 - 2081/04/07
```

## 7. Fiscal year

A fiscal year starts on 1 Shrawan (month 4) and ends on the last day of Ashad (month 3) of the next
BS year. FY 2082/83 is `FiscalYear(2082)`. Quarters are Q1 Shrawan to Ashoj, Q2 Kartik to Poush,
Q3 Magh to Chaitra and Q4 Baishakh to Ashad of the next year.

| C# member | Kotlin | Example |
|---|---|---|
| `enum FiscalYearQuarters` | `Quarter` | `Quarter.Q1` |
| `NepaliDate FiscalYearStartDate(int yearOffset = 0)` | `FiscalYear.containing(date).start()` | `NepaliDate.of(2081, 4, 15).fiscalYear.start()` |
| `NepaliDate FiscalYearEndDate(int yearOffset = 0)` | `FiscalYear.containing(date).end()` | `NepaliDate.of(2081, 4, 15).fiscalYear.end()` |
| `(NepaliDate, NepaliDate) FiscalYearStartAndEndDate(int yearOffset = 0)` | `FiscalYear.containing(date).range()` | `NepaliDate.of(2081, 4, 15).fiscalYear.range()` |
| `NepaliDate FiscalYearQuarterStartDate(FiscalYearQuarters = Current, int = 0)` | `FiscalYear.containing(date).range(q).start` | `fiscalYear.range(Quarter.Q2).start` |
| `NepaliDate FiscalYearQuarterEndDate(FiscalYearQuarters = Current, int = 0)` | `FiscalYear.containing(date).range(q).endInclusive` | `fiscalYear.range(Quarter.Q2).endInclusive` |
| `(NepaliDate, NepaliDate) FiscalYearQuarterStartAndEndDate(...)` | `FiscalYear.containing(date).range(q)` | `fiscalYear.range(Quarter.Q2)` |
| `static NepaliDate GetFiscalYearStartDate(int fiscalYear)` | `FiscalYear(fiscalYear).start()` | `FiscalYear(2081).start()` |
| `static NepaliDate GetFiscalYearEndDate(int fiscalYear)` | `FiscalYear(fiscalYear).end()` | `FiscalYear(2081).end()` |
| `static (NepaliDate, NepaliDate) GetFiscalYearStartAndEndDate(int fiscalYear)` | `FiscalYear(fiscalYear).range()` | `FiscalYear(2081).range()` |

The C# `yearOffset` arguments and the three `GetFiscalYearQuarter*(fiscalYear, month)` helpers are
not ported (PARITY D-08); the fiscal-year value and its quarter range cover them.

```kotlin
val fy = NepaliDate.of(2081, 4, 15).fiscalYear      // FiscalYear(2081)
fy.start()                                          // 2081-04-01
fy.end()                                            // 2082-03-32
fy.range(Quarter.Q4)                                // 2082-01-01..2082-03-32
fy.label()                                          // 2081/82
fy.label(Lang.NEPALI)                               // २०८१/८२
```

`FiscalYear` compares and hashes by its start year, and `Quarter.months` is the quarter's three
months in order.

## 8. Patro

The patro (पात्रो) data is its own artifact, `nepdate-patro`, which depends on `nepdate`. The core
contains none of it (PARITY D-13, hub ADR-0004). The data covers 2077-01-01 to 2084-08-20 BS.

| C# member | Kotlin | Example |
|---|---|---|
| `readonly struct CalendarInfo` | `CalendarInfo` | `Patro.info(NepaliDate.of(2081, 1, 1))` |
| `string TithiNp { get; }` | `info.tithi?.nepali` | `Patro.info(date).tithi?.nepali` |
| `string TithiEn { get; }` | `info.tithi?.english` | `Patro.info(date).tithi?.english` |
| `bool IsPublicHoliday { get; }` | `info.isPublicHoliday` | `Patro.info(date).isPublicHoliday` |
| `string[] EventsNp { get; }` | `info.events.map { it.nepali }` | `Patro.info(date).events.map { it.nepali }` |
| `string[] EventsEn { get; }` | `info.events.map { it.english }` | `Patro.info(date).events.map { it.english }` |
| `CalendarInfo GetCalendarInfo()` | `Patro.info(date)` and the `date.calendarInfo` extension | `date.calendarInfo.tithi?.english` |
| none (Kotlin extra) | `Named.nepali`, `Named.english` and `Named.name(lang: Lang = ENGLISH)` | `info.tithi?.name(Lang.NEPALI)` is `पञ्चमी` |
| none (Kotlin extra) | `Patro.coverage: NepaliDateRange` and `Patro.publicHolidays: List<NepaliDate>` | `Patro.coverage.start` |

A day outside the data range answers with empty values, never null: `tithi` is null, `events` is
empty and `isPublicHoliday` is false.

```kotlin
import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.patro.calendarInfo

val info = Patro.info(NepaliDate.of(2081, 1, 1))
info.tithi?.english                 // Panchami
info.isPublicHoliday                // true
info.events.map { it.english }      // [New Year, Mesh Sankranti, Bisket Jatra]

Patro.coverage                      // 2077-01-01..2084-08-20
Patro.publicHolidays                // every gazetted holiday, ascending
NepaliDate.of(2081, 1, 1).calendarInfo == info
```

`Patro.info` returns a shared immutable value built once per day, so the same day gives the same
instance. `CalendarInfo`, `Named`, `Patro.coverage` and `Patro.publicHolidays` are the rest of the
artifact.

## 9. Errors and conventions

```kotlin
public class NepDateException internal constructor(
    public val kind: Kind,
    public val monthLength: Int,
    message: String,
) : IllegalArgumentException(message) {
    public enum class Kind { /* the nine kinds listed under Parsing */ }
}
```

- `of`, `ofGregorian`, `ofEpochDay`, `fromYmd`, `parse`, `parseLenient`, the arithmetic members and
  the range factories throw `NepDateException`. `ofOrNull`, `parseOrNull` and `parseLenientOrNull`
  answer null and build no exception.
- A model with an invalid date does not exist, so nothing has to be checked for "no date" (PARITY
  D-01).
- `NepaliDate` is a plain class, not a `data class` (no `copy` or `componentN` for the whole value)
  and not a `value class` (Java calls it cleanly).
- `Month.value` is the month number, declared on each constant rather than taken from the
  declaration order, so Java callers and stored values keep their meaning if the constants move.
- `nepaliToday()` with no argument uses Nepal time, never the device zone. Every clock member also
  takes a `java.time.Clock`, which is what makes the clock behaviour testable.
- The `java.time` bridge and every clock member are static methods on `NepaliDateJavaTime` in Java,
  for example `NepaliDateJavaTime.toLocalDate(date)` and
  `NepaliDateJavaTime.currentMonth(NepaliDateRange.Companion, zone)`.
- `nepdate` depends on `kotlin-stdlib` only. `nepdate-serialization` adds `kotlinx-serialization-core`
  and is optional. Nothing in the core refers to the patro or to serialization.
- Allocation contract (ADR-0002 §7, tested): conversion, arithmetic and parsing allocate only the
  returned `NepaliDate`; `Patro.info`, the `append*` functions and `formatTo` into a builder with
  enough capacity allocate nothing; the `String` formatters allocate the result.
