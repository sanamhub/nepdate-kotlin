@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.toNepaliDate
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * C# `Extensions/DateTimeExtensionsTests.cs`. C# converts `System.DateTime`; the port
 * converts its `java.time` twin `LocalDate` (`JavaTime.kt`). `DateTime.MinValue` and
 * `MaxValue` (0001-01-01 and 9999-12-31) are also outside the supported range, so the port asserts
 * the same rule on `LocalDate.MIN` and `LocalDate.MAX`.
 */
class DateTimeExtensionsTests {
    @Test
    fun ToNepaliDate_ValidDateTime_ReturnsCorrectNepaliDate() {
        assertEquals(2080, LocalDate.of(2023, 9, 1).toNepaliDate().year)
    }

    @Test
    fun ToNepaliDate_MinValue_ThrowsArgumentOutOfRangeException() {
        val error = assertFailsWith<NepDateException> { LocalDate.MIN.toNepaliDate() }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
    }

    @Test
    fun ToNepaliDate_MaxValue_ThrowsArgumentOutOfRangeException() {
        val error = assertFailsWith<NepDateException> { LocalDate.MAX.toNepaliDate() }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
    }

    @Test
    fun ToNepaliDate_DateBeforeMinSupported_ThrowsArgumentOutOfRangeException() {
        val error = assertFailsWith<NepDateException> { LocalDate.of(1844, 4, 10).toNepaliDate() }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
    }

    @Test
    fun ToNepaliDate_DateAfterMaxSupported_ThrowsArgumentOutOfRangeException() {
        val error = assertFailsWith<NepDateException> { LocalDate.of(2143, 4, 16).toNepaliDate() }
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, error.kind)
    }
}
