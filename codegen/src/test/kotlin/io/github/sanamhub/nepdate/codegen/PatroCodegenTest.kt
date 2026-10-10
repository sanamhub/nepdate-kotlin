package io.github.sanamhub.nepdate.codegen

import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.readText
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class PatroCodegenTest {
    private val repoRoot =
        generateSequence(Paths.get("").toAbsolutePath()) { it.parent }
            .first { Files.exists(it.resolve("shared/VERSION")) }

    private val calendar = parseCalendar(repoRoot.resolve(CALENDAR_JSON).readText())
    private val days = readPatroDays(repoRoot, calendar)
    private val tables = buildPatroTables(days)

    private fun serial(
        y: Int,
        m: Int,
        d: Int,
    ) = serialOf(calendar, y, m, d)!!

    private fun tithi(i: Int) = NamePair("np$i", "en$i")

    @Test
    fun `serials follow ALGORITHM section 4`() {
        assertEquals(0, serial(1901, 1, 1))
        assertEquals(64285, serial(2077, 1, 1))
        assertEquals(null, serialOf(calendar, 2081, 4, 33))
        assertEquals(null, serialOf(calendar, 1900, 12, 30))
    }

    @Test
    fun `tables hold the PATRO section 3 counts`() {
        assertEquals(64285, tables.baseSerial)
        assertEquals(2793, tables.span)
        assertEquals(serial(2084, 8, 20), tables.baseSerial + tables.span - 1)
        assertEquals(serial(2078, 1, 2) - 64285, tables.tithiFirstOffset)
        assertEquals(2190, tables.tithiIndex.size)
        assertEquals(294, tables.holidays.size)
        assertEquals(1671, tables.eventDays.size)
        assertEquals(1672, tables.eventStart.size)
        assertEquals(3032, tables.eventStart.last())
        assertEquals(3032, tables.eventNames.size)
        assertEquals(753, tables.names.size)
        assertTrue(tables.tithiIndex.all { it in 0 until 32 })
        assertEquals(tables.holidays.sorted(), tables.holidays)
        assertEquals(tables.eventDays.sorted(), tables.eventDays)
    }

    @Test
    fun `events keep file order`() {
        val first = days.first { it.serial == serial(2081, 1, 1) }
        val k = tables.eventDays.indexOf(first.serial - tables.baseSerial)
        val names = (tables.eventStart[k] until tables.eventStart[k + 1]).map { tables.names[tables.eventNames[it]].en }
        assertEquals(listOf("New Year", "Mesh Sankranti", "Bisket Jatra"), names)
    }

    @Test
    fun `a gap inside the tithi range fails`() {
        val synthetic = (0 until 5).map { PatroDay(100 + it, if (it == 2) null else tithi(it), it == 2, emptyList()) }
        val error = assertFailsWith<CodegenException> { buildPatroTables(synthetic) }
        assertTrue("102" in error.message!!, error.message)
    }

    @Test
    fun `more than 32 tithi pairs fail`() {
        assertEquals(32, buildPatroTables((0 until 32).map { PatroDay(it, tithi(it), false, emptyList()) }).names.size)
        val error =
            assertFailsWith<CodegenException> {
                buildPatroTables((0 until 33).map { PatroDay(it, tithi(it), false, emptyList()) })
            }
        assertTrue("33" in error.message!!, error.message)
    }

    @Test
    fun `an event pair equal to a tithi pair reuses its index`() {
        val data = buildPatroTables(listOf(PatroDay(5, tithi(0), false, listOf(tithi(0), NamePair("a", "b")))))
        assertEquals(listOf(0, 1), data.eventNames)
        assertEquals(2, data.names.size)
    }

    @Test
    fun `chars are encoded 20 to a literal with escapes`() {
        val text = encodeChars((0 until 41).map { 'A'.code + it % 26 } + listOf('0'.code + 10, 0, 0xD7FF))
        val lines = text.trimEnd().lines()
        assertEquals(3, lines.size)
        assertEquals("    \"ABCDEFGHIJKLMNOPQRST\" +", lines[0])
        assertEquals("    \"O\\u003A\\u0000\\uD7FF\"", lines[2])
        assertFailsWith<CodegenException> { encodeChars(listOf(0xD800)) }
        assertFailsWith<CodegenException> { encodeChars(listOf(-1)) }
    }

    @Test
    fun `each name is its own literal and only backslash, quote and dollar are escaped`() {
        val text = renderPatro(tables)
        assertTrue("\n        \"World Day Against Human Trafficking\",\n" in text)
        assertTrue("\n        \"पञ्चमी\",\n" in text)
        val odd = renderPatro(buildPatroTables(listOf(PatroDay(0, NamePair("a\"b", "c\\d\$e"), false, emptyList()))))
        assertTrue("\n        \"a\\\"b\",\n        \"c\\\\d\\\$e\",\n" in odd, odd)
    }

    @Test
    fun `a date key outside its file year is named`() {
        val json = """{"schema": 1, "year": 2081, "days": {"2082-01-01": {"holiday": true}}}"""
        val error = assertFailsWith<CodegenException> { parsePatroFile(2081, json, calendar) }
        assertTrue("2082-01-01" in error.message!!, error.message)
    }

    @Test
    fun `a day without data is named`() {
        val json = """{"schema": 1, "year": 2081, "days": {"2081-01-01": {"holiday": false}}}"""
        val error = assertFailsWith<CodegenException> { parsePatroFile(2081, json, calendar) }
        assertTrue("no data" in error.message!!, error.message)
    }

    @Test
    fun `a name pair with an empty side is named`() {
        val json = """{"schema": 1, "year": 2081, "days": {"2081-01-01": {"events": [{"np": "", "en": "x"}]}}}"""
        val error = assertFailsWith<CodegenException> { parsePatroFile(2081, json, calendar) }
        assertTrue("events[0]" in error.message!!, error.message)
    }
}
