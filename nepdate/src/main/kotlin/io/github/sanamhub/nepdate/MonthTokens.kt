package io.github.sanamhub.nepdate

// PARSING section 3. Latin tokens are lower case; input letters are folded to match.
private val MONTH_TOKENS: Array<String> =
    arrayOf(
        "baishakh",
        "baisakh",
        "baishak",
        "vaishakh",
        "vaisakh",
        "bai",
        "बैशाख",
        "वैशाख",
        "बैसाख",
        "jestha",
        "jeth",
        "jyestha",
        "jes",
        "जेठ",
        "जेष्ठ",
        "ज्येष्ठ",
        "ashad",
        "ashadh",
        "asar",
        "asadh",
        "asa",
        "असार",
        "आषाढ",
        "असाढ",
        "shrawan",
        "shravan",
        "srawan",
        "sawan",
        "saun",
        "shr",
        "साउन",
        "श्रावण",
        "सावन",
        "bhadra",
        "bhadau",
        "bha",
        "भदौ",
        "भाद्र",
        "भाद्रपद",
        "ashoj",
        "asoj",
        "ashwin",
        "aso",
        "असोज",
        "आश्विन",
        "असौज",
        "kartik",
        "kattik",
        "kar",
        "कार्तिक",
        "कात्तिक",
        "mangsir",
        "mansir",
        "margashirsha",
        "man",
        "मंसिर",
        "मङ्सिर",
        "मार्गशीर्ष",
        "poush",
        "paush",
        "push",
        "pus",
        "pou",
        "पुष",
        "पौष",
        "पुस",
        "magh",
        "mag",
        "माघ",
        "falgun",
        "phalgun",
        "fagun",
        "fal",
        "फागुन",
        "फाल्गुन",
        "chaitra",
        "chait",
        "cha",
        "चैत",
        "चैत्र",
    )

// The month of each entry of MONTH_TOKENS.
private val MONTH_OF_TOKEN: IntArray =
    intArrayOf(
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        1,
        2,
        2,
        2,
        2,
        2,
        2,
        2,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        3,
        4,
        4,
        4,
        4,
        4,
        4,
        4,
        4,
        4,
        5,
        5,
        5,
        5,
        5,
        5,
        6,
        6,
        6,
        6,
        6,
        6,
        6,
        7,
        7,
        7,
        7,
        7,
        8,
        8,
        8,
        8,
        8,
        8,
        8,
        9,
        9,
        9,
        9,
        9,
        9,
        9,
        9,
        10,
        10,
        10,
        11,
        11,
        11,
        11,
        11,
        11,
        12,
        12,
        12,
        12,
        12,
    )

// Era markers, compared case-insensitively, and the words गते and मिति, compared exactly.
private val DROPPED_TOKENS: Array<String> =
    arrayOf("bs", "b.s.", "b.s", "vs", "v.s.", "v.s", "बि.सं.", "वि.सं.", "बि.सं", "वि.सं", "गते", "मिति")

/**
 * The reference parser lower-cases with Python's `str.lower()`. The only characters it maps onto
 * an ASCII letter are A to Z and the Kelvin sign; `regionMatches(ignoreCase = true)` would also
 * match the dotless ı and the dotted İ against `i`, which the spec's vectors reject.
 */
private fun foldCase(c: Char): Char =
    when (c) {
        in 'A'..'Z' -> c + ('a' - 'A')
        'K' -> 'k'
        else -> c
    }

@Suppress("ReturnCount") // a length check and a mismatch both end the comparison early
private fun tokenEquals(
    text: CharSequence,
    start: Int,
    end: Int,
    token: String,
): Boolean {
    if (end - start != token.length) return false
    for (i in token.indices) {
        if (foldCase(text[start + i]) != token[i]) return false
    }
    return true
}

internal fun monthOfWord(
    text: CharSequence,
    start: Int,
    end: Int,
): Int {
    for (i in MONTH_TOKENS.indices) {
        if (tokenEquals(text, start, end, MONTH_TOKENS[i])) return MONTH_OF_TOKEN[i]
    }
    return 0
}

internal fun isDropped(
    text: CharSequence,
    start: Int,
    end: Int,
): Boolean {
    for (token in DROPPED_TOKENS) {
        if (tokenEquals(text, start, end, token)) return true
    }
    return false
}
