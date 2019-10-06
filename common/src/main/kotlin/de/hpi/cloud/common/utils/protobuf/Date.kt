package de.hpi.cloud.common.utils.protobuf

import com.couchbase.client.java.document.json.JsonObject
import com.google.type.Date
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Deprecated("Use JsonObject.getDateUsingIsoFormat(name) instead.", ReplaceWith("getDateUsingIsoFormat(name)"))
fun JsonObject.getDateUsingMillis(name: String): Date? {
    val millis = getNestedObject(name)?.getLong(TIMESTAMP_MILLIS) ?: return null
    val instant = Instant.ofEpochMilli(millis)
    return LocalDate.ofInstant(instant, ZoneOffset.UTC).toProtobufDate()
}

fun JsonObject.getDateUsingIsoFormat(name: String) = getLocalDate(name)?.toProtobufDate()

fun JsonObject.getLocalDate(name: String): LocalDate? {
    return try {
        LocalDate.parse(
            getString(name) ?: return null,
            DateTimeFormatter.ISO_DATE
        )
    } catch (ex: DateTimeParseException) {
        null
    }
}

fun LocalDate.toProtobufDate(): Date = Date.newBuilder()
    .setYear(year)
    .setMonth(monthValue)
    .setDay(dayOfMonth)
    .build()

@Deprecated("Use Date.toIsoString() instead.", ReplaceWith("toIsoString()"))
fun Date.toQueryString() = toIsoString()

fun Date.toIsoString() = String.format("%04d-%02d-%02d", year, month, day)
