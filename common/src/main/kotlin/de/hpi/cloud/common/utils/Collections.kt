package de.hpi.cloud.common.utils

import java.util.*

// region Array
fun ByteArray.encodeBase64(): String = Base64.getEncoder().encodeToString(this)

fun String.decodeBase64(): ByteArray = Base64.getDecoder().decode(this)
// endregion

// region List
fun <E : Any> MutableList<out E>.removeFirst(filter: (E) -> Boolean): E? {
    val index = indexOfFirst(filter)
    return if (index != -1) {
        val elem = this[index]
        removeAt(index)
        elem
    } else null
}
// endregion

// region Map
fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> {
    return pairs.filterNotNull().toMap()
}

fun <K, V> Map<K, V?>.getOrElse(key: K, alternativeValue: () -> V?): V? =
    if (key in keys) this[key] else alternativeValue()
// endregion
