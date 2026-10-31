@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.patro.catalog

import io.github.sanamhub.nepdate.Lang
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.NepaliDateRange
import io.github.sanamhub.nepdate.patro.BASE_SERIAL
import io.github.sanamhub.nepdate.patro.EMPTY_INFO
import io.github.sanamhub.nepdate.patro.EVENT_DAY_DATA
import io.github.sanamhub.nepdate.patro.EVENT_NAME_DATA
import io.github.sanamhub.nepdate.patro.EVENT_START_DATA
import io.github.sanamhub.nepdate.patro.HOLIDAY_DATA
import io.github.sanamhub.nepdate.patro.Patro
import io.github.sanamhub.nepdate.patro.PatroSummary
import io.github.sanamhub.nepdate.patro.PatroTables
import io.github.sanamhub.nepdate.patro.SPAN
import io.github.sanamhub.nepdate.patro.TITHI_FIRST_OFFSET
import io.github.sanamhub.nepdate.patro.TITHI_INDEX_DATA
import io.github.sanamhub.nepdate.patro.calendarInfo
import io.github.sanamhub.nepdate.patro.d
import io.github.sanamhub.nepdate.patro.serialOf
import io.github.sanamhub.nepdate.patro.spanDays
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * C# `Core/CalendarDataTests.cs`. C# keeps the patro in `NepaliDate` (PARITY D-13); here it
 * is `Patro.info`. Rows about C#'s internal arrays (`CalendarStrings`, `CalendarOffsets`,
 * `TithiData`, `EventData`, `HolidayData`) assert the same fact on the decoded
 * tables and `PatroTables`; each says which. C#'s offsets count from BS 2001-01-01, these count
 * from BASE_SERIAL (2077-01-01).
 */
class CalendarDataTests {
    private fun offset(date: NepaliDate) = serialOf(date) - BASE_SERIAL

    private fun codes(data: String) = data.map { it.code }

    private fun assertStrictlyIncreasing(values: List<Int>) = assertTrue(values.zipWithNext().all { (a, b) -> a < b })

    // CalendarStrings.Pool is the deduplicated name table: PatroTables.pairs.
    @Test
    fun CalendarStrings_Pool_IsNonEmpty() {
        assertTrue(PatroTables.pairs.isNotEmpty())
    }

    @Test
    fun CalendarStrings_Pool_AllEntriesHaveNonEmptyNpAndEn() {
        for (pair in PatroTables.pairs) {
            assertTrue(pair.nepali.isNotEmpty() && pair.english.isNotEmpty(), "$pair")
        }
    }

    // CalendarOffsets.MonthStart has one entry per month; here one CalendarInfo per day of the span.
    @Test
    fun CalendarOffsets_MonthStart_HasExpectedLength() {
        assertEquals(SPAN, PatroTables.infoByDay.size)
    }

    // The offset of each month's first day in the span increases strictly, starting at 0.
    @Test
    fun CalendarOffsets_MonthStart_IsStrictlyMonotonic() {
        val starts = spanDays().filter { it.dayOfMonth == 1 }.map { offset(it) }
        assertEquals(0, starts.first())
        assertStrictlyIncreasing(starts)
    }

    // CalendarOffsets.TotalDays is the span, which is the summary's days.
    @Test
    fun CalendarOffsets_TotalDays_MatchesExpectedCount() {
        assertEquals(PatroSummary.number("days"), SPAN)
    }

    // The tithi days are one contiguous run inside the span, so their offsets ascend by one.
    @Test
    fun TithiData_DayOffsets_IsStrictlySorted() {
        val withTithi = PatroTables.infoByDay.indices.filter { PatroTables.infoByDay[it].tithi != null }
        assertEquals((TITHI_FIRST_OFFSET until TITHI_FIRST_OFFSET + TITHI_INDEX_DATA.length).toList(), withTithi)
        assertTrue(TITHI_FIRST_OFFSET + TITHI_INDEX_DATA.length <= SPAN)
    }

    @Test
    fun TithiData_AllPoolIds_AreInRange() {
        assertTrue(TITHI_INDEX_DATA.all { it.code - '0'.code in 0 until PatroSummary.number("tithi_names") })
    }

