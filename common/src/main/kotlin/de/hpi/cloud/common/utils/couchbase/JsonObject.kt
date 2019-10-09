package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.document.json.JsonArray
import com.couchbase.client.java.document.json.JsonObject

const val NESTED_SEPARATOR = '.'

fun <T> JsonObject.getNested(name: String, getter: JsonObject.(String) -> T): T? {
    val obj =
        if (!name.contains(NESTED_SEPARATOR)) this
        else getNestedObject(name.substringBeforeLast(NESTED_SEPARATOR, ""))
    return obj?.let { getter(name.substringAfterLast(NESTED_SEPARATOR)) }
}

// region Object
fun JsonObject.getNestedObject(name: String): JsonObject? {
    return name.split(NESTED_SEPARATOR).fold<String, JsonObject?>(this) { obj, part -> obj?.getObject(part) }
}
// endregion

// region String
fun JsonObject.getNestedString(name: String): String? {
    return getNested(name, JsonObject::getString)
}
// endregion

// region Primitives
fun JsonObject.getNestedInt(name: String): Int? {
    return getNested(name, JsonObject::getInt)
}
// endregion

// region Array
fun JsonObject.getNestedArray(name: String): JsonArray? {
    return getNested(name, JsonObject::getArray)
}

fun JsonObject.getStringArray(name: String): List<String?> {
    return getNestedArray(name)
        ?.toList()
        ?.map { it as? String }
        ?: emptyList()
}
// endregion
