package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals

class CalendarTablesTest {
    @Test
    fun `month starts end at the total day count`() {
        assertEquals(3589, CalendarTables.MONTH_START.size)
        assertEquals(0, CalendarTables.MONTH_START[0])
        assertEquals(109212, CalendarTables.MONTH_START[3588])
    }

    @Test
    fun `month lengths decode to 29 to 32`() {
        for (i in 0 until CalendarTables.MONTHS) {
            val length = CalendarTables.MONTH_START[i + 1] - CalendarTables.MONTH_START[i]
            assert(length in 29..32) { "month index $i has length $length" }
        }
    }

    @Test
    fun `january firsts start before the epoch and follow the leap rule`() {
        assertEquals(302, CalendarTables.JAN1.size)
        assertEquals(-101, CalendarTables.JAN1[0])
        assertEquals(365, CalendarTables.JAN1[300] - CalendarTables.JAN1[299]) // AD 2144
        assertEquals(366, CalendarTables.JAN1[1] - CalendarTables.JAN1[0]) // AD 1844
        assertEquals(365, CalendarTables.JAN1[57] - CalendarTables.JAN1[56]) // AD 1900, not leap
        assertEquals(366, CalendarTables.JAN1[157] - CalendarTables.JAN1[156]) // AD 2000, leap
    }

    @Test
    fun `cumulative month days for common and leap years`() {
        assertEquals(26, CalendarTables.CUM.size)
        assertEquals(365, CalendarTables.CUM[12])
        assertEquals(366, CalendarTables.CUM[25])
        assertEquals(59, CalendarTables.CUM[2])
        assertEquals(60, CalendarTables.CUM[15])
    }

    @Test
    fun `generated constants`() {
        assertEquals(1901, MIN_YEAR)
        assertEquals(2199, MAX_YEAR)
        assertEquals(-45920, EPOCH_UNIX_DAYS)
        assertEquals(4, EPOCH_WEEKDAY)
        assertEquals(3588, MONTH_LENGTHS_DATA.length)
    }
}
