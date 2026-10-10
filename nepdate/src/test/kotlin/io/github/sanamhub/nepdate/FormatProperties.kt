package io.github.sanamhub.nepdate

import net.jqwik.api.ForAll
import net.jqwik.api.Property
import net.jqwik.api.constraints.IntRange
import kotlin.test.assertEquals

private const val SEED = "20261010"

class FormatProperties {
    @Property(seed = SEED)
    fun `formatted dates parse back`(
        @ForAll @IntRange(min = 0, max = MAX_SERIAL) serial: Int,
    ) {
        val date = NepaliDate.ofEpochDay((serial + EPOCH_UNIX_DAYS).toLong())
        assertEquals(date, NepaliDate.parse(date.toString()))
        assertEquals(date, NepaliDate.parse(date.toIsoString().replace('-', '/')))
        assertEquals(date, NepaliDate.parseLenient(date.toLongString()))
        assertEquals(date, NepaliDate.parseLenient(date.toShortString(lang = Lang.NEPALI)))
    }
}
