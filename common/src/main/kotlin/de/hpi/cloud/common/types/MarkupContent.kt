package de.hpi.cloud.common.types

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.serializers.EnumSerializer
import kotlinx.serialization.Serializable
import de.hpi.cloud.common.v1test.MarkupContent as ProtoMarkupContent

@Serializable
data class MarkupContent(
    val type: Type,
    val content: String
) : Persistable<MarkupContent>() {
    companion object {
        fun plain(content: String): MarkupContent = MarkupContent(Type.PLAIN, content)
        fun html(content: String): MarkupContent = MarkupContent(Type.HTML, content)
        fun markdown(content: String): MarkupContent = MarkupContent(Type.MARKDOWN, content)
    }

    object ProtoSerializer : Persistable.ProtoSerializer<MarkupContent, ProtoMarkupContent> {
        override fun fromProto(proto: ProtoMarkupContent, context: Context): MarkupContent {
            return MarkupContent(
                type = Type.fromMime(proto.type),
                content = proto.content
            )
        }

        override fun toProto(persistable: MarkupContent, context: Context): ProtoMarkupContent =
            ProtoMarkupContent.newBuilder().build(persistable) {
                type = it.type.mime
                content = it.content
            }
    }

    @Serializable(with = Type.Serializer::class)
    enum class Type(val mime: String) {
        PLAIN("text/plain"),
        HTML("text/html"),
        MARKDOWN("text/markdown");

        companion object {
            fun fromMime(mime: String, default: Type = PLAIN): Type {
                return values()
                    .firstOrNull { it.mime.equals(mime, ignoreCase = true) }
                    ?: default
            }
        }

        object Serializer : EnumSerializer<Type>(Type::class, PLAIN, { it.mime })
    }
}

fun MarkupContent.toProto(context: Context): ProtoMarkupContent = MarkupContent.ProtoSerializer.toProto(this, context)
