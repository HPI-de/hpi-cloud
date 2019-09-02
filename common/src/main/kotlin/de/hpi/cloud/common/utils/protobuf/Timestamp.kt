package de.hpi.cloud.common.utils.protobuf

import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.Timestamp
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import de.hpi.cloud.common.utils.grpc.buildWith
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
    return Timestamp.newBuilder().buildWith(getNestedObject(name)) {
        val dbMillis = it.getLong(TIMESTAMP_MILLIS) ?: return null
        val dbNanos = it.getInt(TIMESTAMP_NANOS) ?: return null
        seconds = dbMillis / 1000
        nanos = (dbMillis % 1000).toInt() * 1000000 + dbNanos
    }
}

fun LocalDate.toTimestamp() = atStartOfDay().toTimestamp()
fun LocalDateTime.toTimestamp() = timestampFromSeconds(toEpochSecond(ZoneOffset.UTC))

fun Timestamp.toDbMap() = mapOf(
    TIMESTAMP_MILLIS to (seconds * 1000 + nanos / 1000000),
    TIMESTAMP_NANOS to nanos % 1000000
)
