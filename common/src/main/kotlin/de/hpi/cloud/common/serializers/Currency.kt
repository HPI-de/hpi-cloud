package de.hpi.cloud.common.serializers

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.util.*

@Serializer(forClass = Currency::class)
object CurrencySerializer : KSerializer<Currency> {
    override val descriptor = StringDescriptor.withName("Currency")

    override fun serialize(encoder: Encoder, obj: Currency) = encoder.encodeString(obj.currencyCode)
    override fun deserialize(decoder: Decoder): Currency = Currency.getInstance(decoder.decodeString())
}
