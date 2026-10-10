package io.github.sanamhub.nepdate

/** The twelve BS months, Baishakh (1) to Chaitra (12). */
public enum class Month {
    /** Month 1, बैशाख. */
    BAISHAKH,

    /** Month 2, जेठ. */
    JESTHA,

    /** Month 3, असार. */
    ASHAD,

    /** Month 4, साउन. */
    SHRAWAN,

    /** Month 5, भदौ. */
    BHADRA,

    /** Month 6, असोज. */
    ASHOJ,

    /** Month 7, कार्तिक. */
    KARTIK,

    /** Month 8, मंसिर. */
    MANGSIR,

    /** Month 9, पुष. */
    POUSH,

    /** Month 10, माघ. */
    MAGH,

    /** Month 11, फागुन. */
    FALGUN,

    /** Month 12, चैत. */
    CHAITRA,
    ;

    /** The month number, 1..12. */
    public val value: Int get() = ordinal + 1

    /**
     * The month's name (FORMATTING section 1).
     *
     * @return `Shrawan`, or `साउन` for [Lang.NEPALI]
     */
    @JvmOverloads
    public fun displayName(lang: Lang = Lang.ENGLISH): String =
        if (lang == Lang.NEPALI) MONTH_NAMES_NE[ordinal] else MONTH_NAMES_EN[ordinal]

    /**
     * The three-letter English abbreviation, with `Asa` for Ashad and `Aso` for Ashoj (PARITY D-04).
     * Nepali has no customary abbreviation, so [Lang.NEPALI] gives the full name.
     *
     * @return `Shr`, or `साउन` for [Lang.NEPALI]
     */
    @JvmOverloads
    public fun shortName(lang: Lang = Lang.ENGLISH): String = if (lang == Lang.NEPALI) MONTH_NAMES_NE[ordinal] else MONTH_SHORT_EN[ordinal]

    /** Factory. */
    public companion object {
        private val BY_INDEX = entries

        /**
         * The month with number [value].
         *
         * @return the month, [BAISHAKH] for 1 to [CHAITRA] for 12
         * @throws NepDateException with [NepDateException.Kind.INVALID_MONTH] when [value] is outside 1..12
         */
        @JvmStatic
        @Throws(NepDateException::class)
        public fun of(value: Int): Month {
            if (value !in 1..12) {
                throw NepDateException(NepDateException.Kind.INVALID_MONTH, 0, "month $value is outside 1..12")
            }
            return BY_INDEX[value - 1]
        }
    }
}
