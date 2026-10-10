package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class ShortLongFormatTest {
    @Test
    fun `dates tsv format columns`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/dates.tsv")
        assertEquals(1018, rows.size)
        for (row in rows) {
            val (y, m, d) = SharedFiles.ymd(row.getValue("bs"))
            val date = NepaliDate.of(y, m, d)
            val bs = row.getValue("bs")
            assertEquals(row.getValue("to_string"), date.toString(), bs)
            assertEquals(row.getValue("unicode"), date.toShortString(lang = Lang.NEPALI), bs)
            assertEquals(row.getValue("long_en"), date.toLongString(), bs)
            assertEquals(row.getValue("long_en_weekday"), date.toLongString(weekday = true), bs)
            assertEquals(row.getValue("long_ne"), date.toLongString(lang = Lang.NEPALI), bs)
            assertEquals(row.getValue("long_ne_weekday"), date.toLongString(weekday = true, lang = Lang.NEPALI), bs)
            assertEquals(row.getValue("dmy_dash_nopad"), date.toShortString(DateOrder.DMY, Separator.DASH, pad = false), bs)
        }
    }

    @Test
    fun `examples of FORMATTING sections 3 and 4`() {
        val date = NepaliDate.of(2080, 5, 15)
        assertEquals("2080-05-15", date.toShortString(DateOrder.YMD, Separator.DASH, true, Lang.ENGLISH))
        assertEquals("15-5-2080", date.toShortString(DateOrder.DMY, Separator.DASH, false, Lang.ENGLISH))
        assertEquals("२०८०/०५/१५", date.toShortString(DateOrder.YMD, Separator.SLASH, true, Lang.NEPALI))
        val shrawan = NepaliDate.of(2081, 4, 15)
        assertEquals("Shrawan 15, 2081", shrawan.toLongString())
        assertEquals("साउन १५, २०८१", shrawan.toLongString(lang = Lang.NEPALI))
        assertEquals("Tuesday, Shrawan 15, 2081", shrawan.toLongString(weekday = true))
        assertEquals("मङ्गलवार, साउन १५, २०८१", shrawan.toLongString(weekday = true, lang = Lang.NEPALI))
        assertEquals("Baishakh 01, 1901", NepaliDate.MIN.toLongString())
        assertEquals("बैशाख ०१, १९०१", NepaliDate.MIN.toLongString(lang = Lang.NEPALI))
        val jestha = NepaliDate.of(2079, 2, 6)
        assertEquals("Friday, Jestha 6", jestha.toLongString(weekday = true, year = false, pad = false))
        assertEquals("शुक्रवार, जेठ ६", jestha.toLongString(weekday = true, year = false, pad = false, lang = Lang.NEPALI))
    }

    @Test
    fun `every order and separator`() {
        val date = NepaliDate.of(2080, 5, 15)
        val expected =
            mapOf(
                DateOrder.YMD to "2080_05_15",
                DateOrder.YDM to "2080_15_05",
                DateOrder.MYD to "05_2080_15",
                DateOrder.MDY to "05_15_2080",
                DateOrder.DYM to "15_2080_05",
                DateOrder.DMY to "15_05_2080",
            )
        for ((order, text) in expected) assertEquals(text, date.toShortString(order, Separator.UNDERSCORE))
        val separators =
            mapOf(
                Separator.SLASH to '/',
                Separator.BACKSLASH to '\\',
                Separator.DOT to '.',
                Separator.UNDERSCORE to '_',
                Separator.DASH to '-',
                Separator.SPACE to ' ',
            )
        for ((separator, char) in separators) {
            assertEquals(char, separator.char)
            assertEquals("2080${char}05${char}15", date.toShortString(separator = separator))
        }
    }

    @Test
    fun `iso string and appending`() {
        assertEquals("2081-04-15", NepaliDate.of(2081, 4, 15).toIsoString())
        assertEquals("1901-01-01", NepaliDate.MIN.toIsoString())
        val out = StringBuilder("x")
        assertSame(out, NepaliDate.of(2081, 4, 15).appendShortString(out))
        assertSame(out, NepaliDate.of(2081, 4, 15).appendLongString(out, weekday = true))
        assertEquals("x2081/04/15Tuesday, Shrawan 15, 2081", out.toString())
    }
}
