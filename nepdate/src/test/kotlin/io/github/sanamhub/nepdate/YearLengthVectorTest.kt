package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `year-length.tsv`: the length of every BS year (PARITY D-12). C# has no year-length API, so the
 * file is derived from the month lengths in `shared/data/`, not recorded from C#.
 */
class YearLengthVectorTest {
    @Test
    fun `every row of year-length tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/year-length.tsv")
        assertEquals(299, rows.size)
        for (row in rows) {
            val year = row.getValue("bs_year").toInt()
            val expected = row.getValue("year_length").toInt()
            assertEquals(expected, NepaliDate.of(year, 1, 1).yearLength(), "BS $year")
            // The year length is the sum of the twelve month lengths, which is also the size of
            // the range of the whole year.
            val sum = (1..12).sumOf { month -> NepaliDate.of(year, month, 1).lengthOfMonth() }
            assertEquals(sum, NepaliDate.of(year, 6, 1).yearLength(), "BS $year")
            assertEquals(sum, NepaliDateRange.ofYear(year).size, "BS $year")
        }
    }

    @Test
    fun `year length is 364 to 367 for every supported year`() {
        for (year in 1901..2199) {
            val length = NepaliDate.of(year, 1, 1).yearLength()
            assertTrue(length in 364..367, "BS $year has $length days")
            assertEquals(NepaliDateRange.ofYear(year).size, length, "BS $year")
        }
    }

    @Test
    fun `every month of a BS year reports the same year length`() {
        assertEquals(366, NepaliDate.of(2081, 1, 1).yearLength())
        // D-12 replaces the C# IsLeapYear, which followed the Gregorian year of the date and so
        // changed in the middle of a BS year.
        val lengths = (1..12).map { month -> NepaliDate.of(2081, month, 1).yearLength() }.toSet()
        assertEquals(setOf(366), lengths)
        assertEquals(366, NepaliDateRange.ofYear(2081).size)
    }
}
