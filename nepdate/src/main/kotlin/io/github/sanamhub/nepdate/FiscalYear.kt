package io.github.sanamhub.nepdate

/**
 * A Nepali fiscal year, 1 Shrawan to the end of Ashad of the next BS year (ALGORITHM section 8b,
 * PARITY D-08). FY 2082/83 is `FiscalYear(2082)`. Any [startYear] is accepted; only a method whose
 * result falls outside BS 1901..2199 throws.
 */
public class FiscalYear(
    /** The BS year the fiscal year starts in. */
    public val startYear: Int,
) : Comparable<FiscalYear> {
    /**
     * 1 Shrawan of [startYear].
     *
     * @return the first day
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] outside the supported range
     */
    @Throws(NepDateException::class)
    public fun start(): NepaliDate = NepaliDate.of(startYear, 4, 1)

    /**
     * The last day of Ashad of the next BS year.
     *
     * @return the last day
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] outside the supported range
     */
    @Throws(NepDateException::class)
    public fun end(): NepaliDate = lastDayOf(startYear.toLong() + 1, 3)

    /**
     * Every day of the fiscal year.
     *
     * @return the range from [start] to [end]
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] when either end is outside
     *   the supported range
     */
    @Throws(NepDateException::class)
    public fun range(): NepaliDateRange = NepaliDateRange.of(start(), end())

    /**
     * Every day of [quarter]. Works when only part of the fiscal year is supported, for example
     * `FiscalYear(1900).range(Quarter.Q4)`.
     *
     * @return the range of the quarter's three months
     * @throws NepDateException with [NepDateException.Kind.OUT_OF_RANGE] outside the supported range
     */
    @Throws(NepDateException::class)
    public fun range(quarter: Quarter): NepaliDateRange {
        val first = quarter.months[0].value
        val year = if (quarter == Quarter.Q4) startYear.toLong() + 1 else startYear.toLong()
        return NepaliDateRange.of(firstDayOf(year, first), lastDayOf(year, first + 2))
    }

    /**
     * The label `2082/83`: the start year, a slash and the last two digits of the next year.
     *
     * @return the label in ASCII digits, or Devanagari digits for [Lang.NEPALI]
     */
    @JvmOverloads
    public fun label(lang: Lang = Lang.ENGLISH): String {
        val out = StringBuilder(8)
        out.append(startYear).append('/')
        // Not Math.floorMod: Android has it only from API 24.
        appendNumber(out, ((startYear.toLong() + 1) % 100 + 100) % 100, 2, Lang.ENGLISH)
        if (lang == Lang.NEPALI) {
            for (i in out.indices) {
                val c = out[i]
                if (c in '0'..'9') out.setCharAt(i, DEVANAGARI_ZERO + (c - '0'))
            }
        }
        return out.toString()
    }

    /** Orders by [startYear]. */
    override fun compareTo(other: FiscalYear): Int = startYear.compareTo(other.startYear)

    /** Equal when both start in the same BS year. */
    override fun equals(other: Any?): Boolean = other is FiscalYear && other.startYear == startYear

    /** The hash code of [startYear]. */
    override fun hashCode(): Int = startYear

    /** The same as [label] in English, for example `2082/83`. */
    override fun toString(): String = label()

    /** Factory. */
    public companion object {
        /**
         * The fiscal year [date] falls in: its BS year from Shrawan on, the year before until Ashad.
         *
         * @return the fiscal year
         */
        @JvmStatic
        public fun containing(date: NepaliDate): FiscalYear = FiscalYear(if (date.monthValue >= 4) date.year else date.year - 1)

        private fun firstDayOf(
            year: Long,
            month: Int,
        ): NepaliDate = NepaliDate.of(checkedYear(year), month, 1)

        private fun lastDayOf(
            year: Long,
            month: Int,
        ): NepaliDate = NepaliDate.of(checkedYear(year), month, 1).lastDayOfMonth()

        // startYear + 1 is computed as a Long, so Int.MAX_VALUE can't wrap into the range.
        private fun checkedYear(year: Long): Int = if (year > MAX_YEAR) MAX_YEAR + 1 else year.toInt()
    }
}
