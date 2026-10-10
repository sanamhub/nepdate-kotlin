package io.github.sanamhub.nepdate

// PARSING sections 1 to 3. The scanners read the CharSequence by index and return the numbers
// they found packed into a Long (year shl 28 or month shl 14 or day; each group has at most four
// digits, so each fits in 14 bits), or a negative value -(kind.ordinal + 1). Nothing is allocated
// until the caller builds the date or, on failure, the exception.

private const val YEAR_SHIFT = 28
private const val MONTH_SHIFT = 14
private const val FIELD_MASK = 0x3FFFL
internal const val MAX_DIGITS: Int = 4

internal fun scanned(
    year: Int,
    month: Int,
    day: Int,
): Long = (year.toLong() shl YEAR_SHIFT) or (month.toLong() shl MONTH_SHIFT) or day.toLong()

internal fun scannedYear(value: Long): Int = (value ushr YEAR_SHIFT).toInt()

internal fun scannedMonth(value: Long): Int = ((value ushr MONTH_SHIFT) and FIELD_MASK).toInt()

internal fun scannedDay(value: Long): Int = (value and FIELD_MASK).toInt()

internal fun scanError(kind: NepDateException.Kind): Long = -(kind.ordinal + 1).toLong()

internal fun scanErrorKind(value: Long): NepDateException.Kind = NepDateException.Kind.entries[(-value - 1).toInt()]

/** 0..9 for an ASCII or Devanagari digit (PARITY D-05), else -1. */
internal fun digitValue(c: Char): Int =
    when (c) {
        in '0'..'9' -> c - '0'
        in '०'..'९' -> c - '०'
        else -> -1
    }

/** The strict separators: `- / . _ \ space । |` (PARSING section 1). */
private fun isStrictSeparator(c: Char): Boolean =
    c == '-' || c == '/' || c == '.' || c == '_' || c == '\\' || c == ' ' || c == '।' || c == '|'

/** The lenient separators: the strict ones without `.`, plus `,` (PARSING section 2). */
internal fun isLenientSeparator(c: Char): Boolean = c != '.' && (isStrictSeparator(c) || c == ',')

// One pass mirroring PARSING section 1 step by step; smaller functions would hide the order of the checks.

/** PARSING section 1 steps 1 to 3: three groups of digits, before validation. */
@Suppress("CyclomaticComplexMethod", "ReturnCount")
internal fun scanStrict(text: CharSequence): Long {
    var groups = 0
    var current = 0
    var length = 0
    var year = 0
    var month = 0
    var day = 0
    for (i in 0 until text.length) {
        val c = text[i]
        val digit = digitValue(c)
        when {
            digit >= 0 -> {
                if (length == MAX_DIGITS) return scanError(NepDateException.Kind.NUMBER_TOO_LONG)
                current = current * 10 + digit
                length++
            }
            isStrictSeparator(c) ->
                if (length > 0) {
                    if (groups == 0) year = current
                    if (groups == 1) month = current
                    if (groups == 2) day = current
                    groups++
                    current = 0
                    length = 0
                }
            else -> return scanError(NepDateException.Kind.INVALID_CHARACTER)
        }
        // A digit after three complete groups starts a fourth.
        if (groups == 3 && length > 0) return scanError(NepDateException.Kind.WRONG_GROUP_COUNT)
    }
    // The day is the group still open at the end, or a third group a trailing separator closed.
    return when {
        groups == 2 && length > 0 -> scanned(year, month, current)
        groups == 3 -> scanned(year, month, day)
        else -> scanError(NepDateException.Kind.WRONG_GROUP_COUNT)
    }
}

/** [scanStrict] then validation: the scanned date when it is valid, else a negative kind. */
internal fun scanStrictValid(text: CharSequence): Long {
    val value = scanStrict(text)
    if (value < 0) return value
    val kind = bsErrorKind(scannedYear(value), scannedMonth(value), scannedDay(value))
    return if (kind == null) value else scanError(kind)
}
