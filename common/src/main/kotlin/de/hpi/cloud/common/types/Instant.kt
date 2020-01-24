package de.hpi.cloud.common.types

import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.serializers.json.InstantSerializer
import kotlinx.serialization.Serializable
import java.time.Instant as RawInstant

@Serializable(with = InstantSerializer::class)
data class Instant(
    val rawValue: RawInstant
) : Persistable<Instant>() {
    companion object {
        fun now(): Instant = Instant(RawInstant.now())

        fun fromSecondsNanos(seconds: Long, nanos: Long): Instant =
            Instant(RawInstant.ofEpochSecond(seconds, nanos))

        fun parseIsoString(isoString: String): Instant =
            Instant(RawInstant.parse(isoString))
    }

    val seconds: Long get() = rawValue.epochSecond
    val nanos: Int get() = rawValue.nano
}
