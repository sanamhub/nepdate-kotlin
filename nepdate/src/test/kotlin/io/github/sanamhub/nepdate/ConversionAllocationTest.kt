package io.github.sanamhub.nepdate

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Bytes allocated per call on the hot paths (ADR-0002 section 7, ADR-0004): at most one 24-byte
 * NepaliDate where a date is returned, nothing where a field is read.
 */
class ConversionAllocationTest {
    // A fun interface with primitive types: a Kotlin (Int) -> Long lambda would box both values.
    fun interface Call {
        fun run(i: Int): Long
    }

    private val bean = ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean

    // Results are summed into this field so the JIT can't drop the calls as dead code.
    private var sink = 0L

    private fun bytesPerCall(call: Call): Double {
        val mx = bean
        assumeTrue(mx != null && mx.isThreadAllocatedMemorySupported, "com.sun.management.ThreadMXBean is missing")
        mx!!.isThreadAllocatedMemoryEnabled = true
        repeat(WARM_UP) { sink += call.run(it) }
        val thread = Thread.currentThread().id
        val before = mx.getThreadAllocatedBytes(thread)
        for (i in 0 until CALLS) sink += call.run(i)
        val after = mx.getThreadAllocatedBytes(thread)
        return (after - before).toDouble() / CALLS
    }

    private fun assertAtMost(
        limit: Int,
        name: String,
        call: Call,
    ) {
        val bytes = bytesPerCall(call)
        // Less than one byte over the limit is the counter's own overhead spread over the calls;
        // one more object per call would add at least 16 bytes.
        val ok = bytes < limit + 1.0
        assertTrue(ok, "$name allocates %.3f bytes per call, limit $limit".format(bytes))
    }

    private val date = NepaliDate.of(2081, 4, 15)

    @Test
    fun `factories allocate only the returned date`() {
        assertAtMost(24, "of") { i -> NepaliDate.of(2081, 4, 1 + i % 32).hashCode().toLong() }
        assertAtMost(24, "ofGregorian") { i -> NepaliDate.ofGregorian(2024, 7, 1 + i % 31).hashCode().toLong() }
        assertAtMost(24, "ofEpochDay") { i -> NepaliDate.ofEpochDay((i % 50000).toLong()).hashCode().toLong() }
    }

    @Test
    fun `reading fields allocates nothing`() {
        assertAtMost(0, "year") { i -> (date.year + i).toLong() }
        assertAtMost(0, "gregorianYear") { i -> (date.gregorianYear + i).toLong() }
        assertAtMost(0, "dayOfWeek") { i -> (date.dayOfWeek.ordinal + i).toLong() }
        assertAtMost(0, "lengthOfMonth") { i -> (date.lengthOfMonth() + i).toLong() }
        assertAtMost(0, "toEpochDay") { i -> date.toEpochDay() + i }
    }

    @Test
    fun `ofOrNull on an invalid day builds no exception`() {
        assertAtMost(0, "ofOrNull invalid") { i -> if (NepaliDate.ofOrNull(2081, 4, 33 + i % 2) == null) 1L else 0L }
    }

    private companion object {
        const val WARM_UP = 10_000
        const val CALLS = 100_000
    }
}
