package io.github.sanamhub.nepdate

/**
 * Thrown for a date outside BS 1901-01-01 to 2199-12-30, an invalid date, or text that doesn't
 * parse. It extends [IllegalArgumentException], which `java.time.LocalDate.of` callers already
 * catch. Check [kind], not the message: messages are for people and may change.
 */
public class NepDateException internal constructor(
    /** What went wrong. */
    public val kind: Kind,
    /** The length of the month for [Kind.INVALID_DAY], 0 for every other kind. */
    public val monthLength: Int,
    message: String,
) : IllegalArgumentException(message) {
    /** The kinds of error, in the order of `shared/spec/ALGORITHM.md` section 3 and `PARSING.md`. */
    public enum class Kind {
        /** The year, AD date or serial is outside the supported range. */
        OUT_OF_RANGE,

        /** The month is outside 1..12. */
        INVALID_MONTH,

        /** The day is outside 1 to the length of the month. */
        INVALID_DAY,

        /** The AD date doesn't exist in the Gregorian calendar. */
        INVALID_GREGORIAN,

        /** Parsing: a character that is neither a digit nor a separator. */
        INVALID_CHARACTER,

        /** Parsing: not exactly three groups of digits. */
        WRONG_GROUP_COUNT,

        /** Parsing: a group with more than four digits. */
        NUMBER_TOO_LONG,

        /** Lenient parsing: the year can't be told from the other numbers. */
        AMBIGUOUS,

        /** Lenient parsing: the text matches no accepted form. */
        UNRECOGNIZED,
    }
}
