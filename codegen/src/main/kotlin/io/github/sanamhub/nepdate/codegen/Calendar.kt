package io.github.sanamhub.nepdate.codegen

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

const val MIN_YEAR = 1901
const val MAX_YEAR = 2199
private const val EPOCH_BS = "1901-01-01"
private const val EPOCH_AD = "1844-04-11"
private const val MONTHS = 12
private val LENGTHS = 29..32

/** A problem in `shared/data` that stops generation; the message names the field or year. */
class CodegenException(
    message: String,
) : Exception(message)

/** Month lengths of BS 1901 to 2199 from `bs-calendar.json`, validated. */
class CalendarData(
    val monthLengths: List<List<Int>>,
)

private fun expect(
    condition: Boolean,
    message: () -> String,
) {
    if (!condition) throw CodegenException("bs-calendar.json: ${message()}")
}

fun parseCalendar(text: String): CalendarData {
    val root = Json.parseToJsonElement(text).jsonObject
    expect(root["min_year"]?.jsonPrimitive?.intOrNull == MIN_YEAR) { "min_year is not $MIN_YEAR" }
    expect(root["max_year"]?.jsonPrimitive?.intOrNull == MAX_YEAR) { "max_year is not $MAX_YEAR" }
    val epoch = root["epoch"] as? JsonObject
    expect(
        epoch?.get("bs")?.jsonPrimitive?.content == EPOCH_BS &&
            epoch["ad"]?.jsonPrimitive?.content == EPOCH_AD,
    ) { "epoch is not $EPOCH_BS BS = $EPOCH_AD AD" }
    val years = root["month_lengths"] as? JsonObject
    expect(years != null) { "month_lengths is missing" }
    val lengths =
        (MIN_YEAR..MAX_YEAR).map { year ->
            val row = years?.get(year.toString()) as? JsonArray
            expect(row != null) { "year $year is missing" }
            expect(row!!.size == MONTHS) { "year $year has ${row.size} month lengths, not $MONTHS" }
            row.mapIndexed { index, value ->
                val length = value.jsonPrimitive.intOrNull
                expect(length != null && length in LENGTHS) {
                    "year $year month ${index + 1} has length $value, not 29..32"
                }
                length!!
            }
        }
    return CalendarData(lengths)
}

fun renderCalendar(data: CalendarData): String {
    check(data.monthLengths.size == MAX_YEAR - MIN_YEAR + 1)
    return buildString {
        append(HEADER)
        append("package io.github.sanamhub.nepdate\n\n")
        append("internal const val MIN_YEAR: Int = $MIN_YEAR\n")
        append("internal const val MAX_YEAR: Int = $MAX_YEAR\n")
    }
}
