package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.NepaliDate
import java.io.File

/**
 * The hub's patro counts, vendored as `shared/spec/vectors/patro-summary.json`. The file has one
 * `"key": value` per line and a fixed shape, so a keyed reader is enough: the test sources have no
 * JSON library and do not add one.
 */
internal object PatroSummary {
    private val fields: Map<String, String> by lazy {
        val root =
            System.getProperty("nepdate.shared")
                ?: error("system property nepdate.shared is not set; run the tests through Gradle")
        val file = File(root, "spec/vectors/patro-summary.json")
        check(file.isFile) { "$file is missing: re-sync shared/ from the hub, which generates it with the data" }
        file
            .readLines(Charsets.UTF_8)
            .mapNotNull { line ->
                val field = line.trim().trimEnd(',')
                val colon = field.indexOf(':')
                if (colon < 0) return@mapNotNull null
                field.substring(0, colon).trim().trim('"') to field.substring(colon + 1).trim().trim('"')
            }.toMap()
    }

    /** The count in [field]. */
    fun number(field: String): Int {
        val text = raw(field)
        return text.toIntOrNull() ?: error("patro-summary.json: $field is not a number: $text")
    }

    /** The text in [field]. */
    fun text(field: String): String = raw(field)

    /** The `"YYYY-MM-DD"` in [field], as `(year, month, day)`. */
    fun day(field: String): Triple<Int, Int, Int> {
        val text = raw(field)
        val parts = text.split('-')
        check(parts.size == 3) { "patro-summary.json: $field is not a date: $text" }
        val (year, month, day) =
            parts.map {
                it.toIntOrNull() ?: error("patro-summary.json: $field is not a date: $text")
            }
        return Triple(year, month, day)
    }

    private fun raw(field: String): String = fields[field] ?: error("patro-summary.json has no $field")
}

/** The BS date in [field] of [PatroSummary], as a [NepaliDate]. */
internal fun summaryDay(field: String): NepaliDate {
    val (year, month, day) = PatroSummary.day(field)
    return d(year, month, day)
}
