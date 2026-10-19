@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.FiscalYear
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Quarter
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * C# `Core/FiscalYearInstanceMethodTests.cs`, on the FiscalYear value (PARITY D-08):
 * `yearOffset: n` is `FiscalYear(fy.startYear + n)`, `FiscalYearQuarters.First..Fourth` are
 * `Quarter.Q1..Q4`, and `Current` is `date.quarter`.
 */
class FiscalYearInstanceMethodTests {
    private fun d(
        y: Int,
        m: Int,
        day: Int,
    ) = NepaliDate.of(y, m, day)

    private fun lastOf(
        y: Int,
        m: Int,
    ) = d(y, m, 1).lastDayOfMonth()

    private fun shifted(
        date: NepaliDate,
        offset: Int,
    ) = FiscalYear(date.fiscalYear.startYear + offset)

    private val q1Date = d(2080, 5, 15)
    private val q4Date = d(2081, 2, 10)

    @Test
    fun FiscalYearStartDate_DateInShrawan_ReturnsSameyearShrawan1() {
        assertEquals(d(2080, 4, 1), d(2080, 4, 15).fiscalYear.start())
    }

    @Test
    fun FiscalYearStartDate_DateInChaitra_ReturnsSameYearShrawan1() {
        assertEquals(d(2080, 4, 1), d(2080, 12, 15).fiscalYear.start())
    }

    @Test
    fun FiscalYearStartDate_DateInBasikhakh_ReturnsPreviousYearShrawan1() {
        assertEquals(d(2079, 4, 1), d(2080, 1, 15).fiscalYear.start())
    }

    @Test
    fun FiscalYearStartDate_DateInAshadh_ReturnsPreviousYearShrawan1() {
        assertEquals(d(2079, 4, 1), d(2080, 3, 10).fiscalYear.start())
    }

    @Test
    fun FiscalYearStartDate_WithPositiveYearOffset_ShiftsForward() {
        assertEquals(d(2081, 4, 1), shifted(d(2080, 4, 1), 1).start())
    }

    @Test
    fun FiscalYearStartDate_WithNegativeYearOffset_ShiftsBackward() {
        assertEquals(d(2079, 4, 1), shifted(d(2080, 4, 1), -1).start())
    }

    @Test
    fun FiscalYearEndDate_DateInShrawan_ReturnsNextYearAshad() {
        assertEquals(lastOf(2081, 3), d(2080, 4, 15).fiscalYear.end())
    }

    @Test
    fun FiscalYearEndDate_DateInChaitra_ReturnsNextYearAshadh() {
        assertEquals(lastOf(2081, 3), d(2080, 12, 15).fiscalYear.end())
    }

    @Test
    fun FiscalYearEndDate_DateInBasikhakh_ReturnsSameYearAshadh() {
        assertEquals(lastOf(2080, 3), d(2080, 1, 15).fiscalYear.end())
    }

    @Test
    fun FiscalYearEndDate_WithPositiveYearOffset_ShiftsForward() {
        assertEquals(lastOf(2082, 3), shifted(d(2080, 4, 1), 1).end())
    }

    @Test
    fun FiscalYearStartAndEndDate_DateInQ2_ReturnsBothBoundaries() {
        val fy = d(2080, 8, 15).fiscalYear
        assertEquals(d(2080, 4, 1), fy.start())
        assertEquals(lastOf(2081, 3), fy.end())
    }

    @Test
    fun FiscalYearStartAndEndDate_DateInQ4_ReturnsPreviousFiscalYearBoundaries() {
        val fy = q4Date.fiscalYear
        assertEquals(d(2080, 4, 1), fy.start())
        assertEquals(lastOf(2081, 3), fy.end())
    }

