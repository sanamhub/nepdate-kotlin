@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.FiscalYear
import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * C# `Core/FiscalYearTests.cs`. C#'s static and instance helpers map to the FiscalYear
 * value (PARITY D-08); the static quarter helpers take a BS year and a month in that quarter.
 */
class FiscalYearTests {
    private val end2080 = NepaliDate.of(2081, 3, 1).lastDayOfMonth()

    @Test
    fun GetFiscalYearStartAndEndDate_ValidYear_ReturnsCorrectDates() {
        val fy = FiscalYear(2080)
        assertEquals(NepaliDate.of(2080, 4, 1), fy.start())
        assertEquals(end2080, fy.end())
    }

    @ParameterizedTest
    @ValueSource(ints = [1843, 2200])
    fun GetFiscalYearStartAndEndDate_InvalidYear_ThrowsException(year: Int) {
        assertFailsWith<NepDateException> { FiscalYear(year).range() }
    }

    @Test
    fun GetFiscalYearStartDate_ValidYear_ReturnsCorrectDate() {
        assertEquals(NepaliDate.of(2080, 4, 1), FiscalYear(2080).start())
    }

    @Test
    fun GetFiscalYearEndDate_ValidYear_ReturnsCorrectDate() {
        assertEquals(end2080, FiscalYear(2080).end())
    }

    @Test
    fun FiscalYearStartAndEndDate_OnNepaliDateInstance_ReturnsCorrectDates() {
        val fy = NepaliDate.of(2080, 6, 15).fiscalYear
        assertEquals(NepaliDate.of(2080, 4, 1), fy.start())
        assertEquals(end2080, fy.end())
    }

    @Test
    fun FiscalYearStartDate_OnNepaliDateInstance_ReturnsCorrectDate() {
        assertEquals(NepaliDate.of(2080, 4, 1), NepaliDate.of(2080, 6, 15).fiscalYear.start())
    }

    @Test
    fun FiscalYearEndDate_OnNepaliDateInstance_ReturnsCorrectDate() {
        assertEquals(end2080, NepaliDate.of(2080, 6, 15).fiscalYear.end())
    }

    @Test
    fun GetFiscalYearQuarterStartAndEndDate_ValidYearAndMonth_ReturnsCorrectDates() {
        val quarter = FiscalYear(2080).range(NepaliDate.of(2080, 5, 1).quarter)
        assertEquals(NepaliDate.of(2080, 4, 1), quarter.start)
        assertEquals(NepaliDate.of(2080, 6, 1).lastDayOfMonth(), quarter.endInclusive)
    }

    @Test
    fun GetFiscalYearQuarterStartDate_ValidYearAndMonth_ReturnsCorrectDate() {
        assertEquals(NepaliDate.of(2080, 7, 1), FiscalYear(2080).range(NepaliDate.of(2080, 8, 1).quarter).start)
    }

    @Test
    fun GetFiscalYearQuarterEndDate_ValidYearAndMonth_ReturnsCorrectDate() {
        val end = FiscalYear(2080).range(NepaliDate.of(2080, 11, 1).quarter).endInclusive
        assertEquals(NepaliDate.of(2080, 12, 1).lastDayOfMonth(), end)
    }
}
