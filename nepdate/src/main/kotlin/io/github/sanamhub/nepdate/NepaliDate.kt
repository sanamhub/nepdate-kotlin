package io.github.sanamhub.nepdate

import io.github.sanamhub.nepdate.CalendarTables.MONTH_START

/**
 * A date in the Bikram Sambat (BS) calendar, from 1901-01-01 to 2199-12-30 (AD 1844-04-11 to
 * 2143-04-15). Immutable and safe to share across threads. Create one with [of], [ofGregorian] or
 * [ofEpochDay]; an invalid date can't be constructed (PARITY D-01).
 */
public class NepaliDate private constructor(
    // Days since BS 1901-01-01 (ALGORITHM section 4), and the BS date packed as
    // year shl 10 or month shl 6 or day so the fields are a shift away (ADR-0002 section 1, see pack).
    internal val serial: Int,
    private val ymd: Int,
) : Comparable<NepaliDate> {
    /** The BS year, 1901..2199. */
    public val year: Int get() = packedYear(ymd)

    /** The BS month number, 1..12. */
    public val monthValue: Int get() = packedMonth(ymd)

    /** The BS month. */
    public val month: Month get() = MONTHS[packedMonth(ymd) - 1]

    /** The day of the month, 1..32. */
    public val dayOfMonth: Int get() = packedDay(ymd)

    /** The weekday (ALGORITHM section 7). */
    public val dayOfWeek: Weekday get() = WEEKDAYS[(serial + EPOCH_WEEKDAY) % 7]

    /** The day of the BS year, 1..367. */
    public val dayOfYear: Int get() = serial - MONTH_START[(packedYear(ymd) - MIN_YEAR) * 12] + 1

    /**
     * The length of this date's month.
     *
     * @return 29..32
     */
    public fun lengthOfMonth(): Int = bsMonthLength(packedYear(ymd), packedMonth(ymd))

    /**
     * Day 1 of this date's month.
     *
     * @return the first day of the month
     */
    public fun firstDayOfMonth(): NepaliDate {
        val y = packedYear(ymd)
        val m = packedMonth(ymd)
        return NepaliDate(toSerial(y, m, 1), pack(y, m, 1))
    }

    /**
     * The last day of this date's month.
     *
     * @return the last day of the month
     */
    public fun lastDayOfMonth(): NepaliDate {
        val y = packedYear(ymd)
        val m = packedMonth(ymd)
        val length = bsMonthLength(y, m)
        return NepaliDate(toSerial(y, m, length), pack(y, m, length))
    }

    /** The AD (Gregorian) year. */
    public val gregorianYear: Int get() = packedYear(packedAdOf(serial))

    /** The AD (Gregorian) month, 1..12. */
    public val gregorianMonth: Int get() = packedMonth(packedAdOf(serial))

    /** The AD (Gregorian) day of the month. */
    public val gregorianDay: Int get() = packedDay(packedAdOf(serial))

    /**
     * Days since AD 1970-01-01, the epoch of `java.time.LocalDate.toEpochDay`, so code without
     * `java.time` (Android below API 26) can convert.
     *
     * @return -45920 for [MIN] to 63291 for [MAX]
     */
    public fun toEpochDay(): Long = (serial + EPOCH_UNIX_DAYS).toLong()

    /**
     * The date [days] days later, or earlier for a negative [days] (ALGORITHM section 8).
     *
     * @return the new date
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when the result is outside
     *   BS 1901-01-01..2199-12-30
     */
    @Throws(NepDateException::class)
    public fun plusDays(days: Long): NepaliDate {
        // Checked as a Long first, so a huge argument can't overflow the Int addition.
        if (days < -MAX_SERIAL || days > MAX_SERIAL) throw daysOutOfRange(days)
        val result = serial + days.toInt()
        if (result < 0 || result > MAX_SERIAL) throw daysOutOfRange(days)
        return ofSerial(result)
    }

    /**
     * The date [days] days earlier.
     *
     * @return the new date
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when the result is outside
     *   the supported range
     */
    @Throws(NepDateException::class)
    public fun minusDays(days: Long): NepaliDate {
        // -Long.MIN_VALUE overflows back to itself.
        if (days == Long.MIN_VALUE) throw daysOutOfRange(days)
        return plusDays(-days)
    }

    /**
     * Days from this date to [other]: positive when [other] is later (PARITY D-07).
     *
     * @return the signed number of days, -109211..109211
     */
    public fun daysUntil(other: NepaliDate): Long = (other.serial - serial).toLong()

    /**
     * The same day [months] months later, or earlier for a negative [months] (ALGORITHM section 8).
     * When that day doesn't exist in the target month, [overflow] decides.
     *
     * @return the new date
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when the result is outside
     *   the supported range
     */
    @JvmOverloads
    @Throws(NepDateException::class)
    public fun plusMonths(
        months: Long,
        overflow: MonthOverflow = MonthOverflow.CLAMP,
    ): NepaliDate {
        val serial = monthsLaterSerial(months, overflow)
        if (serial < 0) throw outOfRange("$months months")
        return ofSerial(serial)
    }

    /** The serial [months] months later, or -1 when it is outside the supported range. */
    private fun monthsLaterSerial(
        months: Long,
        overflow: MonthOverflow,
    ): Int {
        // months is range-checked before it is narrowed, so the Int addition can't overflow.
        val target =
            if (months < -CalendarTables.MONTHS || months > CalendarTables.MONTHS) {
                -1
            } else {
                (packedYear(ymd) - MIN_YEAR) * 12 + packedMonth(ymd) - 1 + months.toInt()
            }
        if (target < 0 || target >= CalendarTables.MONTHS) return -1
        val day = packedDay(ymd)
        val length = MONTH_START[target + 1] - MONTH_START[target]
        return when {
            day <= length -> MONTH_START[target] + day - 1
            overflow == MonthOverflow.CLAMP -> MONTH_START[target] + length - 1
            // Day d of a month too short for it is day (d - length) of the next month, which
            // starts at MONTH_START[target + 1].
            target + 1 < CalendarTables.MONTHS -> MONTH_START[target + 1] + day - length - 1
            else -> -1
        }
    }

    /**
     * The same day [years] years later: [plusMonths] with `12 * years`.
     *
     * @return the new date
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when the result is outside
     *   the supported range
     */
    @JvmOverloads
    @Throws(NepDateException::class)
    public fun plusYears(
        years: Long,
        overflow: MonthOverflow = MonthOverflow.CLAMP,
    ): NepaliDate {
        // Checked before multiplying so 12 * years can't overflow.
        if (years < -YEARS || years > YEARS) throw outOfRange("$years years")
        return plusMonths(years * 12, overflow)
    }

    /**
     * The calendar distance to [other] in years, months and days (ALGORITHM section 8a): the most
     * whole months that, added with [MonthOverflow.CLAMP], don't pass the later date, then the days left.
     *
     * @return the distance, with [DateDiff.isNegative] set when [other] is earlier
     */
    public fun until(other: NepaliDate): DateDiff {
        val negative = other.serial < serial
        val from = if (negative) other else this
        val to = if (negative) this else other
        // The month difference is right, or one too many when the day of the month runs past
        // the later date; so step down at most once, never loop.
        var months =
            (packedYear(to.ymd) - packedYear(from.ymd)) * 12 + packedMonth(to.ymd) - packedMonth(from.ymd)
        var landed = from.monthsLaterSerial(months.toLong(), MonthOverflow.CLAMP)
        if (landed > to.serial) {
            months--
            landed = from.monthsLaterSerial(months.toLong(), MonthOverflow.CLAMP)
        }
        return DateDiff(negative, months / 12, months % 12, to.serial - landed, daysUntil(other))
    }

    private fun outOfRange(amount: String) =
        NepDateException(NepDateException.Kind.OUT_OF_RANGE, 0, "$this plus $amount is outside $MIN..$MAX")

    private fun daysOutOfRange(days: Long) =
        NepDateException(NepDateException.Kind.OUT_OF_RANGE, 0, "$this plus $days days is outside $MIN..$MAX")

    /** Equal when both are the same day. */
    override fun equals(other: Any?): Boolean = other is NepaliDate && other.serial == serial

    override fun hashCode(): Int = serial

    /** Orders by day: negative when this date is earlier than [other]. */
    override fun compareTo(other: NepaliDate): Int = serial.compareTo(other.serial)

    /** The date as `YYYY/MM/DD` (FORMATTING section 2), for example `2081/04/15`. */
    override fun toString(): String {
        val out = StringBuilder(10)
        appendPadded(out, year, 4)
        out.append('/')
        appendPadded(out, monthValue, 2)
        out.append('/')
        appendPadded(out, dayOfMonth, 2)
        return out.toString()
    }

    /** Constants and factories. */
    public companion object {
        private val MONTHS = Month.entries

        /** The first supported date, BS 1901-01-01. */
        @JvmField
        public val MIN: NepaliDate = NepaliDate(0, pack(MIN_YEAR, 1, 1))

        /** The last supported date, BS 2199-12-30. */
        @JvmField
        public val MAX: NepaliDate = NepaliDate(MAX_SERIAL, pack(MAX_YEAR, 12, 30))

        /**
         * The BS date [year]-[month]-[day].
         *
         * @return the date
         * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] for a year outside
         *   1901..2199, then [NepDateException.Kind.INVALID_MONTH], then
         *   [NepDateException.Kind.INVALID_DAY] with [NepDateException.monthLength] set
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun of(
            year: Int,
            month: Int,
            day: Int,
        ): NepaliDate {
            val kind = bsErrorKind(year, month, day)
            if (kind != null) throw bsError(kind, year, month, day)
            return NepaliDate(toSerial(year, month, day), pack(year, month, day))
        }

        /**
         * The BS date [year]-[month]-[day].
         *
         * @return the date
         * @throws NepDateException as [of] with a month number
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun of(
            year: Int,
            month: Month,
            day: Int,
        ): NepaliDate = of(year, month.value, day)

        /**
         * The BS date [year]-[month]-[day], or null where [of] would throw. Builds no exception.
         *
         * @return the date, or null when it is invalid or out of range
         */
        @JvmStatic
        public fun ofOrNull(
            year: Int,
            month: Int,
            day: Int,
        ): NepaliDate? {
            if (bsErrorKind(year, month, day) != null) return null
            return NepaliDate(toSerial(year, month, day), pack(year, month, day))
        }

        /**
         * The BS date of the AD (Gregorian) date [year]-[month]-[day].
         *
         * @return the date
         * @throws NepDateException with [NepDateException.Kind.INVALID_GREGORIAN] when the AD date
         *   doesn't exist (month outside 1..12, or day outside the month), else
         *   [NepDateException.Kind.OUT_OF_RANGE] outside AD 1844-04-11..2143-04-15
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun ofGregorian(
            year: Int,
            month: Int,
            day: Int,
        ): NepaliDate {
            val kind = adErrorKind(year, month, day)
            if (kind != null) throw adError(kind, year, month, day)
            return ofSerial(serialOfAd(year, month, day))
        }

        /**
         * The date [epochDay] days after AD 1970-01-01, as `java.time.LocalDate.ofEpochDay`.
         *
         * @return the date
         * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] outside -45920..63291
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun ofEpochDay(epochDay: Long): NepaliDate {
            // Checked as a Long before narrowing, so huge values can't wrap into the range.
            if (epochDay < EPOCH_UNIX_DAYS || epochDay > MAX_SERIAL + EPOCH_UNIX_DAYS) {
                throw NepDateException(
                    NepDateException.Kind.OUT_OF_RANGE,
                    0,
                    "epoch day $epochDay is outside $EPOCH_UNIX_DAYS..${MAX_SERIAL + EPOCH_UNIX_DAYS}",
                )
            }
            return ofSerial((epochDay - EPOCH_UNIX_DAYS).toInt())
        }

        private val WEEKDAYS = Weekday.entries

        internal fun ofSerial(serial: Int): NepaliDate = NepaliDate(serial, packedBsOf(serial))
    }
}

/** The first failing check of ALGORITHM section 3, or null for a valid BS date. */
internal fun bsErrorKind(
    year: Int,
    month: Int,
    day: Int,
): NepDateException.Kind? =
    when {
        year < MIN_YEAR || year > MAX_YEAR -> NepDateException.Kind.OUT_OF_RANGE
        month < 1 || month > 12 -> NepDateException.Kind.INVALID_MONTH
        day < 1 || day > bsMonthLength(year, month) -> NepDateException.Kind.INVALID_DAY
        else -> null
    }

internal fun bsMonthLength(
    year: Int,
    month: Int,
): Int {
    val index = (year - MIN_YEAR) * 12 + month - 1
    return MONTH_START[index + 1] - MONTH_START[index]
}

private fun bsError(
    kind: NepDateException.Kind,
    year: Int,
    month: Int,
    day: Int,
): NepDateException =
    when (kind) {
        NepDateException.Kind.OUT_OF_RANGE ->
            NepDateException(kind, 0, "year $year is outside $MIN_YEAR..$MAX_YEAR")
        NepDateException.Kind.INVALID_MONTH ->
            NepDateException(kind, 0, "month $month is outside 1..12")
        else -> {
            val length = bsMonthLength(year, month)
            NepDateException(kind, length, "day $day is outside 1..$length for $year-${pad2(month)}")
        }
    }

private const val FIRST_AD_YEAR = 1844
private const val LAST_AD_YEAR = 2143

/** INVALID_GREGORIAN for a date that doesn't exist, else OUT_OF_RANGE outside the range, else null. */
private fun adErrorKind(
    year: Int,
    month: Int,
    day: Int,
): NepDateException.Kind? =
    when {
        month < 1 || month > 12 -> NepDateException.Kind.INVALID_GREGORIAN
        day < 1 || day > gregorianMonthLength(year, month) -> NepDateException.Kind.INVALID_GREGORIAN
        year < FIRST_AD_YEAR || year > LAST_AD_YEAR -> NepDateException.Kind.OUT_OF_RANGE
        serialOfAd(year, month, day) !in 0..MAX_SERIAL -> NepDateException.Kind.OUT_OF_RANGE
        else -> null
    }

private fun adError(
    kind: NepDateException.Kind,
    year: Int,
    month: Int,
    day: Int,
): NepDateException =
    if (kind == NepDateException.Kind.INVALID_GREGORIAN) {
        NepDateException(kind, 0, "AD $year-$month-$day is not a date in the Gregorian calendar")
    } else {
        NepDateException(kind, 0, "AD $year-$month-$day is outside 1844-04-11..2143-04-15")
    }

private fun gregorianMonthLength(
    year: Int,
    month: Int,
): Int {
    val base = if (isGregorianLeap(year)) CalendarTables.LEAP_BASE else 0
    return CalendarTables.CUM[base + month] - CalendarTables.CUM[base + month - 1]
}

private fun pad2(value: Int): String = if (value < 10) "0$value" else value.toString()

internal fun appendPadded(
    out: StringBuilder,
    value: Int,
    width: Int,
) {
    var digits = 1
    var rest = value / 10
    while (rest > 0) {
        digits++
        rest /= 10
    }
    repeat(width - digits) { out.append('0') }
    out.append(value)
}
