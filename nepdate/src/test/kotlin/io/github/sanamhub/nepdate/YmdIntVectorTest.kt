package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * `ymd-int.tsv`: the `YYYYMMDD` integer both ways (PARITY D-11), which replaces the C#
 * `TypeConverter` type. A negative integer is read with the same truncating division C# uses.
 */
class YmdIntVectorTest {
    private fun kindOf(block: () -> Unit): NepDateException.Kind = assertFailsWith<NepDateException> { block() }.kind

    @Test
    fun `every row of ymd-int tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/ymd-int.tsv")
        assertEquals(14, rows.size)
        for (row in rows) {
            val context = row.toString()
            when (row.getValue("kind")) {
                "date" -> {
                    val date = SharedFiles.date(row.getValue("input"))
                    assertEquals(row.getValue("ymd").toInt(), date.toYmd(), context)
                    assertEquals(date, NepaliDate.fromYmd(date.toYmd()), context)
                }
                "int" -> {
                    val value = row.getValue("ymd").toInt()
                    val result = row.getValue("result")
                    if (result == "ERR") {
                        assertFailsWith<NepDateException>(context) { NepaliDate.fromYmd(value) }
                    } else {
                        assertEquals(SharedFiles.date(result), NepaliDate.fromYmd(value), context)
                        assertEquals(value, NepaliDate.fromYmd(value).toYmd(), context)
                    }
                }
                else -> error("unknown kind in $context")
            }
        }
    }

    @Test
    fun `the widest integer does not wrap`() {
        // 2147483647 is a year of 214748, not a truncated 1901-to-2199 date: the argument is
        // split before it is validated, so no 32-bit truncation can turn it into a valid date.
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.fromYmd(Int.MAX_VALUE) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.fromYmd(Int.MIN_VALUE) })
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.fromYmd(-1) })
    }

    @Test
    fun `the error kind says which part of the integer is wrong`() {
        assertEquals(NepDateException.Kind.OUT_OF_RANGE, kindOf { NepaliDate.fromYmd(0) })
        // 2199 has a 30-day Chaitra, so the 31st is a day error rather than an out-of-range year.
        assertEquals(NepDateException.Kind.INVALID_DAY, kindOf { NepaliDate.fromYmd(21991231) })
        assertEquals(NepDateException.Kind.INVALID_MONTH, kindOf { NepaliDate.fromYmd(20810015) })
        assertEquals(NepDateException.Kind.INVALID_DAY, kindOf { NepaliDate.fromYmd(20810433) })
        assertEquals(NepDateException.Kind.INVALID_DAY, kindOf { NepaliDate.fromYmd(20810400) })
    }

    @Test
    fun `the packed integer is the inverse of the date`() {
        assertEquals(19010101, NepaliDate.MIN.toYmd())
        assertEquals(21991230, NepaliDate.MAX.toYmd())
        assertEquals(NepaliDate.of(2081, 4, 32), NepaliDate.fromYmd(20810432))
        assertEquals(20810432, NepaliDate.of(2081, 4, 32).toYmd())
    }
}
