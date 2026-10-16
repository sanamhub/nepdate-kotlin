package io.github.sanamhub.nepdate

import io.github.sanamhub.nepdate.CalendarTables.MONTH_START

/**
 * The days from [start] to [endInclusive], both included, as a read-only list (PARITY D-09).
 * `size`, `get` and `contains` are O(1): the range holds only its first and last day.
 */
public class NepaliDateRange internal constructor(
    private val first: Int,
    private val last: Int,
) : AbstractList<NepaliDate>(),
    RandomAccess {
    /** The first day. */
    public val start: NepaliDate get() = NepaliDate.ofSerial(first)

    /** The last day, included. */
    public val endInclusive: NepaliDate get() = NepaliDate.ofSerial(last)

    /** The number of days, first to last included. */
    override val size: Int get() = last - first + 1

    /** The day at [index]; throws [IndexOutOfBoundsException] outside `0 until size`. */
    override fun get(index: Int): NepaliDate {
        if (index < 0 || index >= size) throw IndexOutOfBoundsException("index $index, size $size")
        return NepaliDate.ofSerial(first + index)
    }

    /**
     * Whether [element] is one of the range's days.
     *
     * @return true when [element] is between [start] and [endInclusive]
     */
    override fun contains(element: NepaliDate): Boolean = element.serial in first..last

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

    /** Factories. */
    public companion object {
        /**
         * The days from [start] to [endInclusive], both included.
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
         * Every day of BS month [month] of [year].
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
         * Every day of BS [year].
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
