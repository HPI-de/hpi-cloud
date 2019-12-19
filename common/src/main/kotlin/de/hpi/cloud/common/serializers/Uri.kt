package de.hpi.cloud.common.serializers

import de.hpi.cloud.common.utils.parseUri
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.net.URI

@Serializer(forClass = URI::class)
object UriSerializer : KSerializer<URI> {
    override val descriptor = StringDescriptor.withName("URI")

    override fun serialize(encoder: Encoder, obj: URI) = encoder.encodeString(obj.toString())
    override fun deserialize(decoder: Decoder): URI = decoder.decodeString().parseUri()
}
