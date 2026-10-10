package io.github.sanamhub.nepdate.patro

import java.util.Collections

/**
 * What the patro says about one BS day (PATRO section 2). [Patro.info] returns one shared, immutable
 * instance per day; compare with `equals`, not `===`.
 *
 * @property tithi the lunar day, or null when the day has no tithi data.
 * @property isPublicHoliday true when the day is a gazetted public holiday.
 * @property events the day's events in display order; empty when there are none. The list can't be
 * modified.
 */
public class CalendarInfo internal constructor(
    public val tithi: Named?,
    public val isPublicHoliday: Boolean,
    public val events: List<Named>,
) {
    /** @return true when [other] is a [CalendarInfo] with an equal tithi, holiday flag and event list. */
    override fun equals(other: Any?): Boolean =
        other is CalendarInfo &&
            tithi == other.tithi &&
            isPublicHoliday == other.isPublicHoliday &&
            events == other.events

    /** @return a hash of the tithi, the holiday flag and the events. */
    override fun hashCode(): Int {
        var result = tithi?.hashCode() ?: 0
        result = 31 * result + if (isPublicHoliday) 1 else 0
        return 31 * result + events.hashCode()
    }

    /** @return the three fields, for example `CalendarInfo(tithi=Panchami (पञ्चमी), isPublicHoliday=true, events=[...])`. */
    override fun toString(): String = "CalendarInfo(tithi=$tithi, isPublicHoliday=$isPublicHoliday, events=$events)"
}

/** The answer for a day without data: no tithi, no holiday, no events. */
internal val EMPTY_INFO: CalendarInfo = CalendarInfo(null, false, Collections.emptyList())

/**
 * A read-only view of `all[from until to]`, so a day's events share the one event array (K4-03).
 * It extends the Java `AbstractList`, as `Collections.emptyList()` is Java: a Kotlin read-only list
 * cast to `MutableList` throws `ClassCastException`, where K4-04 asks for `UnsupportedOperationException`.
 */
internal class EventList(
    private val all: Array<Named>,
    private val from: Int,
    private val to: Int,
) : java.util.AbstractList<Named>(),
    RandomAccess {
    override val size: Int get() = to - from

    override fun get(index: Int): Named {
        if (index < 0 || index >= to - from) throw IndexOutOfBoundsException("index $index, size ${to - from}")
        return all[from + index]
    }
}
