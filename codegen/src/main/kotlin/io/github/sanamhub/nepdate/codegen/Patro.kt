package io.github.sanamhub.nepdate.codegen

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject

const val PATRO_DIR = "shared/data/patro"
const val PATRO_OUTPUT = "nepdate-patro/src/main/kotlin/io/github/sanamhub/nepdate/patro/Patro.generated.kt"

private const val SCHEMA = 1
private const val MAX_TITHI_PAIRS = 32
private const val VALUES_PER_LITERAL = 20
private const val FIRST_SURROGATE = 0xD800
private val DATE_KEY = Regex("""(\d{4})-(\d{2})-(\d{2})""")

/** A (Nepali, English) name pair, as in PATRO section 1. */
data class NamePair(
    val np: String,
    val en: String,
)

/** One BS day of `shared/data/patro`, with its serial (ALGORITHM section 4). */
class PatroDay(
    val serial: Int,
    val tithi: NamePair?,
    val holiday: Boolean,
    val events: List<NamePair>,
)

/** The patro tables (PATRO §3). Offsets count days from [baseSerial]; name indexes count pairs in [names]. */
class PatroTablesData(
    val baseSerial: Int,
    val span: Int,
    val tithiFirstOffset: Int,
    val tithiIndex: List<Int>,
    val holidays: List<Int>,
    val eventDays: List<Int>,
    val eventStart: List<Int>,
    val eventNames: List<Int>,
    val names: List<NamePair>,
)

private fun expect(
    condition: Boolean,
    message: () -> String,
) {
    if (!condition) throw CodegenException(message())
}

/** Serial of BS [year]-[month]-[day], or null when the date doesn't exist. */
fun serialOf(
    calendar: CalendarData,
    year: Int,
    month: Int,
    day: Int,
): Int? {
    val lengths = calendar.monthLengths
    val valid = year in MIN_YEAR..MAX_YEAR && month in 1..12 && day in 1..lengths[year - MIN_YEAR][month - 1]
    if (!valid) return null
    var serial = 0
    for (y in MIN_YEAR until year) serial += lengths[y - MIN_YEAR].sum()
    for (m in 1 until month) serial += lengths[year - MIN_YEAR][m - 1]
    return serial + day - 1
}

private fun pair(
    value: Any?,
    where: String,
): NamePair {
    val obj = value as? JsonObject
    val np = (obj?.get("np") as? JsonPrimitive)?.takeIf { it.isString }?.content
    val en = (obj?.get("en") as? JsonPrimitive)?.takeIf { it.isString }?.content
    expect(obj != null && obj.keys == setOf("np", "en") && !np.isNullOrEmpty() && !en.isNullOrEmpty()) {
        "$where is not {\"np\": text, \"en\": text}"
    }
    return NamePair(np!!, en!!)
}

/** Days of one `<year>.json` file, validated against its [fileYear] and the calendar. */
fun parsePatroFile(
    fileYear: Int,
    text: String,
    calendar: CalendarData,
): List<PatroDay> {
    val file = "$fileYear.json"
    val root = Json.parseToJsonElement(text).jsonObject
    expect((root["schema"] as? JsonPrimitive)?.intOrNull == SCHEMA) { "$file: schema is not $SCHEMA" }
    expect((root["year"] as? JsonPrimitive)?.intOrNull == fileYear) { "$file: year is not $fileYear" }
    val days = root["days"] as? JsonObject
    expect(days != null) { "$file: days is missing" }
    return days!!.map { (key, value) ->
        val match = DATE_KEY.matchEntire(key)
        val (y, m, d) = match?.destructured?.toList()?.map { it.toInt() } ?: listOf(0, 0, 0)
        val serial = serialOf(calendar, y, m, d)
        expect(match != null && y == fileYear && serial != null) { "$file: $key is not a BS date of $fileYear" }
        val day = value as? JsonObject
        expect(day != null && day.keys.all { it in setOf("tithi", "holiday", "events") }) {
            "$file: $key has fields other than tithi, holiday and events"
        }
        val holiday = day!!["holiday"]?.let { (it as? JsonPrimitive)?.booleanOrNull }
        expect(day["holiday"] == null || holiday != null) { "$file: $key holiday is not a boolean" }
        val events = day["events"]?.let { it as? JsonArray }
        expect(day["events"] == null || events != null) { "$file: $key events is not an array" }
        val parsed =
            PatroDay(
                serial = serial!!,
                tithi = day["tithi"]?.let { pair(it, "$file: $key tithi") },
                holiday = holiday == true,
                events = events.orEmpty().mapIndexed { i, e -> pair(e, "$file: $key events[$i]") },
            )
        expect(parsed.tithi != null || parsed.holiday || parsed.events.isNotEmpty()) { "$file: $key has no data" }
        parsed
    }
}