    @Test
    fun FiscalYearQuarterStartDate_ExplicitFirst_ReturnsShrawan1() {
        assertEquals(d(2080, 4, 1), q1Date.fiscalYear.range(Quarter.Q1).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_ExplicitSecond_ReturnsKartik1() {
        assertEquals(d(2080, 7, 1), q1Date.fiscalYear.range(Quarter.Q2).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_ExplicitThird_ReturnsMagh1() {
        assertEquals(d(2080, 10, 1), q1Date.fiscalYear.range(Quarter.Q3).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_ExplicitFourth_ReturnsNextYearBasikhakh1() {
        assertEquals(d(2081, 1, 1), q1Date.fiscalYear.range(Quarter.Q4).start)
    }

    @Test
    fun FiscalYearQuarterEndDate_ExplicitFirst_ReturnsLastDayOfAshoj() {
        assertEquals(lastOf(2080, 6), q1Date.fiscalYear.range(Quarter.Q1).endInclusive)
    }

    @Test
    fun FiscalYearQuarterEndDate_ExplicitSecond_ReturnsLastDayOfPoush() {
        assertEquals(lastOf(2080, 9), q1Date.fiscalYear.range(Quarter.Q2).endInclusive)
    }

    @Test
    fun FiscalYearQuarterEndDate_ExplicitThird_ReturnsLastDayOfChaitra() {
        assertEquals(lastOf(2080, 12), q1Date.fiscalYear.range(Quarter.Q3).endInclusive)
    }

    @Test
    fun FiscalYearQuarterEndDate_ExplicitFourth_ReturnsNextYearLastDayOfAshadh() {
        assertEquals(lastOf(2081, 3), q1Date.fiscalYear.range(Quarter.Q4).endInclusive)
    }

    @Test
    fun FiscalYearQuarterStartAndEndDate_ExplicitSecond_ReturnsBothBoundaries() {
        val range = q1Date.fiscalYear.range(Quarter.Q2)
        assertEquals(d(2080, 7, 1), range.start)
        assertEquals(lastOf(2080, 9), range.endInclusive)
    }

    @Test
    fun FiscalYearQuarterStartAndEndDate_ExplicitThird_ReturnsBothBoundaries() {
        val range = q1Date.fiscalYear.range(Quarter.Q3)
        assertEquals(d(2080, 10, 1), range.start)
        assertEquals(12, range.endInclusive.monthValue)
    }

    @Test
    fun FiscalYearQuarterStartDate_Current_DateInQ1_ReturnsShrawan1() {
        assertEquals(d(2080, 4, 1), q1Date.fiscalYear.range(q1Date.quarter).start)
    }

    @Test
    fun FiscalYearQuarterEndDate_Current_DateInQ1_ReturnsLastDayOfAshoj() {
        assertEquals(lastOf(2080, 6), q1Date.fiscalYear.range(q1Date.quarter).endInclusive)
    }

    @Test
    fun FiscalYearQuarterStartDate_Current_DateInQ2_ReturnsKartik1() {
        val date = d(2080, 8, 15)
        assertEquals(d(2080, 7, 1), date.fiscalYear.range(date.quarter).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_Current_DateInQ3_ReturnsMagh1() {
        val date = d(2080, 11, 15)
        assertEquals(d(2080, 10, 1), date.fiscalYear.range(date.quarter).start)
    }

    @Test
    fun FiscalYearQuarterEndDate_Current_DateInQ3_ReturnsLastDayOfChaitra() {
        val date = d(2080, 11, 15)
        assertEquals(lastOf(2080, 12), date.fiscalYear.range(date.quarter).endInclusive)
    }

    @Test
    fun FiscalYearQuarterStartDate_ExplicitFirstWithOffset1_ReturnsNextYearShrawan1() {
        assertEquals(d(2081, 4, 1), shifted(q1Date, 1).range(Quarter.Q1).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_ExplicitFirstWithNegativeOffset_ReturnsPreviousYearShrawan1() {
        assertEquals(d(2079, 4, 1), shifted(q1Date, -1).range(Quarter.Q1).start)
    }

    @ParameterizedTest
    @ValueSource(ints = [4, 6, 7, 12])
    fun FiscalYearStartDate_AllFiscalMonths_Return2080Shrawan1(month: Int) {
        assertEquals(d(2080, 4, 1), d(2080, month, 1).fiscalYear.start())
    }

    @ParameterizedTest
    @ValueSource(ints = [1, 2, 3])
    fun FiscalYearStartDate_Q4CalendarMonths_ReturnPreviousYearShrawan1(month: Int) {
        assertEquals(d(2079, 4, 1), d(2080, month, 1).fiscalYear.start())
    }

    @Test
    fun FiscalYearQuarterStartDate_FromQ4Date_ExplicitFirst_ReturnsFY2080Q1Start() {
        assertEquals(d(2080, 4, 1), q4Date.fiscalYear.range(Quarter.Q1).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_FromQ4Date_ExplicitSecond_ReturnsFY2080Q2Start() {
        assertEquals(d(2080, 7, 1), q4Date.fiscalYear.range(Quarter.Q2).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_FromQ4Date_ExplicitThird_ReturnsFY2080Q3Start() {
        assertEquals(d(2080, 10, 1), q4Date.fiscalYear.range(Quarter.Q3).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_FromQ4Date_ExplicitFourth_ReturnsFY2080Q4Start() {
        assertEquals(d(2081, 1, 1), q4Date.fiscalYear.range(Quarter.Q4).start)
    }

    @Test
    fun FiscalYearQuarterStartDate_FromQ4Date_Current_ReturnsFY2080Q4Start() {
        assertEquals(d(2081, 1, 1), q4Date.fiscalYear.range(q4Date.quarter).start)
    }

    @Test
    fun FiscalYearQuarterEndDate_FromQ4Date_ExplicitFirst_ReturnsFY2080Q1End() {
        assertEquals(lastOf(2080, 6), q4Date.fiscalYear.range(Quarter.Q1).endInclusive)
    }

    @Test
    fun FiscalYearQuarterEndDate_FromQ4Date_ExplicitThird_ReturnsFY2080Q3End() {
        assertEquals(lastOf(2080, 12), q4Date.fiscalYear.range(Quarter.Q3).endInclusive)
    }

    @Test
    fun FiscalYearQuarterEndDate_FromQ4Date_ExplicitFourth_ReturnsFY2080Q4End() {
        assertEquals(lastOf(2081, 3), q4Date.fiscalYear.range(Quarter.Q4).endInclusive)
    }

    @Test
    fun FiscalYearQuarterEndDate_FromQ4Date_Current_ReturnsFY2080Q4End() {
        assertEquals(lastOf(2081, 3), q4Date.fiscalYear.range(q4Date.quarter).endInclusive)
    }
}
