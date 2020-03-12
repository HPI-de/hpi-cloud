package de.hpi.cloud.common.types

import de.hpi.cloud.common.serializers.json.EnumSerializer
import de.hpi.cloud.common.serializers.json.UrlSerializer
import kotlinx.serialization.Serializable
import java.net.URL

@Serializable
data class Image(
    val source: Map<Size, @Serializable(with = UrlSerializer::class) URL>,
    val alt: L10n<String>,
    val aspectRatio: Float? = null
) {
    @Serializable(with = Size.Serializer::class)
    enum class Size {
        ORIGINAL;

        object Serializer : EnumSerializer<Size>(Size::class, ORIGINAL)
    }
}
