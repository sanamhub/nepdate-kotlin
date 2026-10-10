package io.github.sanamhub.nepdate

import org.junit.jupiter.api.Assumptions.assumeTrue
import java.lang.management.ManagementFactory
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * Bytes per call for K3's hot paths (ADR-0002 section 7, ADR-0004): nothing for the append forms
 * into a builder with room, at most one 24-byte NepaliDate for a successful parse, nothing for
 * parseOrNull on invalid input. Same method as ConversionAllocationTest.
 */
class FormatAllocationTest {
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
    private val out = StringBuilder(256)

    @Test
    fun `appending into a builder with room allocates nothing`() {
        assertAtMost(0, "appendShortString") { i ->
            out.setLength(0)
            date.appendShortString(out, lang = if (i % 2 == 0) Lang.ENGLISH else Lang.NEPALI).length.toLong()
        }
        assertAtMost(0, "appendLongString") { i ->
            out.setLength(0)
            date.appendLongString(out, weekday = true, lang = if (i % 2 == 0) Lang.ENGLISH else Lang.NEPALI).length.toLong()
        }
        assertAtMost(0, "formatTo") { i ->
            out.setLength(0)
            date.formatTo(out, if (i % 2 == 0) "dd MMMM yyyy" else "yyyy'BS' \\d M", Lang.NEPALI).length.toLong()
        }
    }

    @Test
    fun `parsing valid text allocates only the date`() {
        assertAtMost(24, "parse") { i -> NepaliDate.parse(if (i % 2 == 0) "2081/04/15" else "२०८१-०४-१६").hashCode().toLong() }
        assertAtMost(24, "parseLenient") { i ->
            NepaliDate.parseLenient(if (i % 2 == 0) "15 Shrawan 2081" else "Shrawan 16, 2081 BS").hashCode().toLong()
        }
    }

    @Test
    fun `parseOrNull on invalid text builds no exception`() {
        assertAtMost(0, "parseOrNull") { i -> if (NepaliDate.parseOrNull(if (i % 2 == 0) "2081/04/33" else "2081/x") == null) 1L else 0L }
    }

    private companion object {
        const val WARM_UP = 10_000
        const val CALLS = 100_000
    }
}
