package io.github.sanamhub.nepdate

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Bytes per call for K2's hot paths (ADR-0002 section 7, ADR-0004): at most one 24-byte NepaliDate
 * where a date is returned, nothing for daysUntil, contains and size. Same method as
 * ConversionAllocationTest.
 */
class ArithmeticAllocationTest {
    // A fun interface with primitive types: a Kotlin (Int) -> Long lambda would box both values.
    fun interface Call {
        fun run(i: Int): Long
    }

    private val bean = ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean

    // Results are summed into this field so the JIT can't drop the calls as dead code.
    private var sink = 0L

    private fun assertAtMost(
        limit: Int,
        name: String,
        call: Call,
    ) {
        val mx = bean
        assumeTrue(mx != null && mx.isThreadAllocatedMemorySupported, "com.sun.management.ThreadMXBean is missing")
        mx!!.isThreadAllocatedMemoryEnabled = true
        repeat(WARM_UP) { sink += call.run(it) }
        val thread = Thread.currentThread().id
        val before = mx.getThreadAllocatedBytes(thread)
        for (i in 0 until CALLS) sink += call.run(i)
        val bytes = (mx.getThreadAllocatedBytes(thread) - before).toDouble() / CALLS
        // Less than one byte over the limit is the counter's own overhead spread over the calls.
        assertTrue(bytes < limit + 1.0, "$name allocates %.3f bytes per call, limit $limit".format(bytes))
    }

    private val date = NepaliDate.of(2081, 4, 15)
    private val other = NepaliDate.of(2090, 1, 1)
    private val range = NepaliDateRange.ofYear(2081)

    @Test
    fun `operations that return a date allocate only that date`() {
        assertAtMost(24, "plusDays") { i -> date.plusDays((i % 1000).toLong()).hashCode().toLong() }
        assertAtMost(24, "plusMonths") { i -> date.plusMonths((i % 100).toLong()).hashCode().toLong() }
        assertAtMost(24, "plusYears") { i -> date.plusYears((i % 100).toLong()).hashCode().toLong() }
        assertAtMost(24, "NepaliDateRange.get") { i -> range[i % 366].hashCode().toLong() }
    }

    @Test
    fun `counting and membership allocate nothing`() {
        assertAtMost(0, "daysUntil") { i -> date.daysUntil(other) + i }
        assertAtMost(0, "NepaliDateRange.contains") { i -> if (range.contains(if (i % 2 == 0) date else other)) 1L else 0L }
        assertAtMost(0, "NepaliDateRange.size") { i -> (range.size + i).toLong() }
    }

    private companion object {
        const val WARM_UP = 10_000
        const val CALLS = 100_000
    }
}
