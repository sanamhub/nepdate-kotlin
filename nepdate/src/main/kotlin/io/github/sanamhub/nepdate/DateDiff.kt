package io.github.sanamhub.nepdate

/**
 * The calendar distance between two dates as years, months and days (ALGORITHM section 8a), from
 * [NepaliDate.until]. [years], [months] and [days] are magnitudes; [isNegative] gives the direction.
 */
public class DateDiff internal constructor(
    /** True when the second date is before the first. */
    public val isNegative: Boolean,
    /** Whole years, never negative. */
    public val years: Int,
    /** Whole months after the years, 0..11. */
    public val months: Int,
    /** Days after the months, 0..32. */
    public val days: Int,
    /** The signed day count, the same as [NepaliDate.daysUntil]. */
    public val totalDays: Long,
) {
    /** Equal when [isNegative], [years], [months], [days] and [totalDays] all match. */
    override fun equals(other: Any?): Boolean =
        other is DateDiff &&
            other.isNegative == isNegative &&
            other.years == years &&
            other.months == months &&
            other.days == days &&
            other.totalDays == totalDays

    /** Equal distances have the same hash code. */
    override fun hashCode(): Int {
        // Written out: Boolean.hashCode() and Long.hashCode() compile to static methods Android
        // only has from API 24 (animalsniffer, ADR-0004 gate 8).
        var result = if (isNegative) 1 else 0
        result = 31 * result + years
        result = 31 * result + months
        result = 31 * result + days
        result = 31 * result + (totalDays xor (totalDays ushr 32)).toInt()
        return result
    }

    /** For debugging only; the format may change. */
    override fun toString(): String = "DateDiff(${if (isNegative) "-" else ""}${years}y ${months}m ${days}d, totalDays=$totalDays)"
}
