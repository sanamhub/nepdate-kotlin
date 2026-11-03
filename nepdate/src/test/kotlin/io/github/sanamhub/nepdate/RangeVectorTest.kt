package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * `ranges.tsv`: construction, bounds, length, containment of a date and of a range, overlap,
 * adjacency, intersection, union, difference, equality, both string forms and iteration.
 *
 * A range is never empty (PARITY D-01), so a start after the end is an error and a disjoint
 * intersection is null. No operation throws at 2199-12-30 (PARITY D-16).
 */
class RangeVectorTest {
    @Test
    fun `every row of ranges tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/ranges.tsv")
        assertEquals(54, rows.size)
        for (row in rows) {
            val start = row.getValue("start")
            val end = row.getValue("end")
            if (row.getValue("operation") == "construct") {
                assertConstruct(row)
                continue
            }
            val range = SharedFiles.range("$start..$end")
            assertEquals(SharedFiles.date(start), range.start, row.toString())
            assertEquals(SharedFiles.date(end), range.endInclusive, row.toString())
            assertOperation(row, range)
        }
    }

    /** The construct rows: C# stores the bounds, the port refuses a start after the end (D-01). */
    private fun assertConstruct(row: Map<String, String>) {
        val context = row.toString()
        val build = {
            NepaliDateRange.of(SharedFiles.date(row.getValue("start")), SharedFiles.date(row.getValue("end")))
        }
        if (row.getValue("result") == "ERR") {
            // The error can come from either bound being outside the supported range, or from a
            // start after the end.
            assertFailsWith<NepDateException>(context) { build() }
        } else {
            assertEquals(SharedFiles.range(row.getValue("result")), build(), context)
        }
    }

    private fun assertOperation(
        row: Map<String, String>,
        range: NepaliDateRange,
    ) {
        val context = row.toString()
        val argument = row.getValue("argument")
        val expected = row.getValue("result")
        when (row.getValue("operation")) {
            "length" -> assertEquals(expected.toInt(), range.size, context)
            "contains_date" -> assertEquals(expected.toBooleanStrict(), range.contains(SharedFiles.date(argument)), context)
            "contains_range" -> assertEquals(expected.toBooleanStrict(), range.containsRange(SharedFiles.range(argument)), context)
            "overlaps" -> assertEquals(expected.toBooleanStrict(), range.overlaps(SharedFiles.range(argument)), context)
            "is_adjacent_to" -> assertEquals(expected.toBooleanStrict(), range.isAdjacentTo(SharedFiles.range(argument)), context)
            "intersect" ->
                if (expected == "none") {
                    assertNull(range.intersect(SharedFiles.range(argument)), context)
                } else {
                    assertEquals(SharedFiles.range(expected), range.intersect(SharedFiles.range(argument)), context)
                }
            "union" -> assertEquals(SharedFiles.range(expected), range.union(SharedFiles.range(argument)), context)
            "except" -> assertEquals(SharedFiles.ranges(expected), range.minus(SharedFiles.range(argument)), context)
            "equals" -> assertEquals(expected.toBooleanStrict(), range == SharedFiles.range(argument), context)
            "to_string" -> assertEquals(expected, range.toString(), context)
            "enumerate" -> assertEquals(SharedFiles.dates(expected), range.toList(), context)
            else -> error("unknown operation in $context")
        }
    }

    @Test
    fun `equality and hashing describe the bounds`() {
        val range = NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20))
        assertEquals(range, NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20)))
        assertEquals(range.hashCode(), NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20)).hashCode())
        assertFalse(range.equals("2081-04-10..2081-04-20"))
        // A list of the same days is not the same value: a range is its two bounds.
        assertFalse(range.equals(range.toList()))
    }

    @Test
    fun `a range iterates every day and is a list`() {
        val range = NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 12))
        assertEquals(listOf(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 11), NepaliDate.of(2081, 4, 12)), range.toList())
        assertEquals(1, range.indexOf(NepaliDate.of(2081, 4, 11)))
        assertEquals(-1, range.indexOf(NepaliDate.of(2081, 5, 1)))
        assertEquals(1, range.lastIndexOf(NepaliDate.of(2081, 4, 11)))
    }

    @Test
    fun `the range operator builds the same range`() {
        val start = NepaliDate.of(2081, 4, 10)
        val end = NepaliDate.of(2081, 4, 20)
        assertEquals(NepaliDateRange.of(start, end), start..end)
        assertEquals(11, (start..end).size)
        assertTrue(NepaliDate.of(2081, 4, 15) in start..end)
        assertFailsWith<NepDateException> { end..start }
    }

    @Test
    fun `adjacency answers false at the data ends instead of throwing`() {
        val atMax = NepaliDateRange.of(NepaliDate.of(2199, 12, 25), NepaliDate.MAX)
        val earlier = NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20))
        assertFalse(atMax.isAdjacentTo(earlier))
        assertFalse(earlier.isAdjacentTo(atMax))
        val atMin = NepaliDateRange.of(NepaliDate.MIN, NepaliDate.of(1901, 1, 5))
        assertFalse(atMin.isAdjacentTo(earlier))
        assertFalse(earlier.isAdjacentTo(atMin))
    }

    @Test
    fun `union keeps the smaller bounds and intersect is null when disjoint`() {
        val first = NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 20))
        val second = NepaliDateRange.of(NepaliDate.of(2081, 4, 15), NepaliDate.of(2081, 4, 25))
        assertEquals(NepaliDateRange.of(NepaliDate.of(2081, 4, 10), NepaliDate.of(2081, 4, 25)), first.union(second))
        assertNull(first.intersect(NepaliDateRange.of(NepaliDate.of(2081, 5, 1), NepaliDate.of(2081, 5, 5))))
    }
}