/** Builds the patro tables from the days of every file, in any order. */
fun buildPatroTables(allDays: List<PatroDay>): PatroTablesData {
    expect(allDays.isNotEmpty()) { "$PATRO_DIR has no days" }
    val days = allDays.sortedBy { it.serial }
    days.zipWithNext().forEach { (a, b) -> expect(a.serial != b.serial) { "serial ${a.serial} appears twice" } }
    val base = days.first().serial
    val span = days.last().serial - base + 1

    val tithiDays = days.filter { it.tithi != null }
    expect(tithiDays.isNotEmpty()) { "$PATRO_DIR has no tithi" }
    val tithiFirst = tithiDays.first().serial
    tithiDays.forEachIndexed { i, day ->
        expect(day.serial == tithiFirst + i) { "tithi missing on serial ${tithiFirst + i} (PATRO section 3)" }
    }

    val index = LinkedHashMap<NamePair, Int>()
    for (day in tithiDays) index.getOrPut(day.tithi!!) { index.size }
    expect(index.size <= MAX_TITHI_PAIRS) { "${index.size} distinct tithi pairs, at most $MAX_TITHI_PAIRS" }
    for (day in days) for (event in day.events) index.getOrPut(event) { index.size }

    val eventDays = days.filter { it.events.isNotEmpty() }
    val eventStart = eventDays.runningFold(0) { total, day -> total + day.events.size }
    return PatroTablesData(
        baseSerial = base,
        span = span,
        tithiFirstOffset = tithiFirst - base,
        tithiIndex = tithiDays.map { index.getValue(it.tithi!!) },
        holidays = days.filter { it.holiday }.map { it.serial - base },
        eventDays = eventDays.map { it.serial - base },
        eventStart = eventStart,
        eventNames = eventDays.flatMap { day -> day.events.map { index.getValue(it) } },
        names = index.keys.toList(),
    )
}

/**
 * [values] as Kotlin string literals of at most 20 characters joined with `+`, one literal per line.
 * Characters other than ASCII letters and digits are written as `\uXXXX`.
 */
fun encodeChars(values: List<Int>): String {
    values.forEach { expect(it in 0 until FIRST_SURROGATE) { "value $it is not in 0 until 0xD800" } }
    if (values.isEmpty()) return "    \"\"\n"
    return values.chunked(VALUES_PER_LITERAL).joinToString(" +\n", postfix = "\n") { chunk ->
        chunk.joinToString("", prefix = "    \"", postfix = "\"") { value ->
            val c = value.toChar()
            if (c in 'a'..'z' || c in 'A'..'Z' || c in '0'..'9') c.toString() else "\\u%04X".format(value)
        }
    }
}

private fun nameLiteral(text: String): String =
    buildString {
        append('"')
        for (c in text) {
            when (c) {
                '\\', '"', '$' -> append('\\').append(c)
                else -> append(c)
            }
        }
        append('"')
    }

fun renderPatro(data: PatroTablesData): String =
    buildString {
        append(HEADER)
        append("package io.github.sanamhub.nepdate.patro\n\n")
        append("/** Serial (ALGORITHM section 4) of the first day with any patro data. */\n")
        append("internal const val BASE_SERIAL: Int = ${data.baseSerial}\n\n")
        append("/** Days from BASE_SERIAL to the last day with any patro data, both included. */\n")
        append("internal const val SPAN: Int = ${data.span}\n\n")
        append("/** Offset of the first day with a tithi; every later day up to the last tithi has one. */\n")
        append("internal const val TITHI_FIRST_OFFSET: Int = ${data.tithiFirstOffset}\n\n")
        append("/** One char per tithi day from TITHI_FIRST_OFFSET: '0' + its pair index in NAMES. */\n")
        append("internal const val TITHI_INDEX_DATA: String =\n")
        append(encodeChars(data.tithiIndex.map { '0'.code + it }))
        append("\n/** One char per public holiday: its offset, ascending. */\n")
        append("internal const val HOLIDAY_DATA: String =\n")
        append(encodeChars(data.holidays))
        append("\n/** One char per day with events: its offset, ascending. */\n")
        append("internal const val EVENT_DAY_DATA: String =\n")
        append(encodeChars(data.eventDays))
        append("\n/** Index in EVENT_NAME_DATA of each event day's first event, then the number of events. */\n")
        append("internal const val EVENT_START_DATA: String =\n")
        append(encodeChars(data.eventStart))
        append("\n/** One char per event, in file order: its pair index in NAMES. */\n")
        append("internal const val EVENT_NAME_DATA: String =\n")
        append(encodeChars(data.eventNames))
        append("\n/** Distinct (Nepali, English) pairs flattened: the tithi pairs first, then the event pairs. */\n")
        append("internal val NAMES: Array<String> =\n")
        append("    arrayOf(\n")
        for (pair in data.names) {
            append("        ").append(nameLiteral(pair.np)).append(",\n")
            append("        ").append(nameLiteral(pair.en)).append(",\n")
        }
        append("    )\n")
    }
