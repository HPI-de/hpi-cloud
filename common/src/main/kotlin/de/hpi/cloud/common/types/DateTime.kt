package de.hpi.cloud.common.types

import com.google.protobuf.Timestamp
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.time.Instant as RawInstant

@Serializable(with = Instant.JsonSerializer::class)
data class Instant(val value: RawInstant) : Persistable<Instant>() {
    companion object {
        const val MILLIS_IN_SECOND = 1_000
        const val NANOS_IN_MILLI = 1_000_000

        fun now(): Instant = Instant(RawInstant.now())

        fun fromMillisNanos(millis: Long, nanos: Int): Instant = Instant(
            value = RawInstant.ofEpochSecond(
                millis / MILLIS_IN_SECOND,
                (millis % MILLIS_IN_SECOND) * NANOS_IN_MILLI + nanos
            )
        )

        fun fromSecondsNanos(seconds: Long, nanos: Long): Instant = Instant(
            value = RawInstant.ofEpochSecond(seconds, nanos)
        )
    }

    object ProtoSerializer : Persistable.ProtoSerializer<Instant, Timestamp> {
        override fun fromProto(proto: Timestamp, context: Context): Instant =
            fromSecondsNanos(proto.seconds, proto.nanos.toLong())

        override fun toProto(persistable: Instant, context: Context): Timestamp =
            Timestamp.newBuilder().build(persistable) {
                val (s, n) = it.secondsNanos
                seconds = s
                nanos = n
            }
    }

    @Serializer(forClass = Instant::class)
    object JsonSerializer : KSerializer<Instant> {
        const val KEY_MILLIS = "millis"
        const val KEY_NANOS = "nanos"

        override val descriptor = object : SerialClassDescImpl("Instant") {
            init {
                addElement(KEY_MILLIS)
                addElement(KEY_NANOS)
            }
        }

        override fun serialize(encoder: Encoder, obj: Instant) {
            val (millis, nanos) = obj.millisNanos
            encoder.beginStructure(descriptor).let {
                it.encodeLongElement(descriptor, 0, millis)
                it.encodeIntElement(descriptor, 1, nanos)
                it.endStructure(descriptor)
            }
        }

        override fun deserialize(decoder: Decoder): Instant {
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
        get() = value.epochSecond to value.nano
    val millisNanos: Pair<Long, Int>
        get() {
            val millis = value.epochSecond * MILLIS_IN_SECOND + value.nano / NANOS_IN_MILLI
            val nanos = value.nano % NANOS_IN_MILLI
            return millis to nanos
        }
}

fun Instant.toProto(context: Context): Timestamp = Instant.ProtoSerializer.toProto(this, context)
