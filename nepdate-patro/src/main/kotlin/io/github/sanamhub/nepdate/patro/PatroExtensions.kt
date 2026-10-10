@file:JvmName("NepaliDatePatro")

package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.NepaliDate

/**
 * What the patro says about this date; the same as [Patro.info].
 *
 * @return the day's tithi, holiday flag and events.
 */
public val NepaliDate.calendarInfo: CalendarInfo
    get() = Patro.info(this)
