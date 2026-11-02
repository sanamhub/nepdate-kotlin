package io.github.sanamhub.nepdate.benchmark

import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import io.github.sanamhub.nepdate.NepaliDate

/**
 * For each competitor, converts the 64 BENCHMARK section 2 dates both ways and prints whether the
 * results agree with shared/data (as represented by this library). A competitor that throws on an
 * out-of-range input counts that input as a difference.
 */
fun main() {
    val expectedAd =
        Array(Inputs.COUNT) { i ->
            val (y, m, d) = Inputs.bs[i]
            val n = NepaliDate.of(y, m, d)
            intArrayOf(n.gregorianYear, n.gregorianMonth, n.gregorianDay)
        }
    val expectedBs =
        Array(Inputs.COUNT) { i ->
            val (y, m, d) = Inputs.ad[i]
            val n = NepaliDate.ofGregorian(y, m, d)
            intArrayOf(n.year, n.monthValue, n.dayOfMonth)
        }

    println("AgreementCheck (${Inputs.COUNT} dates each way, against shared/data):")
    check(
        "shivathapaa",
        expectedAd,
        expectedBs,
        bsToAd = { (y, m, d) ->
            val ad = NepaliDateConverter.convertNepaliToEnglish(y, m, d)
            intArrayOf(ad.year, ad.month, ad.dayOfMonth)
        },
        adToBs = { (y, m, d) ->
            val bs = NepaliDateConverter.convertEnglishToNepali(y, m, d)
            intArrayOf(bs.year, bs.month, bs.dayOfMonth)
        },
    )
}

private fun check(
    name: String,
    expectedAd: Array<IntArray>,
    expectedBs: Array<IntArray>,
    bsToAd: (IntArray) -> IntArray?,
    adToBs: (IntArray) -> IntArray?,
) {
    val adDiffs = countDiffs(expectedAd, Inputs.bs, bsToAd)
    val bsDiffs = countDiffs(expectedBs, Inputs.ad, adToBs)
    val verdict =
        if (adDiffs == 0 && bsDiffs == 0) {
            "yes"
        } else {
            "no ($adDiffs of ${Inputs.COUNT} BS-to-AD differ, $bsDiffs of ${Inputs.COUNT} AD-to-BS differ)"
        }
    println("  $name: $verdict")
}

// A competitor throwing on an out-of-range input is itself a difference, so the catch is
// intentional and the exception is not rethrown.
@Suppress("SwallowedException", "TooGenericExceptionCaught")
private fun countDiffs(
    expected: Array<IntArray>,
    inputs: Array<IntArray>,
    convert: (IntArray) -> IntArray?,
): Int {
    var diffs = 0
    var first: String? = null
    for (i in inputs.indices) {
        val got =
            try {
                convert(inputs[i])
            } catch (e: Exception) {
                null
            }
        val exp = expected[i]
        if (got == null || !got.contentEquals(exp)) {
            diffs++
            if (first == null) {
                first =
                    "first: ${inputs[i].joinToString(
                        "/",
                    )} -> expected ${exp.joinToString("/")}, got ${got?.joinToString("/") ?: "exception"}"
            }
        }
    }
    if (first != null) println("    $first")
    return diffs
}
