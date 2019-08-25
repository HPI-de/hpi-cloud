package de.hpi.cloud.common.utils

// region List
fun <E : Any> MutableList<out E>.removeFirst(filter: (E) -> Boolean): E? {
    val index = this.indexOfFirst(filter)
    return if (index != -1) {
        val elem = this[index]
        this.removeAt(index)
        elem
    } else null
}
// endregion

// region Map
fun <K, V> mapOfNotNull(vararg pairs: Pair<K, V>?): Map<K, V> {
    return pairs.filterNotNull().toMap()
}
// endregion