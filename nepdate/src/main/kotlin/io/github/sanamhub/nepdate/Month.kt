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
