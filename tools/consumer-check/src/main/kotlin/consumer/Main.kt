package consumer

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.serialization.NepaliDateSerializer

/**
 * The throwaway consumer: depends on the three published artifacts from mavenLocal and exercises
 * one conversion, one patro event and the serializer's descriptor, printing all three. This is the
 * same snippet shape as the README, and it proves the coordinates and POMs of each artifact.
 */
fun main() {
    val date = NepaliDate.ofGregorian(2024, 4, 13)
    println("converted: ${date.toIsoString()} (${date.toLongString(lang = Lang.NEPALI)})")

    val event = Patro.info(date).events.first()
    println("event: ${event.english} (${event.nepali})")

    println("serializer: ${NepaliDateSerializer.descriptor.serialName}")
}
