package io.github.sanamhub.nepdate.patro

import io.github.sanamhub.nepdate.Lang

/**
 * A tithi or event name in Nepali and English (PATRO section 2). Instances come from [Patro.info] and
 * are shared between days.
 *
 * @property nepali the name in Devanagari.
 * @property english the name in English.
 */
public class Named internal constructor(
    public val nepali: String,
    public val english: String,
) {
    /**
     * The name in [lang].
     *
     * @return [nepali] for [Lang.NEPALI], [english] for [Lang.ENGLISH].
     */
    @JvmOverloads
    public fun name(lang: Lang = Lang.ENGLISH): String = if (lang == Lang.NEPALI) nepali else english

    /** @return true when [other] is a [Named] with the same Nepali and English names. */
    override fun equals(other: Any?): Boolean = other is Named && nepali == other.nepali && english == other.english

    /** @return a hash of both names. */
    override fun hashCode(): Int = 31 * nepali.hashCode() + english.hashCode()

    /** @return the English name, then the Nepali name in parentheses, for example `Panchami (पञ्चमी)`. */
    override fun toString(): String = "$english ($nepali)"
}
