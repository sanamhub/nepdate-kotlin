package io.github.sanamhub.nepdate

/** The order of year, month and day in [NepaliDate.toShortString] (FORMATTING section 3). */
public enum class DateOrder {
    /** Year, month, day: `2080/05/15`. */
    YMD,

    /** Year, day, month: `2080/15/05`. */
    YDM,

    /** Month, year, day: `05/2080/15`. */
    MYD,

    /** Month, day, year: `05/15/2080`. */
    MDY,

    /** Day, year, month: `15/2080/05`. */
    DYM,

    /** Day, month, year: `15/05/2080`. */
    DMY,
}

/** The character between the numbers in [NepaliDate.toShortString] (FORMATTING section 3). */
public enum class Separator(
    /** The character written. */
    public val char: Char,
) {
    /** `/`. */
    SLASH('/'),

    /** `\`. */
    BACKSLASH('\\'),

    /** `.`. */
    DOT('.'),

    /** `_`. */
    UNDERSCORE('_'),

    /** `-`. */
    DASH('-'),

    /** A space. */
    SPACE(' '),
}
