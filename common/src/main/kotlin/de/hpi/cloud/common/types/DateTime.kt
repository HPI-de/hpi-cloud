package de.hpi.cloud.common.types

import com.google.protobuf.Timestamp
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.time.ZoneOffset
import java.time.LocalDateTime as RawLocalDateTime

@Serializable(with = LocalDateTime.JsonSerializer::class)
data class LocalDateTime(val value: RawLocalDateTime) : Persistable<LocalDateTime>() {
    companion object {
        const val MILLIS_IN_SECOND = 1_000
        const val NANOS_IN_MILLI = 1_000_000

        fun now(): LocalDateTime = LocalDateTime(RawLocalDateTime.now())

        fun fromMillisNanos(millis: Long, nanos: Int): LocalDateTime = LocalDateTime(
            value = RawLocalDateTime.ofEpochSecond(
                millis / MILLIS_IN_SECOND,
                (millis % MILLIS_IN_SECOND).toInt() * NANOS_IN_MILLI + nanos,
                ZoneOffset.UTC
            )
        )

        fun fromSecondsNanos(seconds: Long, nanos: Int): LocalDateTime = LocalDateTime(
            value = RawLocalDateTime.ofEpochSecond(seconds, nanos, ZoneOffset.UTC)
        )
    }

    object ProtoSerializer : Persistable.ProtoSerializer<LocalDateTime, Timestamp> {
        override fun fromProto(proto: Timestamp, context: Context): LocalDateTime =
            fromSecondsNanos(proto.seconds, proto.nanos)

        override fun toProto(persistable: LocalDateTime, context: Context): Timestamp =
            Timestamp.newBuilder().build(persistable) {
                val (s, n) = it.secondsNanos
                seconds = s
                nanos = n
            }
    }

    @Serializer(forClass = LocalDateTime::class)
    object JsonSerializer : KSerializer<LocalDateTime> {
        const val KEY_MILLIS = "millis"
        const val KEY_NANOS = "nanos"

        override val descriptor = object : SerialClassDescImpl("LocalDateTime") {
            init {
                addElement(KEY_MILLIS)
                addElement(KEY_NANOS)
            }
        }

        override fun serialize(encoder: Encoder, obj: LocalDateTime) {
            val (millis, nanos) = obj.millisNanos
            encoder.beginStructure(descriptor).let {
                it.encodeLongElement(descriptor, 0, millis)
                it.encodeIntElement(descriptor, 1, nanos)
                it.endStructure(descriptor)
            }
        }

        override fun deserialize(decoder: Decoder): LocalDateTime {
            val dec = decoder.beginStructure(descriptor)
            var millis: Long? = null
            var nanos: Int? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> millis = dec.decodeLongElement(descriptor, i)
                    1 -> nanos = dec.decodeIntElement(descriptor, i)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)

            return fromMillisNanos(
                millis = millis ?: throw MissingFieldException("millis"),
                nanos = nanos ?: throw MissingFieldException("nanos")
            )
        }
    }

    val secondsNanos: Pair<Long, Int>
        get() = value.toEpochSecond(ZoneOffset.UTC) to value.nano
    val millisNanos: Pair<Long, Int>
        get() {
            val millis = value.toEpochSecond(ZoneOffset.UTC) * MILLIS_IN_SECOND + value.nano / NANOS_IN_MILLI
            val nanos = value.nano % NANOS_IN_MILLI
            return millis to nanos
        }
}

fun LocalDateTime.toProto(context: Context): Timestamp = LocalDateTime.ProtoSerializer.toProto(this, context)
