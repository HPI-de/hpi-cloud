package de.hpi.cloud.common.serializers.json

import de.hpi.cloud.common.utils.toIsoString
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.time.Instant

@Serializer(forClass = Instant::class)
object InstantSerializer : KSerializer<Instant> {
    override val descriptor = StringDescriptor.withName("Instant")

    override fun serialize(encoder: Encoder, obj: Instant) =
        encoder.encodeString(obj.toIsoString())

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(decoder.decodeString())
}
