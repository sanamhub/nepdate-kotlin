@file:Suppress("ktlint:standard:function-naming", "FunctionNaming")

package io.github.sanamhub.nepdate.catalog

import io.github.sanamhub.nepdate.NepaliDate
import java.util.TreeSet
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** C# `Abilities/NepaliDateComparableTests.cs`. */
class NepaliDateComparableTests {
    private val d1 = NepaliDate.of(2081, 1, 1)
    private val d2 = NepaliDate.of(2081, 6, 15)
    private val d3 = NepaliDate.of(2082, 1, 1)

    // C#'s non-generic IComparable is Comparable<NepaliDate> seen through an untyped reference.
    @Suppress("UNCHECKED_CAST")
    private fun untyped(date: NepaliDate) = date as Comparable<Any>

    @Test
    fun CompareTo_Object_Earlier_ReturnsNegative() {
        assertTrue(untyped(d1).compareTo(d2) < 0)
    }

    @Test
    fun CompareTo_Object_Same_ReturnsZero() {
        assertEquals(0, untyped(d1).compareTo(NepaliDate.of(2081, 1, 1)))
    }

    @Test
    fun CompareTo_Object_Later_ReturnsPositive() {
        assertTrue(untyped(d3).compareTo(d1) > 0)
    }

    @Test
    fun CompareTo_Object_Null_ReturnsPositive() {
        // compareTo takes a non-null date; null orders first through compareValues, as in C#.
        assertTrue(compareValues(d1, null) > 0)
    }

    @Test
    fun CompareTo_WrongType_ThrowsArgumentException() {
        // The JVM reports a wrong type as ClassCastException where .NET throws ArgumentException.
        assertFailsWith<ClassCastException> { untyped(d1).compareTo("2081/01/01") }
    }

    @Test
    fun CompareTo_Generic_Earlier_ReturnsNegative() {
        assertTrue(d1 < d2)
    }

    @Test
    fun CompareTo_Generic_Same_ReturnsZero() {
        assertEquals(0, d1.compareTo(NepaliDate.of(2081, 1, 1)))
    }

    @Test
    fun CompareTo_Generic_Later_ReturnsPositive() {
        assertTrue(d3.compareTo(d1) > 0)
    }

    @Test
    fun SortedSet_OrdersCorrectly() {
        val ordered = TreeSet(listOf(d3, d1, d2)).toList()
        assertEquals(listOf(d1, d2, d3), ordered)
    }

    @Test
    fun ArraySort_ViaIComparable_OrdersCorrectly() {
        val dates: Array<Any> = arrayOf(d3, d1, d2)
        java.util.Arrays.sort(dates)
        assertEquals(listOf<Any>(d1, d2, d3), dates.toList())
    }

    @Test
    fun List_Sort_OrdersCorrectly() {
        val list = mutableListOf(d3, d1, d2)
        list.sort()
        assertEquals(listOf(d1, d2, d3), list)
    }
}
