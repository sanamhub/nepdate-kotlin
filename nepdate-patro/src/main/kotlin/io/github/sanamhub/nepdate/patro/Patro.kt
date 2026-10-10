package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.NepaliDate
import io.github.sanamhub.nepdate.NepaliDateRange
import java.util.Collections

/** Days from the serial epoch (BS 1901-01-01) to 1970-01-01: serial = epoch day + 45920 (ADR-0002 section 8). */
private const val SERIAL_OF_UNIX_EPOCH = 45920

/**
 * The patro (Nepali almanac): tithi, public holidays and events per BS day, from the hub's
 * `data/patro` (PATRO section 2). Data covers [coverage]; other days have no data.
 */
public object Patro {
    /**
     * What the patro says about [date]. Never throws and never allocates: the answer for each day is
     * built once, on first use of the patro tables.
     *
     * @return the day's tithi, holiday flag and events; for a day without data, no tithi, `false`
     * and an empty list.
     */
    @JvmStatic
    public fun info(date: NepaliDate): CalendarInfo {
        val offset = date.toEpochDay().toInt() + SERIAL_OF_UNIX_EPOCH - BASE_SERIAL
        return if (offset < 0 || offset >= SPAN) EMPTY_INFO else PatroTables.infoByDay[offset]
    }

    /**
     * The smallest range holding every day with any data: 2077-01-01..2084-08-20 with the current data.
     *
     * @return the covered days.
     */
    @JvmStatic
    public val coverage: NepaliDateRange =
        NepaliDateRange.of(
            NepaliDate.ofEpochDay((BASE_SERIAL - SERIAL_OF_UNIX_EPOCH).toLong()),
            NepaliDate.ofEpochDay((BASE_SERIAL + SPAN - 1 - SERIAL_OF_UNIX_EPOCH).toLong()),
        )

    /**
     * Every public holiday in ascending order, built on first use.
     *
     * @return an unmodifiable list of the holiday dates.
     */
    @JvmStatic
    public val publicHolidays: List<NepaliDate>
        get() = HolidayList.dates
}

/** Holds the holiday dates apart from [Patro], so they are only created when someone asks (K4-03). */
private object HolidayList {
    val dates: List<NepaliDate> =
        Collections.unmodifiableList(
            ArrayList<NepaliDate>(HOLIDAY_DATA.length).apply {
                for (c in HOLIDAY_DATA) add(NepaliDate.ofEpochDay((BASE_SERIAL + c.code - SERIAL_OF_UNIX_EPOCH).toLong()))
            },
        )
}
