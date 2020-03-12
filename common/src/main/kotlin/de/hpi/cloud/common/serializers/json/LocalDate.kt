package de.hpi.cloud.common.serializers.json

import de.hpi.cloud.common.utils.toIsoString
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.time.LocalDate

@Serializer(forClass = LocalDate::class)
object LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor = StringDescriptor.withName("Instant")

    override fun serialize(encoder: Encoder, obj: LocalDate) =
        encoder.encodeString(obj.toIsoString())

    override fun deserialize(decoder: Decoder): LocalDate =
        LocalDate.parse(decoder.decodeString())
}
