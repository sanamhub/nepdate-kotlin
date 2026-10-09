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
