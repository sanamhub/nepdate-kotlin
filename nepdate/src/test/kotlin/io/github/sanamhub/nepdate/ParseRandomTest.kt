package io.github.sanamhub.nepdate

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** 200,000 random strings; the parsers return a date or throw NepDateException, nothing else. */
class ParseRandomTest {
    private val pieces: List<String> =
        ("0123456789".map { it.toString() } + "०१२३४५६७८९".map { it.toString() }) +
            listOf("-", "/", ".", "_", "\\", " ", "।", "|", ",") +
            ('a'..'z').map { it.toString() } + ('A'..'Z').map { it.toString() } +
            listOf("साउन", "बैशाख", "चैत्र", "मंसिर", "वि.सं.", "बि.सं", "गते", "मिति", "BS", "B.S.", "Shrawan", "shr", "2080", "15")

    private fun randomString(random: Random): String {
        val length = random.nextInt(0, 25)
        val out = StringBuilder()
        while (out.length < length) {
            // One in eight pieces is any Char at all, surrogates included.
            if (random.nextInt(8) == 0) out.append(random.nextInt(0, 0x10000).toChar()) else out.append(pieces[random.nextInt(pieces.size)])
        }
        out.setLength(length)
        return out.toString()
    }

    private inline fun dateOrKind(parser: () -> NepaliDate): Any =
        try {
            parser()
        } catch (e: NepDateException) {
            e.kind
        }

    @Test
    fun `200000 random strings`() {
        val random = Random(42)
        repeat(200_000) {
            val text = randomString(random)
            val strict = dateOrKind { NepaliDate.parse(text) }
            dateOrKind { NepaliDate.parseLenient(text) }
            val orNull = NepaliDate.parseOrNull(text)
            if (strict is NepaliDate) {
                assertEquals(strict, orNull, "[$text]")
                assertEquals(strict, NepaliDate.parse(strict.toString()), "[$text]")
            } else {
                assertNull(orNull, "[$text]")
            }
        }
    }
}
