package io.github.sanamhub.nepdate

// One implementation per format, appending to the caller's StringBuilder (ADR-0002 section 6).
// The String forms in NepaliDate call these with a builder sized for the longest output.

/** Longest short form: `2080/05/15`. */
internal const val SHORT_CAPACITY: Int = 10

/** Longest long form: `Wednesday, Baishakh 15, 2080` plus room for Nepali names. */
internal const val LONG_CAPACITY: Int = 32

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
            // A backslash at the very end writes nothing (FORMATTING section 5).
            '\\' -> {
                if (i + 1 < pattern.length) out.append(pattern[i + 1])
                i += 2
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
