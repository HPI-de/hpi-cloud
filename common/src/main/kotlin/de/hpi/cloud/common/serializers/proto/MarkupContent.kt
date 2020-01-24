package de.hpi.cloud.common.serializers.proto

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.serializers.ProtoSerializer
import de.hpi.cloud.common.types.MarkupContent
import de.hpi.cloud.common.v1test.MarkupContent as ProtoMarkupContent

object MarkupContentSerializer : ProtoSerializer<MarkupContent, ProtoMarkupContent> {
    override fun fromProto(proto: ProtoMarkupContent, context: Context): MarkupContent {
        return MarkupContent(
            type = MarkupContent.Type.fromMime(proto.type),
            content = proto.content
        )
    }

    override fun toProto(persistable: MarkupContent, context: Context): ProtoMarkupContent =
        ProtoMarkupContent.newBuilder().build(persistable) {
            type = it.type.mime
            content = it.content
        }
}

fun MarkupContent.toProto(context: Context): ProtoMarkupContent =
    MarkupContentSerializer.toProto(this, context)
