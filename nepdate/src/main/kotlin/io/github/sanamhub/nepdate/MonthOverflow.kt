package io.github.sanamhub.nepdate

/**
 * What [NepaliDate.plusMonths] does when the day doesn't exist in the target month
 * (ALGORITHM section 8), for example 2081-04-32 plus 2 months, where 2081-06 has 30 days.
 */
public enum class MonthOverflow {
    /** Use the last day of the target month: 2081-06-30. C# `AddMonths(n)`. */
    CLAMP,

    /** Carry the extra days into the next month: 2081-07-02. C# `AddMonths(n, awayFromMonthEnd: true)`. */
    SPILL,
}
