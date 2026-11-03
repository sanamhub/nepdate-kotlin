package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `day-diff.tsv`: the two directions of the day difference (PARITY D-07). The named
 * [NepaliDate.daysUntil] answers `b - a`; the `minus` operator keeps the C# direction, `a - b`.
 */
class DayDiffVectorTest {
    @Test
    fun `every row of day-diff tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/day-diff.tsv")
        assertEquals(16, rows.size)
        for (row in rows) {
            val a = SharedFiles.date(row.getValue("a"))
            val b = SharedFiles.date(row.getValue("b"))
            val context = row.toString()
            assertEquals(row.getValue("days_until").toLong(), a.daysUntil(b), context)
            val operatorMinus = a - b
            assertEquals(row.getValue("operator_minus").toLong(), operatorMinus.totalDays, context)
            assertEquals(operatorMinus.totalDays < 0, operatorMinus.isNegative, context)
            assertEquals(a.daysUntil(b), -(b.daysUntil(a)), context)
        }
    }

    @Test
    fun `minus keeps the C# sign and daysUntil keeps the opposite one`() {
        val earlier = NepaliDate.of(2081, 4, 15)
        val later = NepaliDate.of(2081, 4, 20)
        assertEquals(5L, earlier.daysUntil(later))
        assertEquals(-5L, later.daysUntil(earlier))
        assertEquals(5L, (later - earlier).totalDays)
        assertEquals(-5L, (earlier - later).totalDays)
        assertTrue((later - earlier).isNegative.not())
        assertTrue((earlier - later).isNegative)
    }

    @Test
    fun `minus carries the calendar distance too`() {
        val from = NepaliDate.of(2080, 1, 1)
        val to = NepaliDate.of(2081, 4, 15)
        val diff = to - from
        assertEquals(1, diff.years)
        assertEquals(3, diff.months)
        assertEquals(14, diff.days)
        assertEquals(473L, diff.totalDays)
        assertEquals(from.until(to), diff)
    }
}
