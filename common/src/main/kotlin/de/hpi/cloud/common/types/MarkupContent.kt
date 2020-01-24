package de.hpi.cloud.common.types

import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.serializers.json.EnumSerializer
import kotlinx.serialization.Serializable

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
