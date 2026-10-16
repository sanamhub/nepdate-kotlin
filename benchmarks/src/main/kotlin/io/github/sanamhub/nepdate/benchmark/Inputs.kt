package io.github.sanamhub.nepdate.benchmark

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro

/**
 * The 64 BENCHMARK section 2 inputs shared by every benchmark class. Date number i is the BS date
 * at serial `s0 + i * ((s1 - s0) / 63)`; the patro inputs move the same way across Patro.coverage.
 * The AD and text inputs are derived from the same dates.
 */
object Inputs {
    const val COUNT = 64

    val s0: Long = NepaliDate.of(2000, 1, 1).toEpochDay()
    val s1: Long = NepaliDate.of(2090, 12, 30).toEpochDay()

    /** BS (year, month, day) of the 64 dates. */
    val bs: Array<IntArray> = buildDates(s0, s1) { intArrayOf(it.year, it.monthValue, it.dayOfMonth) }

    /** AD (year, month, day) of the same 64 dates, for AD to BS. */
    val ad: Array<IntArray> = buildDates(s0, s1) { intArrayOf(it.gregorianYear, it.gregorianMonth, it.gregorianDay) }

    /** The 64 dates themselves. */
    val dates: Array<NepaliDate> = buildDates(s0, s1) { it }

    /** The 64 dates as epoch days, for the `LocalDate.ofEpochDay` floor. */
    val epochDays: LongArray = LongArray(COUNT) { i -> dates[i].toEpochDay() }

    /** The 64 dates as `YYYY/MM/DD` strings, for parsing. */
    val text: Array<String> = dates.map { it.toString() }.toTypedArray()

    /** The 64 dates moved across Patro.coverage, for B7 and B8. */
    val patroDates: Array<NepaliDate> =
        buildDates(Patro.coverage.start.toEpochDay(), Patro.coverage.endInclusive.toEpochDay()) { it }

    private inline fun <reified T> buildDates(
        first: Long,
        last: Long,
        transform: (NepaliDate) -> T,
    ): Array<T> {
        val step = (last - first) / (COUNT - 1)
        return Array(COUNT) { i -> transform(NepaliDate.ofEpochDay(first + i * step)) }
    }
}
