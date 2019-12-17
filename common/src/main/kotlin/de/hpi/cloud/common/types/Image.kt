package de.hpi.cloud.common.types

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.serializers.EnumSerializer
import de.hpi.cloud.common.serializers.UriSerializer
import de.hpi.cloud.common.utils.parseUri
import kotlinx.serialization.Serializable
import java.net.URI
import de.hpi.cloud.common.v1test.Image as ProtoImage

@Serializable
data class Image(
    val source: Map<Size, @Serializable(with = UriSerializer::class) URI>,
    val alt: L10n<String>,
    val aspectRatio: Float? = null
) : Persistable<Image>() {
    object ProtoSerializer : Persistable.ProtoSerializer<Image, ProtoImage> {
        override fun fromProto(proto: ProtoImage, context: Context): Image {
            return Image(
                source = mapOf(Size.ORIGINAL to proto.source.parseUri()),
                alt = L10n.single(context, proto.alt),
                aspectRatio = proto.aspectRatio
            )
        }

        override fun toProto(persistable: Image, context: Context): ProtoImage =
            toProto(persistable, context, Size.ORIGINAL)

        fun toProto(persistable: Image, context: Context, size: Size = Size.ORIGINAL): ProtoImage =
            ProtoImage.newBuilder().build(persistable) {
                source = it.source.bestMatch(size).toString()
                alt = it.alt[context]
                it.aspectRatio?.let { a -> aspectRatio = a }
            }
    }

    @Serializable(with = Size.Serializer::class)
    enum class Size {
        ORIGINAL;

        object Serializer : EnumSerializer<Size>(Size::class, ORIGINAL)
    }
}

fun Image.toProto(context: Context): ProtoImage = Image.ProtoSerializer.toProto(this, context)


fun <T> Map<Image.Size, T>.bestMatch(size: Image.Size): T {
    require(isNotEmpty()) { "At least one size must be available" }

    return this[size]
        ?: this[Image.Size.ORIGINAL]
        ?: values.first()
}
