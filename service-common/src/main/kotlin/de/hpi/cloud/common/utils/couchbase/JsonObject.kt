package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.document.json.JsonArray
import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.Timestamp
import de.hpi.cloud.common.v1test.Image

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

val LANGUAGES = arrayOf("en", "de")
fun JsonObject.getI18nString(name: String, preferredLanguage: String = "en"): String? {
    val obj = getNestedObject(name)
    return obj?.getString(preferredLanguage)
        ?: LANGUAGES.asSequence()
            .filter { it != preferredLanguage }
            .map { obj?.getString(it) }
            .firstOrNull { it != null }
        ?: (obj?.toMap()?.values?.firstOrNull() as? String)
}
// endregion

// region Primitives
fun JsonObject.getNestedInt(name: String): Int? {
    return getNested(name, JsonObject::getInt)
}
// endregion


const val TIMESTAMP_MILLIS = "millis"
const val TIMESTAMP_NANOS = "nanos"
fun JsonObject.getTimestamp(name: String): Timestamp? {
    val obj = getNestedObject(name) ?: return null
    val millis = obj.getLong(TIMESTAMP_MILLIS) ?: return null
    val nanos = obj.getInt(TIMESTAMP_NANOS) ?: return null
    return Timestamp.newBuilder()
        .setSeconds(millis / 1000)
        .setNanos((millis % 1000).toInt() * 1000000 + nanos)
        .build()
}

enum class ImageSize {
    ORIGINAL
}

fun JsonObject.getImage(name: String, size: ImageSize = ImageSize.ORIGINAL, preferredLanguage: String = "en"): Image? {
    val obj = getNestedObject(name) ?: return null
    val source = obj.getObject("source").getString(size.name.toLowerCase()) ?: return null
    return Image.newBuilder()
        .setSource(source)
        .setAlt(obj.getI18nString("alt", preferredLanguage))
        .apply {
            obj.getDouble("aspectRatio")?.toFloat()?.let {
                aspectRatio = it
            }
        }
        .build()
}

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
