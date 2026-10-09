@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * C# `Core/DictionaryIntegrityTests.cs`, phase 1. C# checks its two conversion dictionaries; here
 * the same facts are checked on the public API, which reads CalendarTables.
 */
class DictionaryIntegrityTests {
    private fun local(date: NepaliDate): LocalDate = LocalDate.of(date.gregorianYear, date.gregorianMonth, date.gregorianDay)

    private fun fromLocal(date: LocalDate) = NepaliDate.ofGregorian(date.year, date.monthValue, date.dayOfMonth)

    private fun forEachDay(action: (NepaliDate) -> Unit) {
        for (year in 1901..2199) {
            for (month in 1..12) {
                val length = NepaliDate.of(year, month, 1).lengthOfMonth()
                for (day in 1..length) action(NepaliDate.of(year, month, day))
            }
        }
    }

    @Test
    fun AllNepaliMonths_RoundtripToEnglishAndBack_ProducesOriginalDate() {
        var failures = 0
        for (year in 1901..2199) {
            for (month in 1..12) {
                val date = NepaliDate.of(year, month, 1)
                if (fromLocal(local(date)) != date) failures++
            }
        }
        assertEquals(0, failures)
    }

    @Test
    fun AllNepaliDays_RoundtripToEnglishAndBack_ProducesOriginalDate() {
        var failures = 0
        var total = 0
        forEachDay { date ->
            if (fromLocal(local(date)) != date) failures++
            total++
        }
        assertEquals(0, failures)
        assertEquals(109212, total)
    }

    @Test
    fun AllEnglishDays_RoundtripToNepaliAndBack_ProducesOriginalDate() {
        var failures = 0
        var total = 0
        var day = local(NepaliDate.MIN)
        val end = local(NepaliDate.MAX)
        while (!day.isAfter(end)) {
            if (local(fromLocal(day)) != day) failures++
            total++
            day = day.plusDays(1)
        }
        assertEquals(0, failures)
        assertEquals(109212, total)
    }

    @Test
    fun AllMonths_MonthEndDay_IsWithinValidRange() {
        for (year in 1901..2199) {
            for (month in 1..12) {
                val length = NepaliDate.of(year, month, 1).lengthOfMonth()
                assertTrue(length in 29..32, "$year-$month has $length days")
            }
        }
    }

    @Test
    fun ConsecutiveNepaliDays_ProduceConsecutiveEnglishDays() {
        var previous: LocalDate? = null
        var violations = 0
        forEachDay { date ->
            val day = local(date)
            if (previous != null && previous!!.plusDays(1) != day) violations++
            previous = day
        }
        assertEquals(0, violations)
    }

    @ParameterizedTest
    @CsvSource(
        "2080, 1, 1, 2023, 4, 14",
        "2000, 1, 1, 1943, 4, 14",
        "2081, 1, 1, 2024, 4, 13",
        "1901, 1, 1, 1844, 4, 11",
    )
    fun KnownReferenceDates_ConvertCorrectly(
        nepY: Int,
        nepM: Int,
        nepD: Int,
        engY: Int,
        engM: Int,
        engD: Int,
    ) {
        val date = NepaliDate.of(nepY, nepM, nepD)
        assertEquals(LocalDate.of(engY, engM, engD), local(date))
        assertEquals(date, NepaliDate.ofGregorian(engY, engM, engD))
    }

    @Test
    fun BoundaryDates_ConvertCorrectly() {
        val min = NepaliDate.MIN
        assertEquals(Triple(1901, 1, 1), Triple(min.year, min.monthValue, min.dayOfMonth))
        assertEquals(min, fromLocal(local(min)))
        val max = NepaliDate.MAX
        assertEquals(2199, max.year)
        assertEquals(12, max.monthValue)
        assertEquals(max, fromLocal(local(max)))
    }

    @Test
    fun TotalDaysInRange_MatchesEnglishDateSpan() {
        var total = 0
        for (year in 1901..2199) {
            for (month in 1..12) total += NepaliDate.of(year, month, 1).lengthOfMonth()
        }
        val span = NepaliDate.MAX.toEpochDay() - NepaliDate.MIN.toEpochDay() + 1
        assertEquals(span, total.toLong())
        assertEquals(local(NepaliDate.MAX).toEpochDay() - local(NepaliDate.MIN).toEpochDay() + 1, span)
    }
}
