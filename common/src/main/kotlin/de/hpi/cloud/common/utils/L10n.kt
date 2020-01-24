package de.hpi.cloud.common.utils

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.types.L10n
import java.util.*

val LOCALE_FALLBACK: Locale = Locale.ENGLISH

fun <T : Any> T?.l10n(locale: Locale): L10n<T> =
    if (this == null) L10n.empty()
    else L10n.single(locale, this)

fun <T : Any> T?.l10n(context: Context): L10n<T> = l10n(context.languageRanges.locale)

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
