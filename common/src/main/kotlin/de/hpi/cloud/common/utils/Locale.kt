package de.hpi.cloud.common.utils

import java.util.*

val LOCALE_FALLBACK: Locale = Locale.ENGLISH

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
