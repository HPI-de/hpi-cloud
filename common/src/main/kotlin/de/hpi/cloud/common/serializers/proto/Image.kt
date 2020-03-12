package de.hpi.cloud.common.serializers.proto

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.types.Image
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.utils.parseUrl
import de.hpi.cloud.common.v1test.Image as ProtoImage

object ImageSerializer : ProtoSerializer<Image, ProtoImage> {
    override fun fromProto(proto: ProtoImage, context: Context): Image {
        return Image(
            source = mapOf(Image.Size.ORIGINAL to proto.source.parseUrl()),
            alt = L10n.single(context, proto.alt),
            aspectRatio = proto.aspectRatio
        )
    }

    override fun toProto(persistable: Image, context: Context): ProtoImage =
        toProto(persistable, context, Image.Size.ORIGINAL)

    fun toProto(persistable: Image, context: Context, size: Image.Size = Image.Size.ORIGINAL): ProtoImage =
        ProtoImage.newBuilder().build(persistable) {
            source = it.source.bestMatch(size).toString()
            alt = it.alt[context]
            it.aspectRatio?.let { a -> aspectRatio = a }
        }

    private fun <T> Map<Image.Size, T>.bestMatch(size: Image.Size): T {
        require(isNotEmpty()) { "At least one size must be available" }

        return this[size]
            ?: this[Image.Size.ORIGINAL]
            ?: values.first()
    }
}

fun ProtoImage.parse(context: Context): Image =
    ImageSerializer.fromProto(this, context)

fun Image.toProto(context: Context): ProtoImage =
    ImageSerializer.toProto(this, context)
