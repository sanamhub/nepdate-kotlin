package io.github.sanamhub.nepdate

import io.github.sanamhub.nepdate.CalendarTables.MONTH_START

/**
 * The days from [start] to [endInclusive], both included, as a read-only list. `size`, [get],
 * [contains], [indexOf] and [lastIndexOf] are O(1): the range holds only its first and last day.
 * Iterating yields every day in order.
 *
 * A range is never empty (PARITY D-01, where the C# `NepaliDateRange` struct can be): the factories
 * refuse a start after the end, and [intersect] returns null instead of an empty range.
 *
 * [equals], [hashCode] and [toString] describe the two bounds rather than every day in the range,
 * so they stay cheap for a range of years. Ranges reached through the data end never throw
 * (PARITY D-16): [splitByMonth], [splitByFiscalQuarter], [datesWithInterval], [workingDays] and
 * [weekendDays] include 2199-12-30, and [isAdjacentTo] answers false rather than looking one day
 * past it.
 */
public class NepaliDateRange internal constructor(
    private val first: Int,
    private val last: Int,
) : AbstractList<NepaliDate>(),
    RandomAccess {
    /** The first day, C# `Start`. */
    public val start: NepaliDate get() = NepaliDate.ofSerial(first)

    /** The last day, included, C# `End`. */
    public val endInclusive: NepaliDate get() = NepaliDate.ofSerial(last)

    /** The number of days, first to last included, C# `Length`. */
    override val size: Int get() = last - first + 1

    /** The day at [index]; throws [IndexOutOfBoundsException] outside `0 until size`. */
    override fun get(index: Int): NepaliDate {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("index $index, size $size")
        return NepaliDate.ofSerial(first + index)
    }

    /**
     * Whether [element] is one of the range's days, C# `Contains(NepaliDate)`.
     *
     * @return true when [element] is between [start] and [endInclusive]
     */
    override fun contains(element: NepaliDate): Boolean = element.serial in first..last

    /**
     * Whether [other] lies entirely inside this range, C# `Contains(NepaliDateRange)`.
     *
     * @return true when every day of [other] is also a day of this range
     */
    public fun containsRange(other: NepaliDateRange): Boolean = other.first >= first && other.last <= last

    /**
     * Whether the two ranges share at least one day, C# `Overlaps`.
     *
     * @return true when the bounds overlap
     */
    public fun overlaps(other: NepaliDateRange): Boolean = first <= other.last && other.first <= last

    /**
     * Whether one range starts the day after the other ends, C# `IsAdjacentTo`.
     *
     * A range that ends at 2199-12-30 has no following day inside the supported range, so this
     * answers false instead of failing (PARITY D-16), and likewise at 1901-01-01 in the other
     * direction.
     *
     * @return true when [other] starts on this range's next day or ends the day before this one
     */
    public fun isAdjacentTo(other: NepaliDateRange): Boolean = last + 1 == other.first || other.last + 1 == first

    /**
     * The days both ranges have, C# `Intersect`.
     *
     * @return the overlapping range, or null when they do not overlap (PARITY D-01)
     */
    public fun intersect(other: NepaliDateRange): NepaliDateRange? {
        val low = if (first > other.first) first else other.first
        val high = if (last < other.last) last else other.last
        return if (low <= high) NepaliDateRange(low, high) else null
    }

    /**
     * The smallest range covering both, C# `Union`.
     *
     * @return this range, [other], or the range that spans both
     */
    public fun union(other: NepaliDateRange): NepaliDateRange =
        NepaliDateRange(if (first < other.first) first else other.first, if (last > other.last) last else other.last)

    /**
     * This range with [other] removed, C# `Except`.
     *
     * @return an empty list when [other] covers this range, otherwise the one or two ranges that
     *   are left, in date order
     */
    public fun minus(other: NepaliDateRange): List<NepaliDateRange> {
        if (!overlaps(other)) return listOf(this)
        val left = if (other.first > first) NepaliDateRange(first, other.first - 1) else null
        val right = if (other.last < last) NepaliDateRange(other.last + 1, last) else null
        return when {
            left == null -> if (right == null) emptyList() else listOf(right)
            right == null -> listOf(left)
            else -> listOf(left, right)
        }
    }

    /**
     * One range per BS month the range touches, C# `SplitByMonth`. The first and last piece are
     * clipped to the range ends, and the last piece ends at [endInclusive] even at 2199-12-30
     * (PARITY D-16).
     *
     * @return 1 to 24 ranges, in date order
     */
    public fun splitByMonth(): List<NepaliDateRange> {
        val pieces = ArrayList<NepaliDateRange>()
        var day = first
        while (day <= last) {
            val date = NepaliDate.ofSerial(day)
            val monthStart = toSerial(date.year, date.monthValue, 1)
            val pieceEnd = minOf(last, monthStart + date.lengthOfMonth() - 1)
            pieces.add(NepaliDateRange(day, pieceEnd))
            day = pieceEnd + 1
        }
        return pieces
    }

    /**
     * One range per fiscal quarter the range touches, C# `SplitByFiscalQuarter`. The first and last
     * piece are clipped to the range ends, and the last piece ends at [endInclusive] even at
     * 2199-12-30 (PARITY D-16).
     *
     * @return 1 to 8 ranges, in date order
     */
    public fun splitByFiscalQuarter(): List<NepaliDateRange> {
        val pieces = ArrayList<NepaliDateRange>()
        var day = first
        while (day <= last) {
            val pieceEnd = minOf(last, quarterEndOf(NepaliDate.ofSerial(day)))
            pieces.add(NepaliDateRange(day, pieceEnd))
            day = pieceEnd + 1
        }
        return pieces
    }

    /**
     * Every [interval]-th day, starting at [start], C# `DatesWithInterval`.
     *
     * @param interval the number of days between two results, at least 1
     * @return the days, in date order, ending at or before [endInclusive]; every positive
     *   [interval] is accepted, including `Int.MAX_VALUE`
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when [interval] is below 1
     */
    @Throws(NepDateException::class)
    public fun datesWithInterval(interval: Int): List<NepaliDate> {
        if (interval < 1) {
            throw NepDateException(NepDateException.Kind.OUT_OF_RANGE, 0, "interval $interval is below 1")
        }
        val step = interval.toLong()
        val dates = ArrayList<NepaliDate>()
        // Accumulated as a Long so a step near Int.MAX_VALUE can't wrap the serial.
        var day = first.toLong()
        while (day <= last) {
            dates.add(NepaliDate.ofSerial(day.toInt()))
            day += step
        }
        return dates
    }

    /**
     * Every day except Saturday, C# `WorkingDays`.
     *
     * @param excludeSunday also skip Sunday
     * @return the working days, in date order
     */
    @JvmOverloads
    public fun workingDays(excludeSunday: Boolean = false): List<NepaliDate> {
        val days = ArrayList<NepaliDate>(size)
        for (day in first..last) {
            val weekday = weekdayOfSerial(day)
            if (weekday == Weekday.SATURDAY || (excludeSunday && weekday == Weekday.SUNDAY)) continue
            days.add(NepaliDate.ofSerial(day))
        }
        return days
    }

    /**
     * Every Saturday, C# `WeekendDays`.
     *
     * @param includeSunday also include Sunday
     * @return the weekend days, in date order
     */
    @JvmOverloads
    public fun weekendDays(includeSunday: Boolean = true): List<NepaliDate> {
        val days = ArrayList<NepaliDate>()
        for (day in first..last) {
            val weekday = weekdayOfSerial(day)
            if (weekday == Weekday.SATURDAY || (includeSunday && weekday == Weekday.SUNDAY)) {
                days.add(NepaliDate.ofSerial(day))
            }
        }
        return days
    }

    /**
     * The two bounds in the default format, C# `ToString()`: `2081/04/10 - 2081/04/20`.
     *
     * Ranges are printed through their bounds, not their days, so a range of years prints in
     * constant time.
     *
     * @return [start] and [endInclusive], separated by `" - "`
     */
    override fun toString(): String =
        appendBounds(StringBuilder(2 * SHORT_CAPACITY + 3), DateOrder.YMD, Separator.SLASH, Lang.ENGLISH).toString()

    /**
     * The two dates in [order] and [separator], C# `ToString(DateFormats, Separators)`: for example
     * `2081-04-10 - 2081-04-20` or `१०/०४/२०८१ - २०/०४/२०८१`.
     *
     * @return the two dates, always zero padded, separated by `" - "`
     */
    @JvmOverloads
    public fun toFormattedString(
        order: DateOrder = DateOrder.YMD,
        separator: Separator = Separator.SLASH,
        lang: Lang = Lang.ENGLISH,
    ): String = appendBounds(StringBuilder(2 * SHORT_CAPACITY + 3), order, separator, lang).toString()

    /** Equal when both ranges have the same first and last day, C# `Equals`. */
    override fun equals(other: Any?): Boolean = other is NepaliDateRange && other.first == first && other.last == last

    /** Equal ranges have the same hash code, from the two bounds only. */
    override fun hashCode(): Int = 31 * first + last

    /**
     * The position of [element] in the range.
     *
     * @return the 0-based index, or -1 when [element] is outside the range
     */
    override fun indexOf(element: NepaliDate): Int = if (contains(element)) element.serial - first else -1

    /**
     * The same as [indexOf]: a range has no duplicates.
     *
     * @return the 0-based index, or -1 when [element] is outside the range
     */
    override fun lastIndexOf(element: NepaliDate): Int = indexOf(element)

    private fun appendBounds(
        out: StringBuilder,
        order: DateOrder,
        separator: Separator,
        lang: Lang,
    ): StringBuilder {
        appendShort(out, NepaliDate.ofSerial(first), order, separator, pad = true, lang = lang)
        out.append(" - ")
        return appendShort(out, NepaliDate.ofSerial(last), order, separator, pad = true, lang = lang)
    }

    /** The serial of the last day of the fiscal quarter [date] falls in. */
    private fun quarterEndOf(date: NepaliDate): Int {
        val quarter = Quarter.ofMonth(date.monthValue)
        val startYear = FiscalYear.containing(date).startYear
        val year = if (quarter == Quarter.Q4) startYear + 1 else startYear
        val firstMonth = quarter.months[0].value
        // The quarter's three months are consecutive, so its end is where the month after them starts.
        return MONTH_START[(year - MIN_YEAR) * 12 + firstMonth - 1 + 3] - 1
    }

    /** Factories. */
    public companion object {
        /**
         * The days from [start] to [endInclusive], both included, C# `NepaliDateRange(start, end)`.
         *
         * @return the range
         * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when [start] is after [endInclusive]
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun of(
            start: NepaliDate,
            endInclusive: NepaliDate,
        ): NepaliDateRange {
            if (start > endInclusive) {
                throw NepDateException(NepDateException.Kind.OUT_OF_RANGE, 0, "range start $start is after its end $endInclusive")
            }
            return NepaliDateRange(start.serial, endInclusive.serial)
        }

        /**
         * The range of one day, C# `SingleDay`.
         *
         * @return the range from [date] to [date]
         */
        @JvmStatic
        public fun singleDay(date: NepaliDate): NepaliDateRange = NepaliDateRange(date.serial, date.serial)

        /**
         * The range of [days] days from [start], C# `FromDayCount`.
         *
         * @param days the number of days, at least 1
         * @return the range from [start] to its `days - 1`th following day
         * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when [days] is below 1
         *   or the last day would be outside BS 1901-01-01..2199-12-30
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun fromDayCount(
            start: NepaliDate,
            days: Int,
        ): NepaliDateRange {
            if (days < 1) {
                throw NepDateException(NepDateException.Kind.OUT_OF_RANGE, 0, "$days days is below 1")
            }
            // Checked as a Long first, so Int.MAX_VALUE days can't wrap the serial.
            val last = start.serial.toLong() + days - 1
            if (last > MAX_SERIAL) {
                throw NepDateException(
                    NepDateException.Kind.OUT_OF_RANGE,
                    0,
                    "$days days from $start ends after ${NepaliDate.MAX}",
                )
            }
            return NepaliDateRange(start.serial, last.toInt())
        }

        /**
         * Every day of BS month [month] of [year], C# `ForMonth`.
         *
         * @return the range, 29..32 days
         * @throws NepDateException as [NepaliDate.of]: [NepDateException.Kind.OUT_OF_RANGE] for the
         *   year, then [NepDateException.Kind.INVALID_MONTH]
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun ofMonth(
            year: Int,
            month: Int,
        ): NepaliDateRange {
            val start = NepaliDate.of(year, month, 1)
            val index = (year - MIN_YEAR) * 12 + month - 1
            return NepaliDateRange(start.serial, MONTH_START[index + 1] - 1)
        }

        /**
         * Every day of BS [year], C# `ForCalendarYear`.
         *
         * @return the range, 364..367 days
         * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] outside 1901..2199
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun ofYear(year: Int): NepaliDateRange {
            if (year < MIN_YEAR || year > MAX_YEAR) {
                throw NepDateException(NepDateException.Kind.OUT_OF_RANGE, 0, "year $year is outside $MIN_YEAR..$MAX_YEAR")
            }
            val index = (year - MIN_YEAR) * 12
            return NepaliDateRange(MONTH_START[index], MONTH_START[index + 12] - 1)
        }
    }
}
