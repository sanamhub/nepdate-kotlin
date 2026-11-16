package io.github.sanamhub.nepdate

// The append forms write to the caller's StringBuilder (ADR-0002 section 6). The String forms of
// the short and long formats fill one array instead; a test checks the two agree for every day.

/** Longest short form: `2080/05/15`. */
internal const val SHORT_CAPACITY: Int = 10

/**
 * Room for the longest long form in either language: the longest weekday and month names, the two
 * `", "`, the space, a two-digit day and a four-digit year.
 */
private val LONG_CHARS: Int =
    Weekday.entries.maxOf { maxOf(it.displayName(Lang.ENGLISH).length, it.displayName(Lang.NEPALI).length) } +
        Month.entries.maxOf { maxOf(it.displayName(Lang.ENGLISH).length, it.displayName(Lang.NEPALI).length) } +
        11

private fun zeroOf(lang: Lang): Char = if (lang == Lang.NEPALI) DEVANAGARI_ZERO else '0'

/** Writes a number below 100, as two digits when it has two or [pad] asks for them. */
private fun writeField(
    buffer: CharArray,
    position: Int,
    value: Int,
    pad: Boolean,
    zero: Char,
): Int {
    var at = position
    if (pad || value >= 10) buffer[at++] = zero + value / 10
    buffer[at++] = zero + value % 10
    return at
}

/** Writes a four-digit year; every supported year has four digits. */
private fun writeYear(
    buffer: CharArray,
    position: Int,
    year: Int,
    zero: Char,
): Int {
    val high = year / 100
    val low = year % 100
    buffer[position] = zero + high / 10
    buffer[position + 1] = zero + high % 10
    buffer[position + 2] = zero + low / 10
    buffer[position + 3] = zero + low % 10
    return position + 4
}

private fun writeName(
    buffer: CharArray,
    position: Int,
    name: String,
): Int {
    name.toCharArray(buffer, position, 0, name.length)
    return position + name.length
}

/**
 * The short form as a String. It fills one array in place: a StringBuilder starts in Latin-1 and
 * copies itself on the first Devanagari digit, and checks its capacity on every append.
 */
internal fun shortString(
    date: NepaliDate,
    order: DateOrder,
    separator: Separator,
    pad: Boolean,
    lang: Lang,
): String {
    val buffer = CharArray(SHORT_CAPACITY)
    val zero = zeroOf(lang)
    val sep = separator.char
    // The constant's name spells its field order.
    val fields = order.name
    var length = 0
    for (i in 0 until 3) {
        if (i > 0) buffer[length++] = sep
        length =
            when (fields[i]) {
                'Y' -> writeYear(buffer, length, date.year, zero)
                'M' -> writeField(buffer, length, date.monthValue, pad, zero)
                else -> writeField(buffer, length, date.dayOfMonth, pad, zero)
            }
    }
    return String(buffer, 0, length)
}

/** The long form as a String, filled in place like [shortString]. */
internal fun longString(
    date: NepaliDate,
    weekday: Boolean,
    year: Boolean,
    pad: Boolean,
    lang: Lang,
): String {
    val buffer = CharArray(LONG_CHARS)
    val zero = zeroOf(lang)
    var length = 0
    if (weekday) {
        length = writeName(buffer, length, date.dayOfWeek.displayName(lang))
        buffer[length++] = ','
        buffer[length++] = ' '
    }
    length = writeName(buffer, length, date.month.displayName(lang))
    buffer[length++] = ' '
    length = writeField(buffer, length, date.dayOfMonth, pad, zero)
    if (year) {
        buffer[length++] = ','
        buffer[length++] = ' '
        length = writeYear(buffer, length, date.year, zero)
    }
    return String(buffer, 0, length)
}

