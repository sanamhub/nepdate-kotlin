@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import java.nio.CharBuffer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * C# `Abilities/NepaliDateParsableTests.cs`, phase 3. .NET's IParsable and ISpanParsable map to
 * `parse` and `parseOrNull`, which take any CharSequence; a CharBuffer stands in for a span.
 */
class NepaliDateParsableTests {
    private val expected = NepaliDate.of(2081, 4, 15)

    @Test
    fun Parse_Via_GenericConstraint_Returns_CorrectDate() {
        assertEquals(expected, NepaliDate.parse("2081-04-15"))
    }

    @Test
    fun TryParse_Via_GenericConstraint_ValidDate_Returns_True() {
        assertEquals(expected, NepaliDate.parseOrNull("2081/04/15"))
    }

    @Test
    fun TryParse_Via_GenericConstraint_InvalidDate_Returns_False() {
        assertNull(NepaliDate.parseOrNull("garbage"))
    }

    @Test
    fun SpanParse_Via_GenericConstraint_Returns_CorrectDate() {
        assertEquals(expected, NepaliDate.parse(CharBuffer.wrap("x2081-04-15x", 1, 11)))
    }

    @Test
    fun SpanTryParse_Via_GenericConstraint_ValidDate_Returns_True() {
        assertEquals(expected, NepaliDate.parseOrNull(CharBuffer.wrap("x2081/04/15x", 1, 11)))
    }
}
