package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.NepaliDate

/** Serial (ALGORITHM section 4) of [date]; the patro sees the core only through the public API. */
fun serialOf(date: NepaliDate): Int = date.toEpochDay().toInt() + 45920

fun dateAtSerial(serial: Int): NepaliDate = NepaliDate.ofEpochDay(serial - 45920L)

fun d(
    y: Int,
    m: Int,
    day: Int,
): NepaliDate = NepaliDate.of(y, m, day)

/** Every day of the patro span, from BASE_SERIAL. */
fun spanDays(): List<NepaliDate> = (0 until SPAN).map { dateAtSerial(BASE_SERIAL + it) }

/** The 64 B7 dates of BENCHMARK.md section 3: serial `c0 + i * ((c1 - c0) / 63)` over the coverage. */
fun b7Dates(): List<NepaliDate> {
    val c0 = BASE_SERIAL
    val c1 = BASE_SERIAL + SPAN - 1
    return (0 until 64).map { dateAtSerial(c0 + it * ((c1 - c0) / 63)) }
}

/** A row of the PATRO section 4 anchor table. */
class Anchor(
    val date: NepaliDate,
    val tithi: String?,
    val holiday: Boolean,
    val events: List<String>,
)

val ANCHORS: List<Anchor> =
    listOf(
        Anchor(d(2081, 1, 1), "Panchami", true, listOf("New Year", "Mesh Sankranti", "Bisket Jatra")),
        Anchor(d(2081, 1, 2), "Shashthi", false, emptyList()),
        Anchor(d(2081, 2, 15), "Panchami", true, listOf("Republic Day")),
        Anchor(
            d(2080, 11, 1),
            "Chaturthi",
            false,
            listOf("Mangala Chauthi Brata", "People's War Day", "Tilakund Chauthi", "Kumbha Sankranti", "World Radio Day"),
        ),
        Anchor(
            d(2081, 4, 15),
            "Dashami",
            false,
            listOf("Kheer Khane Din", "World Day Against Human Trafficking", "International Friendship Day"),
        ),
        Anchor(d(2070, 6, 15), null, false, emptyList()),
        Anchor(d(2090, 1, 1), null, false, emptyList()),
    )

/** Null when [info] matches [anchor], otherwise what differs. */
fun anchorMismatch(
    anchor: Anchor,
    info: CalendarInfo,
): String? {
    val got = Triple(info.tithi?.english, info.isPublicHoliday, info.events.map { it.english })
    val want = Triple(anchor.tithi, anchor.holiday, anchor.events)
    return if (got == want) null else "${anchor.date}: got $got, want $want"
}
