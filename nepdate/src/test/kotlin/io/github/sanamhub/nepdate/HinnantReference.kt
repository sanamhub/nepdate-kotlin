package io.github.sanamhub.nepdate

/**
 * ALGORITHM section 5 line for line, on Long: the reference the table-based conversion of
 * ADR-0002 section 3 is checked against. Test sources only; the core never uses it.
 */
object HinnantReference {
    fun daysFromCivil(
        year: Long,
        month: Long,
        day: Long,
    ): Long {
        var y = year
        if (month <= 2) y = y - 1
        val era = y / 400
        val yoe = y - era * 400
        val mp = (month + 9) % 12
        val doy = (153 * mp + 2) / 5 + day - 1
        val doe = yoe * 365 + yoe / 4 - yoe / 100 + doy
        return era * 146097 + doe - 719468
    }

    fun civilFromDays(days: Long): Triple<Long, Long, Long> {
        val z = days + 719468
        val era = z / 146097
        val doe = z - era * 146097
        val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
        var y = yoe + era * 400
        val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
        val mp = (5 * doy + 2) / 153
        val d = doy - (153 * mp + 2) / 5 + 1
        val m = if (mp < 10) mp + 3 else mp - 9
        if (m <= 2) y = y + 1
        return Triple(y, m, d)
    }
}
