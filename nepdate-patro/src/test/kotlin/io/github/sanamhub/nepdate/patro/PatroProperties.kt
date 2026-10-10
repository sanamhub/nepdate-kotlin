package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.NepaliDate
import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import kotlin.test.assertEquals
import kotlin.test.assertTrue

// Seeds are fixed per property so a CI failure reproduces locally (ADR-0004).
private const val SEED = "20261010"

// Serials of NepaliDate.MIN and NepaliDate.MAX (ALGORITHM section 2).
private const val MAX_SERIAL = 109_211

class PatroProperties {
    private val holidays = Patro.publicHolidays.toSet()

    @Property(seed = SEED)
    fun `info never throws and matches the holiday list`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) serial: Int,
    ) {
        val date = dateAtSerial(serial)
        val info = Patro.info(date)
        assertEquals(date in holidays, info.isPublicHoliday)
        if (date !in Patro.coverage) {
            assertEquals(EMPTY_INFO, info)
            assertTrue(info.events.isEmpty())
        }
    }

    @Property(seed = SEED)
    fun `days near the coverage edges`(
        @ForAll @IntRange(min = -40, max = 40) step: Int,
    ) {
        for (edge in listOf(Patro.coverage.start, Patro.coverage.endInclusive)) {
            val date = edge.plusDays(step.toLong())
            val info = Patro.info(date)
            assertEquals(date in holidays, info.isPublicHoliday)
            if (date !in Patro.coverage) assertEquals(EMPTY_INFO, info)
        }
        assertEquals(NepaliDate.MAX, dateAtSerial(MAX_SERIAL))
    }
}
