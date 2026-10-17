package io.github.sanamhub.nepdate.patro

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.assertTrue

/** Patro lookups allocate nothing (ADR-0002 section 7, ADR-0004), on the 64 B7 dates of BENCHMARK.md section 3. */
class PatroAllocationTest {
    // A fun interface with primitive types: a Kotlin (Int) -> Long lambda would box both values.
    fun interface Call {
        fun run(i: Int): Long
    }

    private val bean = ManagementFactory.getThreadMXBean() as? com.sun.management.ThreadMXBean

    // Results are summed into this field so the JIT can't drop the calls as dead code.
    private var sink = 0L

    private val dates = b7Dates().toTypedArray()

    private fun assertNoAllocation(
        name: String,
        call: Call,
    ) {
        val mx = bean
        assumeTrue(mx != null && mx.isThreadAllocatedMemorySupported, "com.sun.management.ThreadMXBean is missing")
        mx!!.isThreadAllocatedMemoryEnabled = true
        repeat(WARM_UP) { sink += call.run(it) }
        // getId() is deprecated since Java 19, but the tests also run on Java 11 (no threadId()).
        @Suppress("DEPRECATION")
        val thread = Thread.currentThread().id
        val before = mx.getThreadAllocatedBytes(thread)
        for (i in 0 until CALLS) sink += call.run(i)
        val bytes = (mx.getThreadAllocatedBytes(thread) - before).toDouble() / CALLS
        // Less than one byte is the counter's own overhead spread over the calls (as in :nepdate).
        assertTrue(bytes < 1.0, "$name allocates %.3f bytes per call, limit 0".format(bytes))
    }

    @Test
    fun `lookups allocate nothing`() {
        assertNoAllocation("Patro.info") { i -> if (Patro.info(dates[i and 63]).isPublicHoliday) 1L else 0L }
        assertNoAllocation("calendarInfo") { i -> if (dates[i and 63].calendarInfo.tithi == null) 1L else 0L }
        assertNoAllocation("events") { i -> System.identityHashCode(Patro.info(dates[i and 63]).events).toLong() and 1L }
        assertNoAllocation("events.size") { i ->
            Patro
                .info(dates[i and 63])
                .events.size
                .toLong()
        }
        assertNoAllocation("events[i]") { i ->
            val events = Patro.info(dates[i and 63]).events
            if (events.isEmpty()) 0L else events[i % events.size].english.length.toLong()
        }
    }

    private companion object {
        const val WARM_UP = 20_000
        const val CALLS = 200_000
    }
}
