package de.hpi.cloud.common.utils

fun <K, V> Iterable<V>.groupingSections(
    ignoreKeyValues: Boolean = false,
    keyExtractor: (V) -> K?
): Map<K, List<V>> {
    val it = this.iterator()
    val map = mutableMapOf<K, MutableList<V>>()

    var key: K? = null
    while (it.hasNext()) {
        val v = it.next()
        val newKey = keyExtractor(v)
        if (newKey != null) {
            key = newKey
        }
        if (key == null) {
            continue
        } else if (newKey == null || !ignoreKeyValues) {
            map.getOrPut(key) { mutableListOf<V>() }
                .add(v)
        }
    }
    return map
}
