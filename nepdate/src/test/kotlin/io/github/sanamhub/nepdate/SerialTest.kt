package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals

class SerialTest {
    private fun ymd(packed: Int) = Triple(packedYear(packed), packedMonth(packed), packedDay(packed))

    @Test
    fun `first and last serial`() {
        assertEquals(0, toSerial(1901, 1, 1))
        assertEquals(109211, toSerial(2199, 12, 30))
        assertEquals(MAX_SERIAL, toSerial(2199, 12, 30))
    }

    @Test
    fun `serial to BS at both ends`() {
        assertEquals(Triple(1901, 1, 1), ymd(packedBsOf(0)))
        assertEquals(Triple(2199, 12, 30), ymd(packedBsOf(109211)))
    }

    @Test
    fun `serial to AD at both ends`() {
        assertEquals(Triple(1844, 4, 11), ymd(packedAdOf(0)))
        assertEquals(Triple(2143, 4, 15), ymd(packedAdOf(109211)))
    }

    @Test
    fun `AD to serial at both ends`() {
        assertEquals(0, serialOfAd(1844, 4, 11))
        assertEquals(109211, serialOfAd(2143, 4, 15))
    }

    @Test
    fun `reference dates of ALGORITHM section 9`() {
        assertEquals(Triple(2024, 4, 13), ymd(packedAdOf(toSerial(2081, 1, 1))))
        assertEquals(Triple(2025, 4, 14), ymd(packedAdOf(toSerial(2082, 1, 1))))
        assertEquals(Triple(2026, 4, 14), ymd(packedAdOf(toSerial(2083, 1, 1))))
        assertEquals(Triple(2083, 1, 1), ymd(packedBsOf(serialOfAd(2026, 4, 14))))
    }

    @Test
    fun `packing keeps each field`() {
        assertEquals(Triple(2199, 12, 32), ymd(pack(2199, 12, 32)))
        assertEquals(Triple(2081, 4, 32), ymd(pack(2081, 4, 32)))
        assertEquals(Triple(1901, 1, 1), ymd(pack(1901, 1, 1)))
    }
}
