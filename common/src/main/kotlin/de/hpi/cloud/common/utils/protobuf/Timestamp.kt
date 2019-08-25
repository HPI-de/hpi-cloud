package de.hpi.cloud.common.utils.protobuf

import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.Timestamp
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

const val TIMESTAMP_MILLIS = "millis"
const val TIMESTAMP_NANOS = "nanos"

fun timestampNow() = LocalDateTime.now().toTimestamp()
fun timestampFromSeconds(seconds: Long, nanos: Int = 0): Timestamp = Timestamp.newBuilder()
    .setSeconds(seconds)
    .setNanos(nanos)
    .build()

fun timestampFromMillis(millis: Long, nanos: Int = 0) =
    timestampFromSeconds(millis / 1000, (millis % 1000).toInt() * 1000000 + nanos)

fun JsonObject.getTimestamp(name: String): Timestamp? {
    val obj = getNestedObject(name) ?: return null
    val millis = obj.getLong(TIMESTAMP_MILLIS) ?: return null
    val nanos = obj.getInt(TIMESTAMP_NANOS) ?: return null
    return Timestamp.newBuilder()
        .setSeconds(millis / 1000)
        .setNanos((millis % 1000).toInt() * 1000000 + nanos)
        .build()
}
fun LocalDate.toTimestamp() = atStartOfDay().toTimestamp()
fun LocalDateTime.toTimestamp() = timestampFromSeconds(toEpochSecond(ZoneOffset.UTC))

fun Timestamp.toDbMap(): Map<String, Number> {
    return mapOf(
        "millis" to (seconds * 1000 + nanos / 1000000),
        "nanos" to nanos % 1000000
    )
}
