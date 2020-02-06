package de.hpi.cloud.common.serializers.json

import de.hpi.cloud.common.utils.parseUri
import de.hpi.cloud.common.utils.parseUrl
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.net.URI
import java.net.URL

@Serializer(forClass = URI::class)
object UriSerializer : KSerializer<URI> {
    override val descriptor = StringDescriptor.withName("URI")

    override fun serialize(encoder: Encoder, obj: URI) = encoder.encodeString(obj.toString())
    override fun deserialize(decoder: Decoder): URI = decoder.decodeString().parseUri()
}

@Serializer(forClass = URL::class)
object UrlSerializer : KSerializer<URL> {
    override val descriptor = StringDescriptor.withName("URL")

    override fun serialize(encoder: Encoder, obj: URL) = encoder.encodeString(obj.toString())
    override fun deserialize(decoder: Decoder): URL = decoder.decodeString().parseUrl()
}
