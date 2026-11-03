package io.github.sanamhub.nepdate.serialization

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.contextual
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** A user type with a date field: the reason this artifact exists. */
@Serializable
private data class Festival(
    val name: String,
    @Serializable(with = NepaliDateSerializer::class) val date: NepaliDate,
)

class SerializationVectorTest {
    private val rows: List<Map<String, String>> by lazy {
        val root =
            System.getProperty("nepdate.shared")
                ?: error("system property nepdate.shared is not set; run the tests through Gradle")
        val lines =
            File(root, "spec/vectors/csharp-golden/serialization.tsv")
                .readLines(Charsets.UTF_8)
                .filter { it.isNotEmpty() && !it.startsWith("#") }
        val header = lines.first().split('\t')
        lines.drop(1).map { line ->
            val cells = line.split('\t')
            check(cells.size == header.size) { "serialization.tsv: ${cells.size} cells in $line" }
            header.zip(cells).toMap()
        }
    }

    @Test
    fun `every row of serialization tsv`() {
        assertEquals(11, rows.size)
        for (row in rows) {
            val kind = row.getValue("kind")
            val input = row.getValue("input")
            val json = row.getValue("json")
            val result = row.getValue("result")
            val context = row.toString()
            when (kind) {
                "write" -> assertEquals(json, Json.encodeToString(NepaliDateSerializer, NepaliDate.parse(input)), context)
                "read" ->
                    if (result == "ERR") {
                        // A JSON null has no representation (D-01); a bad date string throws the
                        // date error rather than producing a default value.
                        if (input == "null") {
                            assertFailsWith<SerializationException>(context) {
                                Json.decodeFromString(NepaliDateSerializer, input)
                            }
                        } else {
                            assertFailsWith<NepDateException>(context) {
                                Json.decodeFromString(NepaliDateSerializer, input)
                            }
                        }
                    } else {
                        assertEquals(NepaliDate.parse(result), Json.decodeFromString(NepaliDateSerializer, input), context)
                    }
                "roundtrip" -> {
                    val written = Json.encodeToString(NepaliDateSerializer, NepaliDate.parse(input))
                    assertEquals(json, written, context)
                    assertEquals(NepaliDate.parse(result), Json.decodeFromString(NepaliDateSerializer, written), context)
                }
                else -> error("unknown kind '$kind' in $context")
            }
        }
    }

    @Test
    fun `a bad date string throws the date error, not a made up date`() {
        assertEquals(
            NepDateException.Kind.INVALID_DAY,
            assertFailsWith<NepDateException> { Json.decodeFromString(NepaliDateSerializer, "\"2081-01-32\"") }.kind,
        )
    }

    @Test
    fun `the descriptor is a string named after the date`() {
        assertEquals("io.github.sanamhub.nepdate.NepaliDate", NepaliDateSerializer.descriptor.serialName)
        assertEquals(PrimitiveKind.STRING, NepaliDateSerializer.descriptor.kind)
    }

    @Test
    fun `a date field in a user type round trips`() {
        val festival = Festival("New Year", NepaliDate.of(2081, 1, 1))
        val text = Json.encodeToString(festival)
        assertEquals("""{"name":"New Year","date":"2081-01-01"}""", text)
        assertEquals(festival, Json.decodeFromString<Festival>(text))
    }

    @Test
    fun `a contextual registration makes the plain field type work`() {
        @Serializable
        data class Holiday(
            val name: String,
            @Contextual val date: NepaliDate,
        )

        val json =
            Json {
                serializersModule = SerializersModule { contextual(NepaliDate::class, NepaliDateSerializer) }
            }
        val holiday = Holiday("Republic Day", NepaliDate.of(2081, 2, 15))
        val text = json.encodeToString(holiday)
        assertEquals("""{"name":"Republic Day","date":"2081-02-15"}""", text)
        assertEquals(holiday, json.decodeFromString<Holiday>(text))
    }
}
