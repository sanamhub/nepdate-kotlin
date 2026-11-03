package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * `format-matrix.tsv`: every order, separator, padding and language of
 * [NepaliDate.toShortString], plus the buffer rows.
 *
 * The C# `ISpanFormattable.TryFormat` reports false when the caller's buffer is too small. The port
 * appends to a `StringBuilder`, which grows instead, so the buffer rows check that a builder created
 * with the recorded capacity still receives the whole text; that is the difference the Kotlin column
 * of the API names, not a skipped row.
 */
class FormatMatrixVectorTest {
    @Test
    fun `every row of format-matrix tsv`() {
        val rows = SharedFiles.tsv("spec/vectors/csharp-golden/format-matrix.tsv")
        assertEquals(102, rows.size)
        for (row in rows) {
            val context = row.toString()
            val date = SharedFiles.date(row.getValue("bs"))
            val lang = if (row.getValue("lang") == "ne") Lang.NEPALI else Lang.ENGLISH
            when (row.getValue("kind")) {
                "short" -> {
                    val actual =
                        date.toShortString(
                            DateOrder.valueOf(row.getValue("order").uppercase()),
                            Separator.valueOf(row.getValue("separator").uppercase()),
                            row.getValue("pad").toBooleanStrict(),
                            lang,
                        )
                    assertEquals(row.getValue("result"), actual, context)
                }
                "try_format" -> {
                    val pattern = row.getValue("format")
                    val capacity = row.getValue("capacity").toInt()
                    val out = StringBuilder(capacity)
                    val actual =
                        if (pattern == "default") {
                            date.appendShortString(out, lang = lang).toString()
                        } else {
                            date.formatTo(out, pattern, lang).toString()
                        }
                    val stringForm = if (pattern == "default") date.toShortString(lang = lang) else date.format(pattern, lang)
                    assertEquals(stringForm, actual, context)
                    if (row.getValue("result") == "ERR") {
                        assertTrue(actual.length > capacity, "$context: a StringBuilder grows past its capacity")
                    } else {
                        assertEquals(row.getValue("result"), actual, context)
                    }
                }
                else -> error("unknown kind in $context")
            }
        }
    }

    @Test
    fun `a builder with no room still gets the whole text`() {
        val date = NepaliDate.of(2081, 4, 15)
        val out = StringBuilder(0)
        date.appendShortString(out)
        assertEquals("2081/04/15", out.toString())
        assertEquals("२०८१/०४/१५", NepaliDate.of(2081, 4, 15).appendShortString(StringBuilder(), lang = Lang.NEPALI).toString())
    }
}