    @Test
    fun TithiData_EntryCount_Matches2190() {
        assertEquals(PatroSummary.number("tithi_days"), TITHI_INDEX_DATA.length)
        assertEquals(PatroSummary.number("tithi_days"), PatroTables.infoByDay.count { it.tithi != null })
    }

    @Test
    fun EventData_DayOffsets_IsStrictlySorted() {
        assertStrictlyIncreasing(codes(EVENT_DAY_DATA))
    }

    @Test
    fun EventData_SliceStart_IsNonDecreasing() {
        assertTrue(codes(EVENT_START_DATA).zipWithNext().all { (a, b) -> a <= b })
    }

    @Test
    fun EventData_SliceStart_SentinelEqualsEntriesLength() {
        assertEquals(EVENT_DAY_DATA.length + 1, EVENT_START_DATA.length)
        assertEquals(EVENT_NAME_DATA.length, EVENT_START_DATA.last().code)
    }

    @Test
    fun EventData_AllEntries_AreInRange() {
        assertTrue(codes(EVENT_NAME_DATA).all { it in PatroTables.pairs.indices })
    }

    @Test
    fun EventData_DayCount_Matches1671() {
        assertEquals(PatroSummary.number("event_days"), EVENT_DAY_DATA.length)
        assertEquals(PatroSummary.number("event_days"), PatroTables.infoByDay.count { it.events.isNotEmpty() })
    }

    @Test
    fun HolidayData_DayOffsets_IsStrictlySorted() {
        assertStrictlyIncreasing(codes(HOLIDAY_DATA))
        assertEquals(Patro.publicHolidays.sorted(), Patro.publicHolidays)
    }

    @Test
    fun HolidayData_Count_Matches294() {
        assertEquals(PatroSummary.number("holidays"), HOLIDAY_DATA.length)
    }

    // C#'s base is 2001-01-01; ours is 2077-01-01.
    @Test
    fun CalendarOffsets_GetOffset_BaseYear_Month1_Day1_ReturnsZero() {
        assertEquals(0, offset(d(2077, 1, 1)))
    }

    // C# returns -1 before its base; here the offset is negative and Patro.info gives the empty answer.
    @Test
    fun CalendarOffsets_GetOffset_OutOfRange_ReturnsNegativeOne() {
        assertTrue(offset(d(2076, 12, 30)) < 0)
        assertSame(EMPTY_INFO, Patro.info(d(2076, 12, 30)))
        assertSame(EMPTY_INFO, Patro.info(d(2084, 8, 21)))
    }

    // Every day of the coverage maps to its own offset, 0 until SPAN in order.
    @Test
    fun CalendarOffsets_GetOffset_AllOffsetsAreUniqueSentinel() {
        assertEquals((0 until SPAN).toList(), Patro.coverage.map { offset(it) })
    }

    @Test
    fun NepaliDate_2081_01_01_CalendarData() {
        val info = Patro.info(d(2081, 1, 1))
        assertEquals("पञ्चमी", info.tithi?.nepali)
        assertEquals("Panchami", info.tithi?.english)
        assertTrue(info.isPublicHoliday)
        assertTrue(info.events.any { it.nepali == "नयाँ वर्ष" && it.english == "New Year" })
    }

    @Test
    fun NepaliDate_2081_01_02_Tithi_IsShashthi() {
        val tithi = Patro.info(d(2081, 1, 2)).tithi
        assertEquals("षष्ठी", tithi?.name(Lang.NEPALI))
        assertEquals("Shashthi", tithi?.name(Lang.ENGLISH))
    }

    @Test
    fun NepaliDate_2081_01_05_IsPublicHoliday() {
        val info = Patro.info(d(2081, 1, 5))
        assertTrue(info.isPublicHoliday)
        assertEquals("नवमी", info.tithi?.nepali)
        assertEquals("Navami", info.tithi?.english)
    }

    @Test
    fun NepaliDate_2081_02_15_IsPublicHoliday_GanatantraDiwas() {
        val info = Patro.info(d(2081, 2, 15))
        assertTrue(info.isPublicHoliday)
        assertTrue(info.events.any { it.nepali == "गणतन्त्र दिवस" && it.english == "Republic Day" })
    }

