@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.MonthOverflow
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import kotlin.test.Test
import kotlin.test.assertEquals

/** C# `Core/NepaliDateManipulationTests.cs`. */
class NepaliDateManipulationTests {
    private fun ymd(date: NepaliDate) = Triple(date.year, date.monthValue, date.dayOfMonth)

    @Test
    fun MonthEndDate_ReturnsCorrectDate() {
        assertEquals(Triple(2080, 5, 31), ymd(NepaliDate.of(2080, 5, 15).lastDayOfMonth()))
    }

    // The fifth row passes awayFromMonthEnd = true, which is MonthOverflow.SPILL; the others use the default.
    @ParameterizedTest
    @CsvSource(
        "2080, 5, 15, 1, 2080, 6, 15, false",
        "2080, 5, 15, -1, 2080, 4, 15, false",
        "2080, 5, 15, 12, 2081, 5, 15, false",
        "2081, 4, 32, 5, 2081, 9, 29, false",
        "2081, 4, 32, 5, 2081, 10, 3, true",
    )
    fun AddMonths_ReturnsCorrectDate(
        year: Int,
        month: Int,
        day: Int,
        monthsToAdd: Long,
        expectedYear: Int,
        expectedMonth: Int,
        expectedDay: Int,
        awayFromMonthEnd: Boolean,
    ) {
        val overflow = if (awayFromMonthEnd) MonthOverflow.SPILL else MonthOverflow.CLAMP
        val result = NepaliDate.of(year, month, day).plusMonths(monthsToAdd, overflow)
        assertEquals(Triple(expectedYear, expectedMonth, expectedDay), ymd(result))
    }

    @ParameterizedTest
    @CsvSource(
        "2080, 5, 15, 5, 2080, 5, 20",
        "2080, 5, 15, -5, 2080, 5, 10",
        "2080, 5, 31, 1, 2080, 6, 1",
        "2080, 12, 30, 5, 2081, 1, 5",
    )
    fun AddDays_ReturnsCorrectDate(
        year: Int,
        month: Int,
        day: Int,
        daysToAdd: Long,
        expectedYear: Int,
        expectedMonth: Int,
        expectedDay: Int,
    ) {
        assertEquals(Triple(expectedYear, expectedMonth, expectedDay), ymd(NepaliDate.of(year, month, day).plusDays(daysToAdd)))
    }

    // C# returns a TimeSpan; the port returns a signed day count (PARITY D-07).
    @Test
    fun Subtract_TwoDates_ReturnsCorrectTimeSpan() {
        val earlier = NepaliDate.of(2080, 5, 1)
        val later = NepaliDate.of(2080, 5, 16)
        assertEquals(15L, earlier.daysUntil(later))
        assertEquals(-15L, later.daysUntil(earlier))
    }

    @Test
    fun Subtract_SameDate_ReturnsZero() {
        val date = NepaliDate.of(2080, 5, 15)
        assertEquals(0L, date.daysUntil(date))
    }
}
