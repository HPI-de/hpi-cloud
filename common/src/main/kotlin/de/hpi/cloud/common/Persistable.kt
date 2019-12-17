package de.hpi.cloud.common

import com.google.protobuf.GeneratedMessageV3

abstract class Persistable<P : Persistable<P>> {
    interface ProtoSerializer<P : Persistable<P>, Proto : GeneratedMessageV3> {
        fun fromProto(proto: Proto, context: Context): P
        fun toProto(persistable: P, context: Context): Proto
    }
}
