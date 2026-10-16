package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ParseTest {
    // "ERR:InvalidCharacter" names NepDateException.Kind.INVALID_CHARACTER, and so on (PARSING section 4).
    private fun kindOf(cell: String): NepDateException.Kind {
        val name = cell.removePrefix("ERR:")
        val upperSnake = name.replace(Regex("([a-z])([A-Z])"), "$1_$2").uppercase()
        return NepDateException.Kind.valueOf(upperSnake)
    }

    private fun check(
        input: String,
        expected: String,
        parser: (String) -> NepaliDate,
    ) {
        if (expected.startsWith("ERR:")) {
            val error = assertFailsWith<NepDateException>("[$input]") { parser(input) }
            assertEquals(kindOf(expected), error.kind, "[$input]")
            if (error.kind == NepDateException.Kind.INVALID_DAY) assertTrue(error.monthLength in 29..32)
        } else {
            val (y, m, d) = SharedFiles.ymd(expected)
            assertEquals(NepaliDate.of(y, m, d), parser(input), "[$input]")
        }
    }

    private val rows = SharedFiles.tsv("spec/vectors/parse.tsv")

    @Test
    fun `every kind in the vectors maps to a NepDateException kind`() {
        val cells = rows.flatMap { listOf(it.getValue("strict"), it.getValue("lenient")) }.filter { it.startsWith("ERR:") }.toSet()
        assertEquals(8, cells.size, cells.toString())
        assertEquals(NepDateException.Kind.WRONG_GROUP_COUNT, kindOf("ERR:WrongGroupCount"))
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf("ERR:OutOfRange"))
    }

    @Test
    fun `strict column of parse tsv`() {
        assertEquals(67, rows.size)
        for (row in rows) check(row.getValue("input"), row.getValue("strict")) { NepaliDate.parse(it) }
    }

    @Test
    fun `parseOrNull agrees with the strict column`() {
        for (row in rows) {
            val expected = row.getValue("strict")
            val result = NepaliDate.parseOrNull(row.getValue("input"))
            if (expected.startsWith("ERR:")) assertNull(result, row.toString()) else assertEquals(expected, result?.toIsoString())
        }
    }

    @Test
    fun `lenient column of parse tsv`() {
        for (row in rows) check(row.getValue("input"), row.getValue("lenient")) { NepaliDate.parseLenient(it) }
    }

    @Test
    fun `examples`() {
        val date = NepaliDate.of(2080, 5, 15)
        assertEquals(date, NepaliDate.parse("२०८०/०५/१५"))
        assertEquals(date, NepaliDate.parse("२०८०-5-१५"))
        assertEquals(date, NepaliDate.parse(StringBuilder("2080/05/15")))
        val shrawan = NepaliDate.of(2080, 4, 15)
        assertEquals(shrawan, NepaliDate.parseLenient("15 Shrawan 2080"))
        assertEquals(shrawan, NepaliDate.parseLenient("Shrawan 15, 2080"))
        assertEquals(shrawan, NepaliDate.parseLenient("2080 Shrawan 15"))
    }

    @Test
    fun `case folding follows the reference`() {
        val shrawan = NepaliDate.of(2080, 4, 15)
        assertEquals(shrawan, NepaliDate.parseLenient("15 sHrAwAn 2080"))
        // The Kelvin sign lower-cases to k; the dotless ı does not lower-case to i.
        assertEquals(NepaliDate.of(2080, 7, 15), NepaliDate.parseLenient("15 Kartik 2080"))
        assertEquals(
            NepDateException.Kind.UNRECOGNIZED,
            assertFailsWith<NepDateException> { NepaliDate.parseLenient("15 Baıshakh 2080") }.kind,
        )
        // The long s ſ (U+017F) is not folded either (PARSING section 2, hub 1d7ca17).
        assertEquals(
            NepDateException.Kind.UNRECOGNIZED,
            assertFailsWith<NepDateException> { NepaliDate.parseLenient("15 ſhrawan 2080") }.kind,
        )
    }
}
