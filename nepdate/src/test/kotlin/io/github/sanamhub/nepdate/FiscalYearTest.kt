package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class FiscalYearTest {
    private fun outOfRange(block: () -> Unit) =
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, assertFailsWith<NepDateException> { block() }.kind)

    private fun golden(
        expected: String,
        actual: () -> NepaliDate,
        context: String,
    ) {
        if (expected == "ERR") {
            outOfRange { actual() }
        } else {
            val (y, m, d) = SharedFiles.ymd(expected)
            assertEquals(NepaliDate.of(y, m, d), actual(), context)
        }
    }

    @Test
    fun `dates tsv fiscal year and quarter columns`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/dates.tsv")
        assertEquals(1018, rows.size)
        for (row in rows) {
            val (y, m, d) = SharedFiles.ymd(row.getValue("bs"))
            val date = NepaliDate.of(y, m, d)
            val fy = FiscalYear.containing(date)
            assertEquals(fy, date.fiscalYear)
            golden(row.getValue("fy_start"), { fy.start() }, "${row["bs"]} fy_start")
            golden(row.getValue("fy_end"), { fy.end() }, "${row["bs"]} fy_end")
            golden(row.getValue("quarter_start"), { fy.range(date.quarter).start }, "${row["bs"]} quarter_start")
            golden(row.getValue("quarter_end"), { fy.range(date.quarter).endInclusive }, "${row["bs"]} quarter_end")
        }
    }

    @Test
    fun `labels in English and Nepali`() {
        assertEquals("2082/83", FiscalYear(2082).label())
        assertEquals("2082/83", FiscalYear(2082).label(Lang.ENGLISH))
        assertEquals("२०८२/८३", FiscalYear(2082).label(Lang.NEPALI))
        assertEquals("2099/00", FiscalYear(2099).label())
        assertEquals("2082/83", FiscalYear(2082).toString())
    }

    @Test
    fun `only results outside the range throw`() {
        outOfRange { FiscalYear(2199).end() }
        outOfRange { FiscalYear(1900).start() }
        outOfRange { FiscalYear(1900).range() }
        val q4 = FiscalYear(1900).range(Quarter.Q4)
        assertEquals(NepaliDate.of(1901, 1, 1), q4.start)
        assertEquals(NepaliDate.of(1901, 3, 32), q4.endInclusive)
        assertEquals(FiscalYear(Int.MAX_VALUE), FiscalYear(Int.MAX_VALUE))
        outOfRange { FiscalYear(Int.MAX_VALUE).end() }
    }

    @Test
    fun `fiscal year runs from Shrawan to Ashad`() {
        assertEquals(FiscalYear(2081), NepaliDate.of(2081, 4, 1).fiscalYear)
        assertEquals(FiscalYear(2080), NepaliDate.of(2081, 3, 31).fiscalYear)
        val range = FiscalYear(2081).range()
        assertEquals(NepaliDate.of(2081, 4, 1), range.start)
        assertEquals(NepaliDate.of(2082, 3, 1).lastDayOfMonth(), range.endInclusive)
        assertTrue(FiscalYear(2080) < FiscalYear(2081))
        assertNotEquals(FiscalYear(2080), FiscalYear(2081))
        assertEquals(FiscalYear(2080).hashCode(), FiscalYear(2080).hashCode())
    }

    @Test
    fun `quarters and their months`() {
        assertEquals(listOf(Month.SHRAWAN, Month.BHADRA, Month.ASHOJ), Quarter.Q1.months)
        assertEquals(listOf(Month.KARTIK, Month.MANGSIR, Month.POUSH), Quarter.Q2.months)
        assertEquals(listOf(Month.MAGH, Month.FALGUN, Month.CHAITRA), Quarter.Q3.months)
        assertEquals(listOf(Month.BAISHAKH, Month.JESTHA, Month.ASHAD), Quarter.Q4.months)
        for (month in Month.entries) {
            val quarter = NepaliDate.of(2081, month.value, 1).quarter
            assertTrue(month in quarter.months, "$month in $quarter")
        }
        assertEquals(NepaliDate.of(2082, 1, 1), FiscalYear(2081).range(Quarter.Q4).start)
    }
}
