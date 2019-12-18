package de.hpi.cloud.common.types

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.serializers.LocaleSerializer
import kotlinx.serialization.*
import kotlinx.serialization.internal.LinkedHashMapSerializer
import kotlinx.serialization.internal.NamedMapClassDescriptor
import kotlinx.serialization.json.JsonElementSerializer
import java.util.*


val LOCALE_FALLBACK: Locale = Locale.ENGLISH

@Serializable(with = L10n.JsonSerializer::class)
data class L10n<T>(
    val values: Map<Locale, T>
) {
    companion object {
        fun <T> single(locale: Locale, value: T): L10n<T> = L10n(mapOf(locale to value))

        fun <T> single(context: Context, value: T): L10n<T> = single(context.languageRanges.locale, value)

        fun <T> from(en: T? = null, de: T? = null): L10n<T> =
            L10n(
                values = listOfNotNull(
                    en?.let { Locale.ENGLISH to en },
                    de?.let { Locale.GERMAN to de }
                ).toMap()
            )
    }

    operator fun get(context: Context): T = get(context.languageRanges)
    operator fun get(languageRanges: List<Locale.LanguageRange>): T =
        values.getValue(values.keys.toList().bestMatch(languageRanges))

    @Serializer(forClass = L10n::class)
    class JsonSerializer<T>(
        private val dataSerializer: KSerializer<T>
    ) : KSerializer<L10n<T>> {
        override val descriptor: SerialDescriptor =
            NamedMapClassDescriptor("L10n", LocaleSerializer.descriptor, JsonElementSerializer.descriptor)

        override fun serialize(encoder: Encoder, obj: L10n<T>) {
            LinkedHashMapSerializer(LocaleSerializer, dataSerializer).serialize(encoder, obj.values)
        }

        override fun deserialize(decoder: Decoder): L10n<T> {
            return L10n(LinkedHashMapSerializer(LocaleSerializer, dataSerializer).deserialize(decoder))
        }
    }
}

fun <T> T.l10n(locale: Locale = Locale.ENGLISH): L10n<T> = L10n.single(locale, this)
fun <T> T.l10n(context: Context): L10n<T> = l10n(context.languageRanges.locale)

fun String.parseLocale(): Locale = Locale.forLanguageTag(this)
fun List<Locale>.bestMatch(languageRanges: List<Locale.LanguageRange>): Locale {
    require(isNotEmpty()) { "At least one locale must be available" }

    val tagStrings = map { it.toString() }
    return Locale.lookupTag(languageRanges, tagStrings)
        ?.let { this[tagStrings.indexOf(it)] }
        ?: LOCALE_FALLBACK
        ?: first()
}

val List<Locale.LanguageRange>.locale: Locale
    get() = maxBy { it.weight }?.range?.parseLocale() ?: LOCALE_FALLBACK
