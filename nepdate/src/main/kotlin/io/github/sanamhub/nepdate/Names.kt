package io.github.sanamhub.nepdate

// The name tables of FORMATTING section 1, indexed by Month.ordinal and Weekday.ordinal.

internal val MONTH_NAMES_EN: Array<String> =
    arrayOf("Baishakh", "Jestha", "Ashad", "Shrawan", "Bhadra", "Ashoj", "Kartik", "Mangsir", "Poush", "Magh", "Falgun", "Chaitra")

// D-04: Ashad and Ashoj are Asa and Aso; C# gives both as Ash.
internal val MONTH_SHORT_EN: Array<String> =
    arrayOf("Bai", "Jes", "Asa", "Shr", "Bha", "Aso", "Kar", "Man", "Pou", "Mag", "Fal", "Cha")

internal val MONTH_NAMES_NE: Array<String> =
    arrayOf("बैशाख", "जेठ", "असार", "साउन", "भदौ", "असोज", "कार्तिक", "मंसिर", "पुष", "माघ", "फागुन", "चैत")

internal val WEEKDAY_NAMES_EN: Array<String> =
    arrayOf("Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

internal val WEEKDAY_SHORT_EN: Array<String> = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

internal val WEEKDAY_NAMES_NE: Array<String> =
    arrayOf("आइतवार", "सोमवार", "मङ्गलवार", "बुधवार", "बिहिवार", "शुक्रवार", "शनिवार")

internal val WEEKDAY_SHORT_NE: Array<String> = arrayOf("आइत", "सोम", "मङ्गल", "बुध", "बिहि", "शुक्र", "शनि")

/** U+0966, DEVANAGARI DIGIT ZERO; digit d is DEVANAGARI_ZERO + d. */
internal const val DEVANAGARI_ZERO: Char = '०'

/** Appends the digit [d] (0..9) in [lang]'s digits. */
internal fun appendDigit(
    out: StringBuilder,
    d: Int,
    lang: Lang,
): StringBuilder = out.append(if (lang == Lang.NEPALI) DEVANAGARI_ZERO + d else '0' + d)

/**
 * Appends the non-negative [value] with at least [width] digits, zero-padded, in [lang]'s digits.
 * No allocation: the digits are written from the most significant one, found by dividing.
 */
internal fun appendNumber(
    out: StringBuilder,
    value: Long,
    width: Int,
    lang: Lang,
): StringBuilder {
    var digits = 1
    var scale = 1L
    while (value / scale >= 10) {
        scale *= 10
        digits++
    }
    repeat(width - digits) { appendDigit(out, 0, lang) }
    while (scale > 0) {
        appendDigit(out, ((value / scale) % 10).toInt(), lang)
        scale /= 10
    }
    return out
}
