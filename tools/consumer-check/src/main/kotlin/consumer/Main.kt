package consumer

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro

/**
 * The throwaway consumer: depends on both published artifacts from mavenLocal and exercises one
 * conversion and one patro event, printing both. This is the same snippet shape as the README.
 */
fun main() {
    val date = NepaliDate.ofGregorian(2024, 4, 13)
    println("converted: ${date.toIsoString()} (${date.toLongString(lang = Lang.NEPALI)})")

    val event = Patro.info(date).events.first()
    println("event: ${event.english} (${event.nepali})")
}
