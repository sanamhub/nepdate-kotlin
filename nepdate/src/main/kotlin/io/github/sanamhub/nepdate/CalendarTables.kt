package io.github.sanamhub.nepdate

/**
 * The core tables of ADR-0002 section 2, built together on first use. JVM class initialisation is
 * lazy and thread-safe (JLS 12.4.2), so no lock is needed. The large tables are decoded from a
 * string in loops instead of written as array literals, which would cost one store instruction per
 * element in the class initialiser (ADR-0002 section 4).
 */
internal object CalendarTables {
    /** Number of BS months from 1901-01 to 2199-12. */
    const val MONTHS: Int = (MAX_YEAR - MIN_YEAR + 1) * 12

    /** First AD year in [JAN1]. */
    const val JAN1_BASE_YEAR: Int = 1844

    /** Index of the leap-year half of [CUM]. */
    const val LEAP_BASE: Int = 13

    /** Serial of day 1 of BS month index `(year - 1901) * 12 + month - 1`; the last entry is 109212. */
    @JvmField
    val MONTH_START: IntArray = IntArray(MONTHS + 1)

    /** Serial of 1 January of AD `1844 + i`. Entry 0 (AD 1844) is -101, before the epoch. */
    @JvmField
    val JAN1: IntArray = IntArray(302)

    /** Days before each AD month: common year in 0..12, leap year in 13..25. */
    @JvmField
    @Suppress("ktlint:standard:argument-list-wrapping")
    val CUM: IntArray =
        intArrayOf(
            0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334, 365,
            0, 31, 60, 91, 121, 152, 182, 213, 244, 274, 305, 335, 366,
        )

    init {
        var serial = 0
        for (i in 0 until MONTHS) {
            serial += 29 + (MONTH_LENGTHS_DATA[i] - '0')
            MONTH_START[i + 1] = serial
        }
        var jan1 = -101
        for (i in JAN1.indices) {
            JAN1[i] = jan1
            jan1 += if (isGregorianLeap(JAN1_BASE_YEAR + i)) 366 else 365
        }
    }
}

internal fun isGregorianLeap(year: Int): Boolean = (year % 4 == 0 && year % 100 != 0) || year % 400 == 0
