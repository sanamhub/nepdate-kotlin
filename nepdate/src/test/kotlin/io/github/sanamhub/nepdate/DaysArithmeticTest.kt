package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DaysArithmeticTest {
    private fun outOfRange(block: () -> Unit) =
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, assertFailsWith<NepDateException> { block() }.kind)

    @Test
    fun `every row of add-days tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/add-days.tsv")
        assertEquals(9162, rows.size)
        for (row in rows) {
            val (y, m, d) = SharedFiles.ymd(row.getValue("bs"))
            val date = NepaliDate.of(y, m, d)
            val days = row.getValue("days").toLong()
            val expected = row.getValue("result")
            if (expected == "ERR") {
                outOfRange { date.plusDays(days) }
            } else {
                val (ey, em, ed) = SharedFiles.ymd(expected)
                assertEquals(NepaliDate.of(ey, em, ed), date.plusDays(days), row.toString())
                assertEquals(date, NepaliDate.of(ey, em, ed).minusDays(days), row.toString())
            }
        }
    }

    @Test
    fun `stepping past either end throws OUT_OF_RANGE`() {
        outOfRange { NepaliDate.MAX.plusDays(1) }
        outOfRange { NepaliDate.MIN.minusDays(1) }
        outOfRange { NepaliDate.MIN.plusDays(Long.MAX_VALUE) }
        outOfRange { NepaliDate.MAX.plusDays(Long.MIN_VALUE) }
        outOfRange { NepaliDate.MAX.minusDays(Long.MIN_VALUE) }
        outOfRange { NepaliDate.MIN.plusDays(109212) }
        outOfRange { NepaliDate.MIN.plusDays(Int.MAX_VALUE + 1L) }
        assertEquals(NepaliDate.MAX, NepaliDate.MIN.plusDays(109211))
        assertEquals(NepaliDate.MIN, NepaliDate.MAX.minusDays(109211))
    }

    @Test
    fun `days until is signed`() {
        assertEquals(109211L, NepaliDate.MIN.daysUntil(NepaliDate.MAX))
        assertEquals(-109211L, NepaliDate.MAX.daysUntil(NepaliDate.MIN))
        assertEquals(0L, NepaliDate.MIN.daysUntil(NepaliDate.MIN))
        assertEquals(1L, NepaliDate.of(2081, 4, 32).daysUntil(NepaliDate.of(2081, 5, 1)))
    }
}
