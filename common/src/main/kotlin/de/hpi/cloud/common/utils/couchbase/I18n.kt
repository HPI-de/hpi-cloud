package de.hpi.cloud.common.utils.couchbase

import de.hpi.cloud.common.utils.mapOfNotNull

fun i18nSingle(content: String?, language: String) = content?.let { mapOf(language to content) } ?: emptyMap()
fun i18nMap(de: String? = null, en: String? = null) = mapOfNotNull(
    de?.let { "de" to it },
    en?.let { "en" to it }
)
