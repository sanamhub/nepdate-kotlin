package io.github.sanamhub.nepdate

import kotlin.test.Test
import kotlin.test.assertEquals

class NamesTest {
    @Test
    fun `month names of FORMATTING section 1`() {
        val rows =
            listOf(
                Triple("Baishakh", "Bai", "बैशाख"),
                Triple("Jestha", "Jes", "जेठ"),
                Triple("Ashad", "Asa", "असार"),
                Triple("Shrawan", "Shr", "साउन"),
                Triple("Bhadra", "Bha", "भदौ"),
                Triple("Ashoj", "Aso", "असोज"),
                Triple("Kartik", "Kar", "कार्तिक"),
                Triple("Mangsir", "Man", "मंसिर"),
                Triple("Poush", "Pou", "पुष"),
                Triple("Magh", "Mag", "माघ"),
                Triple("Falgun", "Fal", "फागुन"),
                Triple("Chaitra", "Cha", "चैत"),
            )
        for ((month, row) in Month.entries.zip(rows)) {
            assertEquals(row.first, month.displayName())
            assertEquals(row.first, month.displayName(Lang.ENGLISH))
            assertEquals(row.second, month.shortName())
            assertEquals(row.third, month.displayName(Lang.NEPALI))
            assertEquals(row.third, month.shortName(Lang.NEPALI))
        }
        assertEquals("Asa", Month.ASHAD.shortName())
        assertEquals("Aso", Month.ASHOJ.shortName())
    }

    @Test
    fun `weekday names of FORMATTING section 1`() {
        val rows =
            listOf(
                listOf("Sunday", "Sun", "आइतवार", "आइत"),
                listOf("Monday", "Mon", "सोमवार", "सोम"),
                listOf("Tuesday", "Tue", "मङ्गलवार", "मङ्गल"),
                listOf("Wednesday", "Wed", "बुधवार", "बुध"),
                listOf("Thursday", "Thu", "बिहिवार", "बिहि"),
                listOf("Friday", "Fri", "शुक्रवार", "शुक्र"),
                listOf("Saturday", "Sat", "शनिवार", "शनि"),
            )
        for ((day, row) in Weekday.entries.zip(rows)) {
            assertEquals(row[0], day.displayName())
            assertEquals(row[1], day.shortName())
            assertEquals(row[2], day.displayName(Lang.NEPALI))
            assertEquals(row[3], day.shortName(Lang.NEPALI))
        }
    }

    @Test
    fun `nepali digits`() {
        assertEquals("२०८१", nepaliDigits(2081))
        assertEquals("०५", nepaliDigits(5, 2))
        assertEquals("-१२", nepaliDigits(-12))
        assertEquals("-००१२", nepaliDigits(-12, 4))
        assertEquals("०", nepaliDigits(0))
        assertEquals("५", nepaliDigits(5, -3))
        assertEquals("-९२२३३७२०३६८५४७७५८०८", nepaliDigits(Long.MIN_VALUE))
        assertEquals("९२२३३७२०३६८५४७७५८०७", nepaliDigits(Long.MAX_VALUE))
        assertEquals("०१२३४५६७८९", nepaliDigits(123456789, 10))
    }
}
