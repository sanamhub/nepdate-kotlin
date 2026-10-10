package io.github.sanamhub.nepdate

/** Days of the week, Sunday first as on Nepali calendars (ALGORITHM section 7: 0 = Sunday). */
public enum class Weekday {
    /** आइतवार. */
    SUNDAY,

    /** सोमवार. */
    MONDAY,

    /** मङ्गलवार. */
    TUESDAY,

    /** बुधवार. */
    WEDNESDAY,

    /** बिहिवार. */
    THURSDAY,

    /** शुक्रवार. */
    FRIDAY,

    /** शनिवार, the weekly holiday in Nepal. */
    SATURDAY,
    ;

    /**
     * The weekday's name (FORMATTING section 1).
     *
     * @return `Tuesday`, or `मङ्गलवार` for [Lang.NEPALI]
     */
    @JvmOverloads
    public fun displayName(lang: Lang = Lang.ENGLISH): String =
        if (lang == Lang.NEPALI) WEEKDAY_NAMES_NE[ordinal] else WEEKDAY_NAMES_EN[ordinal]

    /**
     * The short name for calendar grid headers.
     *
     * @return `Tue`, or `मङ्गल` for [Lang.NEPALI]
     */
    @JvmOverloads
    public fun shortName(lang: Lang = Lang.ENGLISH): String =
        if (lang == Lang.NEPALI) WEEKDAY_SHORT_NE[ordinal] else WEEKDAY_SHORT_EN[ordinal]
}
