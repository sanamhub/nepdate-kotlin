package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/** The decoded tables and the three queries (PATRO sections 2 to 4). */
class PatroTest {
    private val info = PatroTables.infoByDay

    @Test
    fun `decoded tables hold the PATRO section 3 counts`() {
        assertEquals(2793, info.size)
        assertEquals(64285, BASE_SERIAL)
        assertEquals(2190, info.count { it.tithi != null })
        assertEquals(294, info.count { it.isPublicHoliday })
        assertEquals(1671, info.count { it.events.isNotEmpty() })
        assertEquals(3032, info.sumOf { it.events.size })
        assertEquals(753, PatroTables.pairs.size)
        assertEquals(753, PatroTables.pairs.toSet().size)
        assertEquals(3032, PatroTables.events.size)
    }

    @Test
    fun `days share CalendarInfo objects`() {
        val distinct = Collections.newSetFromMap(IdentityHashMap<CalendarInfo, Boolean>())
        distinct.addAll(info)
        assertTrue(distinct.size <= 1671 + 294 + 32 + 1, "${distinct.size} distinct instances")
        val tithiOnly = info.filter { it.tithi != null && !it.isPublicHoliday && it.events.isEmpty() }
        assertTrue(tithiOnly.isNotEmpty())
        for (day in tithiOnly) assertSame(tithiOnly.first { it.tithi === day.tithi }, day)
        assertTrue(info.filter { it.tithi == null && !it.isPublicHoliday && it.events.isEmpty() }.all { it === EMPTY_INFO })
    }

    @Test
    fun `events share the pair objects and the tithi range is contiguous`() {
        val pairs = Collections.newSetFromMap(IdentityHashMap<Named, Boolean>())
        pairs.addAll(PatroTables.pairs)
        assertTrue(PatroTables.events.all { it in pairs })
        val withTithi = info.indices.filter { info[it].tithi != null }
        assertEquals(TITHI_FIRST_OFFSET, withTithi.first())
        assertEquals((withTithi.first()..withTithi.last()).toList(), withTithi)
        assertEquals(serialOf(d(2078, 1, 2)) - BASE_SERIAL, TITHI_FIRST_OFFSET)
        assertEquals(serialOf(d(2083, 12, 30)) - BASE_SERIAL, withTithi.last())
    }

    @Test
    fun `every PATRO section 4 anchor matches`() {
        for (anchor in ANCHORS) assertNull(anchorMismatch(anchor, Patro.info(anchor.date)))
    }

    @Test
    fun `anchor names in Nepali`() {
        val newYear = Patro.info(d(2081, 1, 1))
        assertEquals("पञ्चमी", newYear.tithi?.name(Lang.NEPALI))
        assertEquals("Panchami", newYear.tithi?.name())
        assertEquals("नयाँ वर्ष", newYear.events[0].nepali)
    }

    @Test
    fun `coverage is 2077-01-01 to 2084-08-20`() {
        assertEquals(d(2077, 1, 1), Patro.coverage.start)
        assertEquals(d(2084, 8, 20), Patro.coverage.endInclusive)
        assertEquals(SPAN, Patro.coverage.size)
        assertSame(Patro.coverage, Patro.coverage)
    }

    @Test
    fun `public holidays ascend from 2077-01-01 to 2084-08-18`() {
        val holidays = Patro.publicHolidays
        assertEquals(294, holidays.size)
        assertEquals(d(2077, 1, 1), holidays.first())
        assertEquals(d(2084, 8, 18), holidays.last())
        assertEquals(holidays.sorted(), holidays)
        assertEquals(holidays.toSet().size, holidays.size)
        assertSame(holidays, Patro.publicHolidays)
        assertTrue(holidays.all { Patro.info(it).isPublicHoliday })
        @Suppress("UNCHECKED_CAST")
        val mutable = holidays as MutableList<NepaliDate>
        assertFailsWith<UnsupportedOperationException> { mutable.add(NepaliDate.MIN) }
    }

    @Test
    fun `MIN and MAX have no data and don't throw`() {
        for (date in listOf(NepaliDate.MIN, NepaliDate.MAX)) {
            val empty = Patro.info(date)
            assertNull(empty.tithi)
            assertFalse(empty.isPublicHoliday)
            assertTrue(empty.events.isEmpty())
            assertEquals(EMPTY_INFO, empty)
        }
    }

    @Test
    fun `the same day gives the same instance`() {
        for (date in spanDays()) {
            assertSame(Patro.info(date), Patro.info(NepaliDate.ofEpochDay(date.toEpochDay())))
            assertSame(Patro.info(date), date.calendarInfo)
        }
    }

    @Test
    fun `events can't be modified`() {
        val events = Patro.info(d(2081, 1, 1)).events
        val other = Patro.info(d(2081, 1, 2)).events
        for (list in listOf(events, other)) {
            @Suppress("UNCHECKED_CAST")
            val mutable = list as MutableList<Named>
            assertFailsWith<UnsupportedOperationException> { mutable.add(events[0]) }
        }
        assertFailsWith<IndexOutOfBoundsException> { events[3] }
        assertFailsWith<IndexOutOfBoundsException> { events[-1] }
        assertEquals(listOf("New Year", "Mesh Sankranti", "Bisket Jatra"), events.map { it.english })
    }

    @Test
    fun `equals, hashCode and toString`() {
        val a = Patro.info(d(2081, 1, 1))
        val copy = CalendarInfo(a.tithi, a.isPublicHoliday, a.events.toList())
        assertEquals(a, copy)
        assertEquals(a.hashCode(), copy.hashCode())
        assertNotEquals(a, Patro.info(d(2081, 1, 2)))
        assertNotEquals<Any>(a, "x")
        assertEquals(
            "CalendarInfo(tithi=Panchami (पञ्चमी), isPublicHoliday=true, " +
                "events=[New Year (नयाँ वर्ष), Mesh Sankranti (मेष संक्रान्ति), Bisket Jatra (बिस्का: जात्रा)])",
            a.toString(),
        )
        val named = Named("क", "k")
        assertEquals(Named("क", "k"), named)
        assertEquals(Named("क", "k").hashCode(), named.hashCode())
        assertNotEquals(Named("क", "x"), named)
        assertNotEquals(Named("ख", "k"), named)
        assertNotEquals<Any>(named, "k")
        assertEquals("k", named.name(Lang.ENGLISH))
        assertEquals("क", named.name(Lang.NEPALI))
        assertEquals(EMPTY_INFO.hashCode(), CalendarInfo(null, false, emptyList()).hashCode())
    }
}
