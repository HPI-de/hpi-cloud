package de.hpi.cloud.common.serializers

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.util.*

@Serializer(forClass = Locale::class)
object LocaleSerializer : KSerializer<Locale> {
    override val descriptor = StringDescriptor.withName("AsString")

    override fun serialize(encoder: Encoder, obj: Locale) = encoder.encodeString(obj.toLanguageTag())
    override fun deserialize(decoder: Decoder): Locale = Locale.forLanguageTag(decoder.decodeString())
}
