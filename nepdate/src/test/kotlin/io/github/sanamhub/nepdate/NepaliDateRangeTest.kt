package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NepaliDateRangeTest {
    private fun kindOf(block: () -> Unit) = assertFailsWith<NepDateException> { block() }.kind

    @Test
    fun `every year's size is the sum of its months and lies in 364 to 367`() {
        for (year in MIN_YEAR..MAX_YEAR) {
            val range = NepaliDateRange.ofYear(year)
            val sum = (1..12).sumOf { NepaliDate.of(year, it, 1).lengthOfMonth() }
            assertEquals(sum, range.size, "BS $year")
            assertTrue(range.size in 364..367, "BS $year has ${range.size} days")
            assertEquals(NepaliDate.of(year, 1, 1), range.start)
            assertEquals(NepaliDate.of(year, 12, 1).lastDayOfMonth(), range.endInclusive)
        }
    }

    @Test
    fun `month range`() {
        val range = NepaliDateRange.ofMonth(2081, 4)
        assertEquals(32, range.size)
        assertEquals(NepaliDate.of(2081, 4, 32), range.last())
        assertEquals(NepaliDate.of(2081, 4, 1), range.first())
        assertEquals(NepaliDate.of(2081, 4, 15), range[14])
        assertEquals(14, range.indexOf(NepaliDate.of(2081, 4, 15)))
        assertEquals(14, range.lastIndexOf(NepaliDate.of(2081, 4, 15)))
        assertEquals(-1, range.indexOf(NepaliDate.of(2081, 5, 1)))
    }

    @Test
    fun `contains is a range check`() {
        val year = NepaliDateRange.ofYear(2081)
        assertFalse(year.contains(NepaliDate.MAX))
        assertTrue(year.contains(NepaliDate.of(2081, 12, 1)))
        assertFalse(NepaliDate.of(2080, 12, 30) in year)
    }

    @Test
    fun `get outside the list throws IndexOutOfBoundsException`() {
        val range = NepaliDateRange.ofMonth(2081, 4)
        assertFailsWith<IndexOutOfBoundsException> { range[32] }
        assertFailsWith<IndexOutOfBoundsException> { range[-1] }
    }

    @Test
    fun `factories validate`() {
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDateRange.of(NepaliDate.MAX, NepaliDate.MIN) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDateRange.ofYear(1900) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDateRange.ofYear(2200) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDateRange.ofMonth(2200, 13) })
        assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf { NepaliDateRange.ofMonth(2081, 13) })
        assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf { NepaliDateRange.ofMonth(2081, 0) })
        assertEquals(1, NepaliDateRange.of(NepaliDate.MIN, NepaliDate.MIN).size)
        assertEquals(109212, NepaliDateRange.of(NepaliDate.MIN, NepaliDate.MAX).size)
    }

    @Test
    fun `a range is a list of its days`() {
        val range = NepaliDateRange.of(NepaliDate.of(2081, 4, 31), NepaliDate.of(2081, 5, 2))
        assertEquals(
            listOf(NepaliDate.of(2081, 4, 31), NepaliDate.of(2081, 4, 32), NepaliDate.of(2081, 5, 1), NepaliDate.of(2081, 5, 2)),
            range.toList(),
        )
        assertEquals(range, NepaliDateRange.of(NepaliDate.of(2081, 4, 31), NepaliDate.of(2081, 5, 2)))
    }
}
