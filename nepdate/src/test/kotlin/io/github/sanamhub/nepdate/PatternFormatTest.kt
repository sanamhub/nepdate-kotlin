package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PatternFormatTest {
    // A run of exactly three M: the short month name, where D-04 differs from C#.
    private fun hasShortMonth(pattern: String): Boolean {
        var i = 0
        while (i < pattern.length) {
            if (pattern[i] == 'M') {
                var j = i
                while (j < pattern.length && pattern[j] == 'M') j++
                if (j - i == 3) return true
                i = j
            } else {
                i++
            }
        }
        return false
    }

    @Test
    fun `every row of format-pattern tsv except the D-04 rows`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/format-pattern.tsv")
        assertEquals(451, rows.size)
        var d04 = 0
        for (row in rows) {
            val (y, m, d) = SharedFiles.ymd(row.getValue("bs"))
            val date = NepaliDate.of(y, m, d)
            val pattern = row.getValue("pattern")
            val expected = row.getValue("result")
            if ((m == 3 || m == 6) && hasShortMonth(pattern)) {
                // D-04: C# writes Ash for both Ashad and Ashoj; the port writes Asa and Aso.
                d04++
                assertEquals(expected.replace("Ash", if (m == 3) "Asa" else "Aso"), date.format(pattern), "$row")
                continue
            }
            assertEquals(expected, date.format(pattern), "$row")
        }
        assertEquals(7, d04)
    }

    @Test
    fun `examples of FORMATTING section 5`() {
        val date = NepaliDate.of(2081, 4, 15)
        assertEquals("2081-04-15", date.format("yyyy-MM-dd"))
        assertEquals("4/15/2081", date.format("M/d/yyyy"))
        assertEquals("2081BS", date.format("yyyy'BS'"))
        assertEquals("2081MM-dd", date.format("""yyyy\M\M-\d\d"""))
        // A backslash at the very end writes nothing (hub e2f9778 fixed the reference to match).
        assertEquals("2081", date.format("""yyyy\"""))
        assertEquals("", date.format("""\"""))
        assertEquals("Shr 2081", date.format("MMM yyyy"))
        assertEquals("15.04.81", date.format("dd.MM.yy"))
    }

    @Test
    fun `standard patterns`() {
        val date = NepaliDate.of(2081, 4, 15)
        for (pattern in listOf("", "G", "g", "d")) assertEquals("2081/04/15", date.format(pattern))
        assertEquals("२०८१/०४/१५", date.format("d", Lang.NEPALI))
        assertEquals("Shrawan 15, 2081", date.format("D"))
        assertEquals("साउन १५, २०८१", date.format("D", Lang.NEPALI))
        assertEquals("2081-04-15", date.format("s"))
        assertEquals("२०८१-०४-१५", date.format("s", Lang.NEPALI))
    }

    @Test
    fun `token rules`() {
        val date = NepaliDate.of(2081, 4, 5)
        assertEquals("5 05 5 5", date.format("d dd ddd dddd"))
        assertEquals("81 81 81 2081 2081", date.format("y yy yyy yyyy yyyyy"))
        assertEquals("4 04 Shr Shrawan Shrawan", date.format("M MM MMM MMMM MMMMM"))
        assertEquals("साउन साउन ०४", date.format("MMM MMMM MM", Lang.NEPALI))
        // Literal characters are never localised, even digits.
        assertEquals("12 ०५", date.format("'12' dd", Lang.NEPALI))
        assertEquals("x", date.format("""\x"""))
        assertEquals("2081", date.format("""yyyy\"""))
        assertEquals("2081 open", date.format("yyyy' open"))
        assertEquals("2081:", date.format("yyyy:"))
    }

    @Test
    fun `formatTo appends and returns the builder`() {
        val out = StringBuilder("> ")
        assertSame(out, NepaliDate.of(2081, 4, 15).formatTo(out, "dd/MM"))
        assertSame(out, NepaliDate.of(2081, 4, 15).formatTo(out, " D", Lang.NEPALI))
        // " D" isn't the standard pattern D, so the D is copied as-is.
        assertEquals("> 15/04 D", out.toString())
    }
}
