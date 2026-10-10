package io.github.sanamhub.nepdate

// PARSING sections 2 and 3, on top of the strict scanner in Parsing.kt.

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

private fun monthOfWord(
    text: CharSequence,
    start: Int,
    end: Int,
): Int {
    for (i in MONTH_TOKENS.indices) {
        if (tokenEquals(text, start, end, MONTH_TOKENS[i])) return MONTH_OF_TOKEN[i]
    }
    return 0
}

private fun isDropped(
    text: CharSequence,
    start: Int,
    end: Int,
): Boolean {
    for (token in DROPPED_TOKENS) {
        if (tokenEquals(text, start, end, token)) return true
    }
    return false
}

/**
 * What the lenient tokeniser found. A small mutable holder local to one call; escape analysis
 * keeps it off the heap.
 */
private class LenientTokens {
    var numbers = 0
    var words = 0
    var month = 0
    var tooLong = false
    val values = IntArray(3)
    val lengths = IntArray(3)

    fun addNumber(
        value: Int,
        length: Int,
    ) {
        if (length > MAX_DIGITS) tooLong = true
        if (numbers < 3) {
            values[numbers] = value
            lengths[numbers] = length
        }
        numbers++
    }
}

/** PARSING section 2 step 2: tokenise [text] from [start] to [end], a run without separators. */
private fun addRawToken(
    tokens: LenientTokens,
    text: CharSequence,
    start: Int,
    end: Int,
) {
    var numeric = true
    for (i in start until end) {
        if (digitValue(text[i]) < 0 && text[i] != '.') numeric = false
    }
    if (numeric) {
        // Digits and dots only: each run of digits between dots is a number.
        var value = 0
        var length = 0
        for (i in start..end) {
            if (i < end && text[i] != '.') {
                // Values of over four digits only set tooLong; keep the Int from overflowing.
                if (length < MAX_DIGITS + 1) value = value * 10 + digitValue(text[i])
                length++
            } else if (length > 0) {
                tokens.addNumber(value, length)
                value = 0
                length = 0
            }
        }
    } else if (!isDropped(text, start, end)) {
        tokens.words++
        if (tokens.words == 1) tokens.month = monthOfWord(text, start, end)
    }
}

/** PARSING section 2: the lenient result before validation, or a negative kind. */
internal fun scanLenient(text: CharSequence): Long {
    val strict = scanStrictValid(text)
    if (strict >= 0) return strict
    val tokens = LenientTokens()
    var start = -1
    for (i in 0..text.length) {
        if (i == text.length || isLenientSeparator(text[i])) {
            if (start >= 0) addRawToken(tokens, text, start, i)
            start = -1
        } else if (start < 0) {
            start = i
        }
    }
    return when {
        tokens.tooLong -> scanError(NepDateException.Kind.NUMBER_TOO_LONG)
        tokens.words == 1 && tokens.month > 0 && tokens.numbers == 2 -> monthNameForm(tokens)
        tokens.words == 0 && tokens.numbers == 3 -> numericForm(tokens)
        else -> scanError(NepDateException.Kind.UNRECOGNIZED)
    }
}

/** PARSING section 2 step 3: one 4-digit year, and a day of 1 or 2 digits, in either order. */
private fun monthNameForm(tokens: LenientTokens): Long {
    val firstIsYear = tokens.lengths[0] == MAX_DIGITS
    val secondIsYear = tokens.lengths[1] == MAX_DIGITS
    val year = if (firstIsYear) 0 else 1
    val day = 1 - year
    return if (firstIsYear == secondIsYear || tokens.lengths[day] > 2) {
        scanError(NepDateException.Kind.AMBIGUOUS)
    } else {
        scanned(tokens.values[year], tokens.month, tokens.values[day])
    }
}

/** PARSING section 2 step 4: `Y M D` or `D M Y`, told apart by the 4-digit year. */
private fun numericForm(tokens: LenientTokens): Long =
    when {
        tokens.lengths[0] == MAX_DIGITS -> scanned(tokens.values[0], tokens.values[1], tokens.values[2])
        tokens.lengths[2] == MAX_DIGITS -> scanned(tokens.values[2], tokens.values[1], tokens.values[0])
        else -> scanError(NepDateException.Kind.AMBIGUOUS)
    }
