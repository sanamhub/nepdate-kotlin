package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * `range-splits.tsv`: one piece per month and per fiscal quarter, clipped at the range ends. A
 * range that reaches 2199-12-30 returns every piece (PARITY D-16) instead of advancing past it.
 */
class RangeSplitVectorTest {
    @Test
    fun `every row of range-splits tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/range-splits.tsv")
        assertEquals(9, rows.size)
        for (row in rows) {
            val range = SharedFiles.range("${row.getValue("start")}..${row.getValue("end")}")
            val expected = SharedFiles.ranges(row.getValue("result"))
            val actual =
                when (row.getValue("operation")) {
                    "split_by_month" -> range.splitByMonth()
                    "split_by_fiscal_quarter" -> range.splitByFiscalQuarter()
                    else -> error("unknown operation in $row")
                }
            assertEquals(expected, actual, row.toString())
            // The pieces cover the range without a gap or an overlap, and the last one ends at the end.
            assertEquals(range.start, actual.first().start, row.toString())
            assertEquals(range.endInclusive, actual.last().endInclusive, row.toString())
            assertEquals(range.size, actual.sumOf { it.size }, row.toString())
        }
    }

    @Test
    fun `the last month and quarter of the supported range are split`() {
        val last = NepaliDateRange.of(NepaliDate.of(2199, 11, 30), NepaliDate.MAX)
        assertEquals(2, last.splitByMonth().size)
        assertEquals(NepaliDate.MAX, last.splitByMonth().last().endInclusive)
        val quarter = NepaliDateRange.of(NepaliDate.of(2199, 12, 25), NepaliDate.MAX)
        assertEquals(1, quarter.splitByFiscalQuarter().size)
        assertEquals(NepaliDate.MAX, quarter.splitByFiscalQuarter().last().endInclusive)
        // A whole year needs twelve month pieces and four quarter pieces.
        assertEquals(12, NepaliDateRange.ofYear(2081).splitByMonth().size)
        assertEquals(4, NepaliDateRange.ofYear(2081).splitByFiscalQuarter().size)
    }
}
