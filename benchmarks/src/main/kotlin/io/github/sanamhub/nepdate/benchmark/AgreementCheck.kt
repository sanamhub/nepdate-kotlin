package io.github.sanamhub.nepdate.benchmark

import com.nepalidate.ADBS.NepaliDateConverter.AdBs
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import io.github.aagitoex.nepdate.Converter
import io.github.aagitoex.nepdate.NepDate
import io.github.sanamhub.nepdate.NepaliDate
import java.time.LocalDate
import java.util.Calendar

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
    check(
        "aagitoex",
        expectedAd,
        expectedBs,
        bsToAd = { (y, m, d) ->
            val ad = NepDate(y, m, d).ad
            intArrayOf(ad.year, ad.monthValue, ad.dayOfMonth)
        },
        adToBs = { (y, m, d) ->
            val bs = Converter.fromADToBS(LocalDate.of(y, m, d))
            intArrayOf(bs.year, bs.month, bs.day)
        },
    )
    check(
        "adbs",
        expectedAd,
        expectedBs,
        bsToAd = { (y, m, d) ->
            val date = AdBs().convertBsToAd("%02d%02d%04d".format(d, m, y))
            val calendar = Calendar.getInstance().apply { time = date }
            intArrayOf(calendar[Calendar.YEAR], calendar[Calendar.MONTH] + 1, calendar[Calendar.DAY_OF_MONTH])
        },
        adToBs = { (y, m, d) ->
            val out = AdBs().convertAdToBs("%02d-%02d-%04d".format(d, m, y))
            val parts = out.split('-')
            intArrayOf(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
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
