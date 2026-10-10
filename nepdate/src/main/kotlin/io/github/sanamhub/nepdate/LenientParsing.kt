package io.github.sanamhub.nepdate

// PARSING sections 2 and 3, on top of the strict scanner in Parsing.kt.

// PARSING section 2 step 2. nextToken returns one token at a time as a Long, so scanLenient keeps
// its counts in local variables and allocates nothing: bits 0..30 the start index, bits 31..61 the
// end index, bit 62 set for a number. -1 means no more tokens.
private const val END_SHIFT = 31
private const val NUMBER_FLAG = 1L shl 62
private const val INDEX_MASK = 0x7FFFFFFFL

private fun token(
    start: Int,
    end: Int,
    number: Boolean,
): Long = start.toLong() or (end.toLong() shl END_SHIFT) or (if (number) NUMBER_FLAG else 0L)

/** True when the run from [start] to the next separator holds only digits and dots. */
private fun isNumericRun(
    text: CharSequence,
    start: Int,
): Boolean {
    var i = start
    while (i < text.length && !isLenientSeparator(text[i])) {
        if (text[i] != '.' && digitValue(text[i]) < 0) return false
        i++
    }
    return true
}

/** The first number in a run of digits and dots from [from], or -1 when only dots remain. */
private fun nextNumber(
    text: CharSequence,
    from: Int,
): Long {
    var i = from
    while (i < text.length && text[i] == '.') i++
    if (i == text.length || isLenientSeparator(text[i])) return -1L
    var end = i
    while (end < text.length && digitValue(text[end]) >= 0) end++
    return token(i, end, true)
}

// Branches follow PARSING section 2 step 2 one for one: a number inside a run, separators, a
// run of digits and dots, and a word.

/**
 * The next token at or after [from]. A run without separators is one word, dots included
 * (`B.S.`), unless it holds only digits and dots: then each run of digits between dots is a number
 * (`2080.05.15`). Words end at a separator, so [from] inside a run (its previous char isn't a
 * separator) means the last token was a number of the same run.
 */
@Suppress("CyclomaticComplexMethod", "ReturnCount", "LoopWithTooManyJumpStatements")
private fun nextToken(
    text: CharSequence,
    from: Int,
): Long {
    if (from > 0 && from < text.length && !isLenientSeparator(text[from - 1])) {
        val number = nextNumber(text, from)
        if (number >= 0) return number
    }
    var i = from
    while (i < text.length) {
        if (isLenientSeparator(text[i]) || (i > 0 && !isLenientSeparator(text[i - 1]))) {
            i++
            continue
        }
        if (isNumericRun(text, i)) {
            val number = nextNumber(text, i)
            if (number >= 0) return number
            i++
            continue
        }
        var end = i
        while (end < text.length && !isLenientSeparator(text[end])) end++
        return token(i, end, false)
    }
    return -1L
}

// One loop with the counts in locals keeps the lenient parser free of allocation (ADR-0002 section 7).

/** PARSING section 2: the lenient result before validation, or a negative kind. */
@Suppress("CyclomaticComplexMethod")
internal fun scanLenient(text: CharSequence): Long {
    val strict = scanStrictValid(text)
    if (strict >= 0) return strict
    var numbers = 0
    var words = 0
    var month = 0
    var tooLong = false
    var v0 = 0
    var v1 = 0
    var v2 = 0
    var l0 = 0
    var l1 = 0
    var l2 = 0
    var pos = 0
    while (true) {
        val t = nextToken(text, pos)
        if (t < 0) break
        val start = (t and INDEX_MASK).toInt()
        val end = ((t ushr END_SHIFT) and INDEX_MASK).toInt()
        pos = end
        if (t and NUMBER_FLAG != 0L) {
            val length = end - start
            if (length > MAX_DIGITS) tooLong = true
            val value = if (length > MAX_DIGITS) 0 else numberValue(text, start, end)
            when (numbers) {
                0 -> {
                    v0 = value
                    l0 = length
                }
                1 -> {
                    v1 = value
                    l1 = length
                }
                2 -> {
                    v2 = value
                    l2 = length
                }
            }
            numbers++
        } else if (!isDropped(text, start, end)) {
            words++
            if (words == 1) month = monthOfWord(text, start, end)
        }
    }
    return when {
        tooLong -> scanError(NepDateException.Kind.NUMBER_TOO_LONG)
        words == 1 && month > 0 && numbers == 2 -> monthNameForm(v0, l0, v1, l1, month)
        words == 0 && numbers == 3 -> numericForm(v0, l0, v1, v2, l2)
        else -> scanError(NepDateException.Kind.UNRECOGNIZED)
    }
}

private fun numberValue(
    text: CharSequence,
    start: Int,
    end: Int,
): Int {
    var value = 0
    for (i in start until end) value = value * 10 + digitValue(text[i])
    return value
}

/** PARSING section 2 step 3: one 4-digit year, and a day of 1 or 2 digits, in either order. */
private fun monthNameForm(
    v0: Int,
    l0: Int,
    v1: Int,
    l1: Int,
    month: Int,
): Long {
    val firstIsYear = l0 == MAX_DIGITS
    val secondIsYear = l1 == MAX_DIGITS
    val dayLength = if (firstIsYear) l1 else l0
    return when {
        firstIsYear == secondIsYear || dayLength > 2 -> scanError(NepDateException.Kind.AMBIGUOUS)
        firstIsYear -> scanned(v0, month, v1)
        else -> scanned(v1, month, v0)
    }
}

/** PARSING section 2 step 4: `Y M D` or `D M Y`, told apart by the 4-digit year. */
private fun numericForm(
    v0: Int,
    l0: Int,
    v1: Int,
    v2: Int,
    l2: Int,
): Long =
    when {
        l0 == MAX_DIGITS -> scanned(v0, v1, v2)
        l2 == MAX_DIGITS -> scanned(v2, v1, v0)
        else -> scanError(NepDateException.Kind.AMBIGUOUS)
    }
