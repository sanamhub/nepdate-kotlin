@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.Month
import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import kotlin.test.Test
import kotlin.test.assertEquals

/** C# `Core/NepaliDateMonthNameTests.cs`. C#'s `NepaliMonths` enum is `Month`. */
class NepaliDateMonthNameTests {
    @ParameterizedTest
    @CsvSource(
        "1, BAISHAKH",
        "2, JESTHA",
        "3, ASHAD",
        "4, SHRAWAN",
        "5, BHADRA",
        "6, ASHOJ",
        "7, KARTIK",
        "8, MANGSIR",
        "9, POUSH",
        "10, MAGH",
        "11, FALGUN",
        "12, CHAITRA",
    )
    fun MonthName_AllMonths_ReturnCorrectEnum(
        month: Int,
        expected: Month,
    ) {
        assertEquals(expected, NepaliDate.of(2080, month, 1).month)
    }

    @Test
    fun MonthName_SameMonthDifferentDays_AllReturnSameEnum() {
        val first = NepaliDate.of(2080, 4, 1)
        assertEquals(Month.SHRAWAN, first.month)
        assertEquals(Month.SHRAWAN, NepaliDate.of(2080, 4, 15).month)
        assertEquals(Month.SHRAWAN, first.lastDayOfMonth().month)
    }

    @ParameterizedTest
    @ValueSource(ints = [2070, 2080, 2090, 2100])
    fun MonthName_SameMonthDifferentYears_AllReturnSameEnum(year: Int) {
        assertEquals(Month.POUSH, NepaliDate.of(year, 9, 1).month)
    }

    @Test
    fun MonthName_AddingOneMonth_AdvancesEnum() {
        val falgun = NepaliDate.of(2080, 11, 1)
        assertEquals(Month.FALGUN, falgun.month)
        assertEquals(Month.CHAITRA, falgun.plusMonths(1).month)
    }

    @Test
    fun MonthName_YearBoundary_WrapsFromChaitraToBasikhakh() {
        assertEquals(Month.CHAITRA, NepaliDate.of(2080, 12, 1).month)
        assertEquals(Month.BAISHAKH, NepaliDate.of(2081, 1, 1).month)
    }

    @ParameterizedTest
    @CsvSource(
        "BAISHAKH, 1",
        "JESTHA, 2",
        "ASHAD, 3",
        "SHRAWAN, 4",
        "BHADRA, 5",
        "ASHOJ, 6",
        "KARTIK, 7",
        "MANGSIR, 8",
        "POUSH, 9",
        "MAGH, 10",
        "FALGUN, 11",
        "CHAITRA, 12",
    )
    fun NepaliMonthsEnum_IntegerValueMatchesMonthNumber(
        month: Month,
        expectedNumber: Int,
    ) {
        assertEquals(expectedNumber, month.value)
    }
}
