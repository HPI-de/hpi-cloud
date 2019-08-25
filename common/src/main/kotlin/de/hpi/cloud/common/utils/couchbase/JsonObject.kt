package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.document.json.JsonArray
import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.Timestamp
import com.google.type.Date
import com.google.type.Money
import de.hpi.cloud.common.utils.protobuf.TIMESTAMP_MILLIS
import de.hpi.cloud.common.v1test.Image
import java.util.*

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


fun JsonObject.getDate(name: String): Date? {
    val millis = getNestedObject(name)?.getLong(TIMESTAMP_MILLIS) ?: return null
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    return Date.newBuilder()
        .setYear(cal.get(Calendar.YEAR))
        .setMonth(cal.get(Calendar.MONTH) + 1)
        .setDay(cal.get(Calendar.DATE))
        .build()
}

fun Date.toQueryString() = String.format("%04d-%02d-%02d", year, month, day)


private const val MONEY_CURRENCY_CODE = "currencyCode"
private const val MONEY_UNITS = "units"
private const val MONEY_NANOS = "nanos"
fun JsonObject.getMoney(name: String): Money? {
    val obj = getNestedObject(name) ?: return null
    val currencyCode = obj.getString(MONEY_CURRENCY_CODE) ?: return null
    val units = obj.getLong(MONEY_UNITS) ?: return null
    val nanos = obj.getInt(MONEY_NANOS) ?: return null
    return Money.newBuilder()
        .setCurrencyCode(currencyCode)
        .setUnits(units)
        .setNanos(nanos)
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