internal fun appendShort(
    out: StringBuilder,
    date: NepaliDate,
    order: DateOrder,
    separator: Separator,
    pad: Boolean,
    lang: Lang,
): StringBuilder {
    val yearWidth = if (pad) 4 else 0
    val fieldWidth = if (pad) 2 else 0
    val year = date.year.toLong()
    val month = date.monthValue.toLong()
    val day = date.dayOfMonth.toLong()
    val sep = separator.char
    when (order) {
        DateOrder.YMD -> {
            appendNumber(out, year, yearWidth, lang).append(sep)
            appendNumber(out, month, fieldWidth, lang).append(sep)
            appendNumber(out, day, fieldWidth, lang)
        }
        DateOrder.YDM -> {
            appendNumber(out, year, yearWidth, lang).append(sep)
            appendNumber(out, day, fieldWidth, lang).append(sep)
            appendNumber(out, month, fieldWidth, lang)
        }
        DateOrder.MYD -> {
            appendNumber(out, month, fieldWidth, lang).append(sep)
            appendNumber(out, year, yearWidth, lang).append(sep)
            appendNumber(out, day, fieldWidth, lang)
        }
        DateOrder.MDY -> {
            appendNumber(out, month, fieldWidth, lang).append(sep)
            appendNumber(out, day, fieldWidth, lang).append(sep)
            appendNumber(out, year, yearWidth, lang)
        }
        DateOrder.DYM -> {
            appendNumber(out, day, fieldWidth, lang).append(sep)
            appendNumber(out, year, yearWidth, lang).append(sep)
            appendNumber(out, month, fieldWidth, lang)
        }
        DateOrder.DMY -> {
            appendNumber(out, day, fieldWidth, lang).append(sep)
            appendNumber(out, month, fieldWidth, lang).append(sep)
            appendNumber(out, year, yearWidth, lang)
        }
    }
    return out
}

/** FORMATTING section 5: a standard pattern, or one left-to-right pass over a custom one. */
internal fun appendPattern(
    out: StringBuilder,
    date: NepaliDate,
    pattern: String,
    lang: Lang,
): StringBuilder =
    when (pattern) {
        "", "G", "g", "d" -> appendShort(out, date, DateOrder.YMD, Separator.SLASH, true, lang)
        "D" -> appendLong(out, date, weekday = false, year = true, pad = true, lang = lang)
        "s" -> appendShort(out, date, DateOrder.YMD, Separator.DASH, true, lang)
        else -> appendCustomPattern(out, date, pattern, lang)
    }

private fun appendCustomPattern(
    out: StringBuilder,
    date: NepaliDate,
    pattern: String,
    lang: Lang,
): StringBuilder {
    var i = 0
    while (i < pattern.length) {
        val c = pattern[i]
        when (c) {
            // A backslash escapes the next character; a backslash at the very end writes itself
            // (FORMATTING section 5).
            '\\' ->
                if (i + 1 < pattern.length) {
                    out.append(pattern[i + 1])
                    i += 2
                } else {
                    out.append('\\')
                    i++
                }
            // An unterminated quote runs to the end of the pattern.
            '\'' -> {
                val close = pattern.indexOf('\'', i + 1)
                val end = if (close < 0) pattern.length else close
                out.append(pattern, i + 1, end)
                i = end + 1
            }
            'y', 'M', 'd' -> {
                var run = i
                while (run < pattern.length && pattern[run] == c) run++
                appendToken(out, date, c, run - i, lang)
                i = run
            }
            else -> {
                out.append(c)
                i++
            }
        }
    }
    return out
}

private fun appendToken(
    out: StringBuilder,
    date: NepaliDate,
    letter: Char,
    length: Int,
    lang: Lang,
) {
    when (letter) {
        'y' ->
            if (length >= 4) {
                appendNumber(out, date.year.toLong(), 4, lang)
            } else {
                appendNumber(out, (date.year % 100).toLong(), 2, lang)
            }
        'M' ->
            when {
                length >= 4 -> out.append(date.month.displayName(lang))
                length == 3 -> out.append(date.month.shortName(lang))
                else -> appendNumber(out, date.monthValue.toLong(), length, lang)
            }
        else -> appendNumber(out, date.dayOfMonth.toLong(), if (length == 2) 2 else 0, lang)
    }
}

internal fun appendLong(
    out: StringBuilder,
    date: NepaliDate,
    weekday: Boolean,
    year: Boolean,
    pad: Boolean,
    lang: Lang,
): StringBuilder {
    if (weekday) out.append(date.dayOfWeek.displayName(lang)).append(", ")
    out.append(date.month.displayName(lang)).append(' ')
    appendNumber(out, date.dayOfMonth.toLong(), if (pad) 2 else 0, lang)
    if (year) {
        out.append(", ")
        appendNumber(out, date.year.toLong(), 4, lang)
    }
    return out
}
