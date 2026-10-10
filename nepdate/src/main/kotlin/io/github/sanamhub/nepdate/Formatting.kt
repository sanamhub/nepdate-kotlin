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
