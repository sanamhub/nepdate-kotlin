@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.Month
import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.Weekday
import org.junit.jupiter.api.Disabled
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.fail

/** C# `Core/NepaliDatePropertiesTests.cs`, phase 1. */
class NepaliDatePropertiesTests {
    private val date = NepaliDate.of(2080, 5, 15)

    @Test
    fun EnglishDate_Conversion_ReturnsCorrectDate() {
        assertEquals(Triple(2023, 9, 1), Triple(date.gregorianYear, date.gregorianMonth, date.gregorianDay))
    }

    @Test
    fun DayOfWeek_ReturnsCorrectValue() {
        assertEquals(Weekday.FRIDAY, date.dayOfWeek)
    }

    @Test
    fun MonthEndDay_ReturnsCorrectValue() {
        assertEquals(31, date.lengthOfMonth())
    }

    @Test
    fun MonthName_ReturnsCorrectEnum() {
        assertEquals(Month.BHADRA, date.month)
    }

    @Test
    @Disabled(NEEDS_TODAY)
    fun Today_ReturnsCurrentNepaliDate() {
        fail(NEEDS_TODAY)
    }

    @Test
    fun DayOfYear_FirstDayOfYear_Returns1() {
        assertEquals(1, NepaliDate.of(2080, 1, 1).dayOfYear)
    }

    @Test
    fun DayOfYear_FirstDayOfMonth4_EqualsSum_Of_Months1To3_Plus1() {
        val expected = (1..3).sumOf { NepaliDate.of(2080, it, 1).lengthOfMonth() } + 1
        assertEquals(expected, NepaliDate.of(2080, 4, 1).dayOfYear)
    }

    @Test
    @Suppress("EqualsNullCall") // the C# row calls Equals(null) on purpose
    fun Equals_NullObject_ReturnsFalse() {
        assertFalse(date.equals(null))
    }

    @Test
    fun Equals_DifferentType_ReturnsFalse() {
        assertFalse(date.equals("2080/05/15"))
        assertFalse(date.equals(42))
    }
}
