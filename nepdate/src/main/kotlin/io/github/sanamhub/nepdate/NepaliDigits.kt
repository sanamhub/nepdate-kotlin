@file:JvmName("NepaliDigits")

package io.github.sanamhub.nepdate

/**
 * [n] written with Devanagari digits (U+0966..U+096F), zero-padded to at least [minWidth] digits.
 * A leading minus sign is not counted in the width; a [minWidth] below 0 counts as 0.
 *
 * @return for example `२०८१` for 2081, `०५` for 5 with [minWidth] 2, `-१२` for -12
 */
@JvmOverloads
public fun nepaliDigits(
    n: Long,
    minWidth: Int = 0,
): String {
    val out = StringBuilder(21)
    if (n < 0) {
        out.append('-')
        // Long.MIN_VALUE has no positive Long, so its last digit is written separately.
        if (n == Long.MIN_VALUE) {
            appendNumber(out, -(n / 10), minWidth - 1, Lang.NEPALI)
            return appendDigit(out, -(n % 10).toInt(), Lang.NEPALI).toString()
        }
    }
    return appendNumber(out, if (n < 0) -n else n, minWidth, Lang.NEPALI).toString()
}
