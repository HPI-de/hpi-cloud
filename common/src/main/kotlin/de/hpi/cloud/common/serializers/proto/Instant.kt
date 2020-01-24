package de.hpi.cloud.common.serializers.proto

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.types.Instant
import com.google.protobuf.Timestamp as ProtoInstant

object InstantSerializer : Persistable.ProtoSerializer<Instant, ProtoInstant> {
    override fun fromProto(proto: ProtoInstant, context: Context): Instant =
        Instant.fromSecondsNanos(proto.seconds, proto.nanos.toLong())

    override fun toProto(persistable: Instant, context: Context): ProtoInstant =
        ProtoInstant.newBuilder().build(persistable) {
            seconds = it.seconds
            nanos = it.nanos
        }
}

fun Instant.toProto(context: Context): ProtoInstant =
    InstantSerializer.toProto(this, context)