    @Test
    fun NepaliDate_2080_11_01_CalendarData() {
        val info = Patro.info(d(2080, 11, 1))
        assertEquals("चतुर्थी", info.tithi?.nepali)
        assertEquals("Chaturthi", info.tithi?.english)
        assertFalse(info.isPublicHoliday)
        assertTrue(info.events.any { it.nepali == "जनयुद्ध दिवस" && it.english == "People's War Day" })
    }

    @Test
    fun NepaliDate_BeforeRange_AllPropertiesReturnDefaults() {
        assertEquals(EMPTY_INFO, Patro.info(d(2000, 6, 15)))
        assertTrue(Patro.info(d(2000, 6, 15)).events.isEmpty())
    }

    @Test
    fun NepaliDate_AfterRange_AllPropertiesReturnDefaults() {
        assertEquals(EMPTY_INFO, Patro.info(d(2090, 1, 1)))
        assertTrue(Patro.info(d(2090, 1, 1)).events.isEmpty())
    }

    @Test
    fun NepaliDate_OutsideTithiRange_TithiIsEmpty() {
        assertNull(Patro.info(d(2070, 6, 15)).tithi)
        assertNull(Patro.info(d(2077, 1, 1)).tithi)
    }

    // C# has parallel EventsNp and EventsEn arrays; here every event carries both names.
    @Test
    fun NepaliDate_EventsNp_And_EventsEn_AlwaysHaveSameLength() {
        for (date in listOf(d(2081, 1, 1), d(2081, 1, 5), d(2081, 2, 10), d(2080, 11, 1), d(2075, 1, 1))) {
            val events = Patro.info(date).events
            assertEquals(events.map { it.nepali }.size, events.map { it.english }.size)
            assertTrue(events.all { it.nepali.isNotEmpty() && it.english.isNotEmpty() })
        }
    }

    // C#'s GetCalendarInfo and its flat properties are Patro.info and the calendarInfo extension here.
    @Test
    fun GetCalendarInfo_TithiMatchesFlatProperties() {
        val date = d(2081, 1, 1)
        assertSame(Patro.info(date), date.calendarInfo)
        assertEquals(Patro.info(date).tithi, date.calendarInfo.tithi)
    }

    @Test
    fun AllDaysInScrapedRange_OffsetsAreInBounds() {
        val scraped = NepaliDateRange.of(d(2078, 1, 1), d(2082, 12, 1).lastDayOfMonth())
        assertTrue(scraped.all { offset(it) in 0 until SPAN })
    }

    @Test
    fun AllDaysInScrapedRange_NoExceptionOnPropertyAccess() {
        val scraped = NepaliDateRange.of(d(2078, 1, 1), d(2082, 12, 1).lastDayOfMonth())
        var names = 0
        for (date in scraped) {
            val info = Patro.info(date)
            names += (info.tithi?.name(Lang.NEPALI)?.length ?: 0) + info.events.sumOf { it.english.length }
            if (info.isPublicHoliday) names++
        }
        assertTrue(names > 0)
    }

    @Test
    fun AllTithiEntries_ResolveToNonEmptyStrings() {
        for (info in PatroTables.infoByDay) {
            val tithi = info.tithi ?: continue
            assertTrue(tithi.nepali.isNotEmpty() && tithi.english.isNotEmpty(), "$tithi")
        }
    }

    @Test
    fun AllHolidayOffsets_AreInBounds() {
        assertTrue(codes(HOLIDAY_DATA).all { it in 0 until SPAN })
    }

    @Test
    fun AllEventOffsets_AreInBounds() {
        assertTrue(codes(EVENT_DAY_DATA).all { it in 0 until SPAN })
    }

    @Test
    fun HolidayCount_ViaApi_Matches294() {
        val all = NepaliDateRange.of(d(2001, 1, 1), d(2089, 12, 1).lastDayOfMonth())
        assertEquals(PatroSummary.number("holidays"), all.count { Patro.info(it).isPublicHoliday })
    }

    // C# returns empty arrays, never null; Kotlin's List<Named> can't be null, so this checks both kinds of day.
    @Test
    fun CalendarInfo_EventArrays_AreNeverNull() {
        assertNotNull(Patro.info(d(2081, 1, 1)).events)
        assertTrue(Patro.info(d(2081, 1, 1)).events.isNotEmpty())
        assertTrue(Patro.info(d(2001, 1, 2)).events.isEmpty())
    }
}
