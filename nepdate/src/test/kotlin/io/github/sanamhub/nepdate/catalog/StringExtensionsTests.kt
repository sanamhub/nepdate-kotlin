@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * C# `Extensions/StringExtensionsTests.cs`, phase 3. The port has no string extensions; C#'s
 * `ToNepaliDate` is lenient parsing, so these rows call `parseLenient`.
 */
class StringExtensionsTests {
    private val expected = NepaliDate.of(2080, 5, 15)

    @Test
    fun ToNepaliDate_ValidDateString_ReturnsNepaliDate() {
        assertEquals(expected, NepaliDate.parseLenient("2080/05/15"))
    }

    @Test
    fun ToNepaliDate_NepaliDigits_ReturnsNepaliDate() {
        assertEquals(expected, NepaliDate.parseLenient("२०८०/०५/१५"))
    }

    @Test
    fun ToNepaliDate_InvalidDateString_ThrowsFormatException() {
        assertFailsWith<NepDateException> { NepaliDate.parseLenient("not a date") }
    }

    @Test
    fun TryToNepaliDate_ValidDateString_ReturnsTrue() {
        assertEquals(expected, NepaliDate.parseOrNull("2080/05/15"))
    }

    @Test
    fun TryToNepaliDate_InvalidDateString_ReturnsFalse() {
        assertNull(NepaliDate.parseOrNull("not a date"))
    }
}
