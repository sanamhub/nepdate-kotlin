package io.github.sanamhub.nepdate

import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** K4-07: the `java.time` interop file, PORT-PLAN phase 4 acceptance criteria. */
class JavaTimeTest {
    @Test
    fun `today at a fixed instant differs between UTC and Kathmandu`() {
        val instant = Instant.parse("2024-07-30T18:30:00Z")
        assertEquals(NepaliDate.of(2081, 4, 15), nepaliToday(Clock.fixed(instant, ZoneOffset.UTC)))
        assertEquals(NepaliDate.of(2081, 4, 16), nepaliToday(Clock.fixed(instant, KATHMANDU)))
    }

    @Test
    fun `today around the New York DST changes equals LocalDate now`() {
        val newYork = ZoneId.of("America/New_York")
        for (instant in listOf("2024-03-10T06:30:00Z", "2024-11-03T05:30:00Z")) {
            val clock = Clock.fixed(Instant.parse(instant), newYork)
            assertEquals(LocalDate.now(clock).toNepaliDate(), nepaliToday(clock))
        }
    }

    @Test
    fun `KATHMANDU is Asia Kathmandu on the CI JDKs`() {
        assertEquals("Asia/Kathmandu", KATHMANDU.id)
    }

    @Test
    fun `dates and clocks just outside the supported range throw OUT_OF_RANGE`() {
        val early = assertFailsWith<NepDateException> { LocalDate.of(1844, 4, 10).toNepaliDate() }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, early.kind)
        val late = assertFailsWith<NepDateException> { LocalDate.of(2143, 4, 16).toNepaliDate() }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, late.kind)
        val clock = Clock.fixed(Instant.parse("2143-04-16T00:00:00Z"), ZoneOffset.UTC)
        val today = assertFailsWith<NepDateException> { nepaliToday(clock) }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, today.kind)
    }

    @Test
    fun `round trip for every supported serial`() {
        for (serial in 0..109_211) {
            val date = NepaliDate.ofEpochDay((serial - 45920).toLong())
            assertEquals(date, date.toLocalDate().toNepaliDate())
        }
    }
}
