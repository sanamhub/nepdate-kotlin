package io.github.sanamhub.nepdate.patro

import java.util.Collections

/**
 * The decoded patro tables and every day's answer, built once on first use (ADR-0002 section 5).
 * JVM class initialisation is lazy and thread-safe (JLS 12.4.2), so no lock is needed.
 */
internal object PatroTables {
    /** The distinct name pairs; index i is the pair at `NAMES[2 * i]`, `NAMES[2 * i + 1]`. */
    @JvmField val pairs: Array<Named>

    /** Every event in file order, sharing the [pairs] objects. */
    @JvmField val events: Array<Named>

    /** One answer per day from BASE_SERIAL, [SPAN] entries. */
    @JvmField val infoByDay: Array<CalendarInfo>

    /** Distinct tithi pairs: the tithi indexes are `0` until this count. */
    private val tithiPairs: Int = TITHI_INDEX_DATA.maxOf { it.code } - '0'.code + 1

    init {
        pairs = Array(NAMES.size / 2) { Named(NAMES[2 * it], NAMES[2 * it + 1]) }
        events = Array(EVENT_NAME_DATA.length) { pairs[EVENT_NAME_DATA[it].code] }

        val tithiOnly = arrayOfNulls<CalendarInfo>(tithiPairs)
        var holiday = 0
        var eventDay = 0
        infoByDay =
            Array(SPAN) { offset ->
                val t = offset - TITHI_FIRST_OFFSET
                val tithiIndex = if (t >= 0 && t < TITHI_INDEX_DATA.length) TITHI_INDEX_DATA[t].code - '0'.code else -1
                val tithi = if (tithiIndex >= 0) pairs[tithiIndex] else null
                val isHoliday = holiday < HOLIDAY_DATA.length && HOLIDAY_DATA[holiday].code == offset
                if (isHoliday) holiday++
                val hasEvents = eventDay < EVENT_DAY_DATA.length && EVENT_DAY_DATA[eventDay].code == offset
                when {
                    hasEvents -> {
                        val from = EVENT_START_DATA[eventDay].code
                        val to = EVENT_START_DATA[eventDay + 1].code
                        eventDay++
                        CalendarInfo(tithi, isHoliday, EventList(events, from, to))
                    }

                    isHoliday -> {
                        CalendarInfo(tithi, true, Collections.emptyList())
                    }

                    tithi != null -> {
                        tithiOnly[tithiIndex] ?: CalendarInfo(tithi, false, Collections.emptyList()).also { tithiOnly[tithiIndex] = it }
                    }

                    else -> {
                        EMPTY_INFO
                    }
                }
            }
    }
}
