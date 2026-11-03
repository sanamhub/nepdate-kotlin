package io.github.sanamhub.nepdate.serialization

import io.github.sanamhub.nepdate.NepDateException
import io.github.sanamhub.nepdate.NepaliDate
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * The kotlinx.serialization hook for [NepaliDate] (PARITY D-11): a date is one JSON string in the
 * canonical `YYYY-MM-DD` form of [NepaliDate.toIsoString], and nothing else.
 *
 * This is the only serialization form. The C# object form (`{ "Year", "Month", "Day" }`), the
 * Newtonsoft and XML adapters and the `TypeConverter` are not ported, and a JSON `null` has no
 * representation because a date is never absent (PARITY D-01).
 *
 * Use it on one property, or register it once and let `NepaliDate` be a plain field type.
 *
 * ```
 * @Serializable
 * data class Event(val name: String, @Serializable(with = NepaliDateSerializer::class) val date: NepaliDate)
 *
 * val json = Json { serializersModule = SerializersModule { contextual(NepaliDate::class, NepaliDateSerializer) } }
 * ```
 *
 * Decoding a string that is not a date, or a JSON `null`, throws [NepDateException] (for the text)
 * or `kotlinx.serialization.SerializationException` (for the null), never a made-up date.
 */
public object NepaliDateSerializer : KSerializer<NepaliDate> {
    /** `String`, described under the date's own class name. */
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("io.github.sanamhub.nepdate.NepaliDate", PrimitiveKind.STRING)

    /**
     * Writes [value] as `YYYY-MM-DD`.
     *
     * @param value the date, never absent
     */
    override fun serialize(
        encoder: Encoder,
        value: NepaliDate,
    ) {
        encoder.encodeString(value.toIsoString())
    }

    /**
     * Reads a `YYYY-MM-DD` string.
     *
     * @return the date
     * @throws NepDateException when the string is not a date
     */
    override fun deserialize(decoder: Decoder): NepaliDate = NepaliDate.parse(decoder.decodeString())
}
