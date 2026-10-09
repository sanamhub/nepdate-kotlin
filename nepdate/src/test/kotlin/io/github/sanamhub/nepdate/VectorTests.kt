package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals

private fun weekdayName(date: NepaliDate): String =
    date.dayOfWeek.name
        .lowercase()
        .replaceFirstChar { it.uppercase() }

class MonthBoundariesTest {
    @Test
    fun `every row of month-boundaries csv`() {
        val rows = SharedFiles.csv("spec/vectors/month-boundaries.csv")
        assertEquals(7176, rows.size)
        for (row in rows) {
            val (y, m, d) = SharedFiles.ymd(row.getValue("bs"))
            val (ay, am, ad) = SharedFiles.ymd(row.getValue("ad"))
            val date = NepaliDate.of(y, m, d)
            val context = row.toString()
            assertEquals(Triple(ay, am, ad), Triple(date.gregorianYear, date.gregorianMonth, date.gregorianDay), context)
            assertEquals(date, NepaliDate.ofGregorian(ay, am, ad), context)
            assertEquals(row.getValue("weekday"), weekdayName(date), context)
            assertEquals(row.getValue("month_length").toInt(), date.lengthOfMonth(), context)
        }
    }
}

class GoldenDatesTest {
    @Test
    fun `columns ad, weekday, day_of_year and month_length of dates tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/dates.tsv")
        assertEquals(1018, rows.size)
        for (row in rows) {
            val (y, m, d) = SharedFiles.ymd(row.getValue("bs"))
            val date = NepaliDate.of(y, m, d)
            val context = row.getValue("bs")
            assertEquals(SharedFiles.ymd(row.getValue("ad")), Triple(date.gregorianYear, date.gregorianMonth, date.gregorianDay), context)
            assertEquals(row.getValue("weekday"), weekdayName(date), context)
            assertEquals(row.getValue("day_of_year").toInt(), date.dayOfYear, context)
            assertEquals(row.getValue("month_length").toInt(), date.lengthOfMonth(), context)
        }
    }
}
