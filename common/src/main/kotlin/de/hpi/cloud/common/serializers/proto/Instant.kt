package de.hpi.cloud.common.serializers.proto

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.serializers.ProtoSerializer
import java.time.Instant
import com.google.protobuf.Timestamp as ProtoInstant

object InstantSerializer : ProtoSerializer<Instant, ProtoInstant> {
    override fun fromProto(proto: ProtoInstant, context: Context): Instant =
        Instant.ofEpochSecond(proto.seconds, proto.nanos.toLong())

    override fun toProto(persistable: Instant, context: Context): ProtoInstant =
        ProtoInstant.newBuilder().build(persistable) {
            seconds = it.epochSecond
            nanos = it.nano
        }
}

fun Instant.toProto(context: Context): ProtoInstant =
    InstantSerializer.toProto(this, context)
