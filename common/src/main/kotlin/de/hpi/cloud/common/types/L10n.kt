package de.hpi.cloud.common.types

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.serializers.json.L10nSerializer
import de.hpi.cloud.common.utils.bestMatch
import de.hpi.cloud.common.utils.locale
import de.hpi.cloud.common.utils.mapOfNotNull
import kotlinx.serialization.Serializable
import java.util.*

@Serializable(with = L10nSerializer::class)
data class L10n<T : Any>(
    val values: Map<Locale, T>
) {
    companion object {
        fun <T : Any> single(locale: Locale, value: T): L10n<T> = L10n(mapOf(locale to value))

        fun <T : Any> single(context: Context, value: T): L10n<T> = single(context.languageRanges.locale, value)

        fun <T : Any> from(en: T? = null, de: T? = null): L10n<T> =
            L10n(
                values = mapOfNotNull(
                    en?.let { Locale.ENGLISH to en },
                    de?.let { Locale.GERMAN to de }
                )
            )

        fun <T : Any> empty(): L10n<T> = L10n(emptyMap())
    }

    operator fun get(context: Context): T = get(context.languageRanges)
    operator fun get(languageRanges: List<Locale.LanguageRange>): T =
        values.getValue(values.keys.toList().bestMatch(languageRanges))
}

fun <T : Any> T?.l10n(locale: Locale): L10n<T> =
    if (this == null) L10n.empty()
    else L10n.single(locale, this)

fun <T : Any> T?.l10n(context: Context): L10n<T> = l10n(context.languageRanges.locale)
