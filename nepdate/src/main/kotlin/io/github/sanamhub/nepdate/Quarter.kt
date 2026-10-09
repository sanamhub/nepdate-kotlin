package io.github.sanamhub.nepdate

/** The quarters of Nepal's fiscal year, which starts on 1 Shrawan (ALGORITHM section 8b). */
public enum class Quarter(
    first: Month,
    second: Month,
    third: Month,
) {
    /** Shrawan, Bhadra, Ashoj. */
    Q1(Month.SHRAWAN, Month.BHADRA, Month.ASHOJ),

    /** Kartik, Mangsir, Poush. */
    Q2(Month.KARTIK, Month.MANGSIR, Month.POUSH),

    /** Magh, Falgun, Chaitra. */
    Q3(Month.MAGH, Month.FALGUN, Month.CHAITRA),

    /** Baishakh, Jestha, Ashad, in the next BS year. */
    Q4(Month.BAISHAKH, Month.JESTHA, Month.ASHAD),
    ;

    /** The three months of the quarter in order; unmodifiable and shared. */
    public val months: List<Month> = ThreeMonths(first, second, third)

    internal companion object {
        private val BY_INDEX = entries

        /** The quarter of BS month [month] (1..12). */
        fun ofMonth(month: Int): Quarter = BY_INDEX[((month + 8) % 12) / 3]
    }
}

// A read-only list: listOf() would return a java.util.Arrays list, whose set() Java callers can use.
private class ThreeMonths(
    private val first: Month,
    private val second: Month,
    private val third: Month,
) : AbstractList<Month>() {
    override val size: Int get() = 3

    override fun get(index: Int): Month =
        when (index) {
            0 -> first
            1 -> second
            2 -> third
            else -> throw IndexOutOfBoundsException("index $index, size 3")
        }
}
