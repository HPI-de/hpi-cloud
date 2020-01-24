package de.hpi.cloud.common.serializers

import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Context

interface ProtoSerializer<P : Any, Proto : GeneratedMessageV3> {
    fun fromProto(proto: Proto, context: Context): P
    fun toProto(persistable: P, context: Context): Proto
}
