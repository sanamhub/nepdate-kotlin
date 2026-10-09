package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MonthsArithmeticTest {
    private fun outOfRange(block: () -> Unit) =
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, assertFailsWith<NepDateException> { block() }.kind)

    private fun date(text: String): NepaliDate {
        val (y, m, d) = SharedFiles.ymd(text)
        return NepaliDate.of(y, m, d)
    }

    @Test
    fun `every row of add-months tsv, both columns`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/add-months.tsv")
        assertEquals(10180, rows.size)
        for (row in rows) {
            val start = date(row.getValue("bs"))
            val months = row.getValue("months").toLong()
            for ((column, overflow) in listOf("clamp" to MonthOverflow.CLAMP, "overflow" to MonthOverflow.SPILL)) {
                val expected = row.getValue(column)
                if (expected == "ERR") {
                    outOfRange { start.plusMonths(months, overflow) }
                } else {
                    assertEquals(date(expected), start.plusMonths(months, overflow), "$row $column")
                }
            }
        }
    }

    @Test
    fun `examples of ALGORITHM section 8`() {
        val start = NepaliDate.of(2081, 4, 32)
        assertEquals(NepaliDate.of(2081, 6, 30), start.plusMonths(2))
        assertEquals(NepaliDate.of(2081, 6, 30), start.plusMonths(2, MonthOverflow.CLAMP))
        assertEquals(NepaliDate.of(2081, 7, 2), start.plusMonths(2, MonthOverflow.SPILL))
        assertEquals(NepaliDate.of(2081, 9, 29), start.plusMonths(5))
        assertEquals(NepaliDate.of(2081, 10, 3), start.plusMonths(5, MonthOverflow.SPILL))
        assertEquals(NepaliDate.of(2081, 4, 30), NepaliDate.of(2081, 6, 30).plusMonths(-2))
    }

    @Test
    fun `years are twelve months`() {
        val start = NepaliDate.of(2081, 4, 32)
        assertEquals(start.plusMonths(24), start.plusYears(2))
        assertEquals(start.plusMonths(-12, MonthOverflow.SPILL), start.plusYears(-1, MonthOverflow.SPILL))
        assertEquals(NepaliDate.of(2199, 1, 1), NepaliDate.of(1901, 1, 1).plusYears(298))
    }

    @Test
    fun `amounts outside the range throw before multiplying`() {
        outOfRange { NepaliDate.MIN.plusMonths(3588) }
        outOfRange { NepaliDate.MAX.plusMonths(1) }
        outOfRange { NepaliDate.MIN.plusMonths(-1) }
        outOfRange { NepaliDate.MIN.plusMonths(Long.MAX_VALUE) }
        outOfRange { NepaliDate.MIN.plusMonths(Long.MIN_VALUE) }
        outOfRange { NepaliDate.MIN.plusYears(Long.MAX_VALUE) }
        outOfRange { NepaliDate.MIN.plusYears(Long.MAX_VALUE / 12 + 1) }
        outOfRange { NepaliDate.MIN.plusYears(300) }
        outOfRange { NepaliDate.of(2199, 12, 1).plusMonths(0, MonthOverflow.SPILL).plusMonths(1) }
        assertEquals(NepaliDate.of(2199, 12, 1), NepaliDate.MIN.plusMonths(3587))
    }

    @Test
    fun `spilling past the last month throws`() {
        // 2199-05 has 32 days and 2199-12 has 30, so day 32 spills into BS 2200-01.
        assertEquals(32, NepaliDate.of(2199, 5, 1).lengthOfMonth())
        assertEquals(NepaliDate.MAX, NepaliDate.of(2199, 5, 32).plusMonths(7))
        outOfRange { NepaliDate.of(2199, 5, 32).plusMonths(7, MonthOverflow.SPILL) }
    }
}
