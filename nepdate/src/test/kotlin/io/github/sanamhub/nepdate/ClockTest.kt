package io.github.sanamhub.nepdate

import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The clock rows: [nepaliToday], [NepaliDate.isToday], [NepaliDate.isYesterday],
 * [NepaliDate.isTomorrow], and the three `current*` range factories.
 *
 * Every assertion that fixes a date uses a [Clock] with a fixed instant, so the test never depends
 * on the machine's clock. The `ZoneId` overloads read the real clock, so they are checked against
 * the date the same zone reports now, not against a constant.
 */
class ClockTest {
    // AD 2024-07-30 12:00 UTC is 2024-07-30 17:45 in Nepal: BS 2081-04-15, the same day.
    private val noonUtc = Instant.parse("2024-07-30T12:00:00Z")
    private val nepalNoon: Clock = Clock.fixed(noonUtc, KATHMANDU)

    // The same instant is 2024-07-30 in UTC but already 2024-07-31 in Nepal.
    private val lateUtc = Instant.parse("2024-07-30T18:20:00Z")

    @Test
    fun `nepaliToday takes the date of the clock's zone`() {
        assertEquals(NepaliDate.of(2081, 4, 15), nepaliToday(nepalNoon))
        assertEquals(NepaliDate.of(2081, 4, 15), nepaliToday(Clock.fixed(noonUtc, ZoneOffset.UTC)))
        assertEquals(NepaliDate.of(2081, 4, 16), nepaliToday(Clock.fixed(lateUtc, KATHMANDU)))
        // The same instant, two zones, two BS dates.
        assertEquals(NepaliDate.of(2081, 4, 15), nepaliToday(Clock.fixed(lateUtc, ZoneOffset.UTC)))
    }

    @Test
    fun `isToday isYesterday and isTomorrow on a fixed clock`() {
        val today = NepaliDate.of(2081, 4, 15)
        assertTrue(today.isToday(nepalNoon))
        assertFalse(today.isYesterday(nepalNoon))
        assertFalse(today.isTomorrow(nepalNoon))

        assertTrue(NepaliDate.of(2081, 4, 14).isYesterday(nepalNoon))
        assertTrue(NepaliDate.of(2081, 4, 16).isTomorrow(nepalNoon))
        assertFalse(NepaliDate.of(2081, 4, 16).isToday(nepalNoon))
        assertFalse(NepaliDate.of(2081, 4, 14).isTomorrow(nepalNoon))
        assertFalse(NepaliDate.of(2081, 5, 1).isYesterday(nepalNoon))
    }

    @Test
    fun `the relative predicates follow the clock's zone`() {
        val nepalClock = Clock.fixed(lateUtc, KATHMANDU)
        val utcClock = Clock.fixed(lateUtc, ZoneOffset.UTC)
        assertTrue(NepaliDate.of(2081, 4, 16).isToday(nepalClock))
        assertFalse(NepaliDate.of(2081, 4, 16).isToday(utcClock))
        assertTrue(NepaliDate.of(2081, 4, 15).isToday(utcClock))
        assertTrue(NepaliDate.of(2081, 4, 15).isYesterday(Clock.fixed(Instant.parse("2024-07-31T12:00:00Z"), KATHMANDU)))
        assertFalse(NepaliDate.of(2081, 4, 16).isYesterday(Clock.fixed(Instant.parse("2024-07-31T12:00:00Z"), KATHMANDU)))
    }

    @Test
    fun `the zone overloads agree with the date that zone reports now`() {
        val zone: ZoneId = KATHMANDU
        val today = nepaliToday(zone)
        // The zone overloads read the real clock, so this is the strongest deterministic
        // statement: whichever of the three predicates holds, exactly one of them holds. If
        // midnight passes between the two clock reads, today became yesterday and the count is
        // still one.
        val matches = listOf(today.isToday(zone), today.isYesterday(zone), today.isTomorrow(zone)).count { it }
        assertEquals(1, matches)
    }

    @Test
    fun `the current range factories follow the clock`() {
        val today = NepaliDate.of(2081, 4, 15)
        val month = NepaliDateRange.currentMonth(nepalNoon)
        assertEquals(NepaliDate.of(2081, 4, 1), month.start)
        assertEquals(NepaliDate.of(2081, 4, 32), month.endInclusive)
        assertEquals(NepaliDateRange.ofMonth(today.year, today.monthValue), month)

        val fiscal = NepaliDateRange.currentFiscalYear(nepalNoon)
        assertEquals(NepaliDate.of(2081, 4, 1), fiscal.start)
        assertEquals(FiscalYear(2081).range(), fiscal)

        val calendar = NepaliDateRange.currentCalendarYear(nepalNoon)
        assertEquals(NepaliDateRange.ofYear(2081), calendar)

        // 2081-04-15 is in the month that starts the fiscal year, so the month and the fiscal
        // year start together but end on different days.
        assertEquals(fiscal.start, month.start)
        assertTrue(fiscal.size > month.size)
    }

    @Test
    fun `the current range factories with a zone read the real clock`() {
        val today = nepaliToday(KATHMANDU)
        // Each factory may read the clock a moment after `today` did, so the range either holds
        // today or starts after it once midnight has passed.
        val month = NepaliDateRange.currentMonth(KATHMANDU)
        assertTrue(month.contains(today) || month.start > today)
        val year = NepaliDateRange.currentCalendarYear(KATHMANDU)
        assertTrue(year.contains(today) || year.start > today)
        val fiscal = NepaliDateRange.currentFiscalYear(KATHMANDU)
        assertTrue(fiscal.contains(today) || fiscal.start > today)
        assertEquals(Month.SHRAWAN, fiscal.start.month)
        assertEquals(1, fiscal.start.dayOfMonth)
        assertEquals(Month.ASHAD, fiscal.endInclusive.month)
    }
}
