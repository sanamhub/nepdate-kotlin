package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * The String forms fill one array and the append forms write piece by piece. Both must give the
 * same text for every day, option and language.
 */
class StringFormsTest {
    private val total = NepaliDate.MIN.daysUntil(NepaliDate.MAX).toInt() + 1

    private fun nepali(ascii: String): String =
        buildString(ascii.length) {
            for (c in ascii) append(if (c in '0'..'9') '०' + (c - '0') else c)
        }

    @Test
    fun `the long form of every day matches the appended one`() {
        for (offset in 0 until total) {
            val date = NepaliDate.MIN.plusDays(offset.toLong())
            // The three flags as the bits of 0 to 7.
            for (bits in 0 until 8) {
                checkLong(date, bits and 1 != 0, bits and 2 != 0, bits and 4 != 0)
            }
        }
    }

    private fun checkLong(
        date: NepaliDate,
        weekday: Boolean,
        year: Boolean,
        pad: Boolean,
    ) {
        for (lang in Lang.entries) {
            val expected = date.appendLongString(StringBuilder(), weekday, year, pad, lang).toString()
            assertEquals(expected, date.toLongString(weekday, year, pad, lang))
        }
    }

    @Test
    fun `the short form of every day matches the appended one`() {
        val separators = Separator.entries
        for (offset in 0 until total step 3) {
            val date = NepaliDate.MIN.plusDays(offset.toLong())
            val separator = separators[offset % separators.size]
            for (order in DateOrder.entries) {
                checkShort(date, order, separator, pad = offset % 2 == 0)
            }
        }
    }

    private fun checkShort(
        date: NepaliDate,
        order: DateOrder,
        separator: Separator,
        pad: Boolean,
    ) {
        for (lang in Lang.entries) {
            val expected = date.appendShortString(StringBuilder(), order, separator, pad, lang).toString()
            assertEquals(expected, date.toShortString(order, separator, pad, lang))
        }
    }

    @Test
    fun `every day prints its padded numbers`() {
        for (offset in 0 until total) {
            val date = NepaliDate.MIN.plusDays(offset.toLong())
            val year = date.year.toString().padStart(4, '0')
            val month = date.monthValue.toString().padStart(2, '0')
            val day = date.dayOfMonth.toString().padStart(2, '0')
            assertEquals("$year/$month/$day", date.toString())
            assertEquals("$year-$month-$day", date.toIsoString())
            assertEquals(
                nepali("$day.$month.$year"),
                date.toShortString(DateOrder.DMY, Separator.DOT, true, Lang.NEPALI),
            )
            assertEquals(
                "${date.dayOfMonth} ${date.monthValue} ${date.year}",
                date.toShortString(DateOrder.DMY, Separator.SPACE, false),
            )
            assertEquals(
                "${date.dayOfWeek.displayName(Lang.NEPALI)}, ${date.month.displayName(Lang.NEPALI)} " +
                    "${nepali(day)}, ${nepali(year)}",
                date.toLongString(weekday = true, lang = Lang.NEPALI),
            )
            assertEquals(
                "${date.month.displayName()} ${date.dayOfMonth}",
                date.toLongString(year = false, pad = false),
            )
        }
    }

    @Test
    fun `every order places the three fields`() {
        val date = NepaliDate.of(2081, 4, 5)
        assertEquals("2081.04.05", date.toShortString(DateOrder.YMD, Separator.DOT))
        assertEquals("2081.05.04", date.toShortString(DateOrder.YDM, Separator.DOT))
        assertEquals("04.2081.05", date.toShortString(DateOrder.MYD, Separator.DOT))
        assertEquals("04.05.2081", date.toShortString(DateOrder.MDY, Separator.DOT))
        assertEquals("05.2081.04", date.toShortString(DateOrder.DYM, Separator.DOT))
        assertEquals("05.04.2081", date.toShortString(DateOrder.DMY, Separator.DOT))
        assertEquals("२०८१/४/५", date.toShortString(pad = false, lang = Lang.NEPALI))
        assertEquals("1901/9/10", NepaliDate.of(1901, 9, 10).toShortString(pad = false))
        assertEquals("2199/10/9", NepaliDate.of(2199, 10, 9).toShortString(pad = false))
    }
}
