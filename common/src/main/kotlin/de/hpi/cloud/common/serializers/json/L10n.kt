package de.hpi.cloud.common.serializers.json

import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.*
import kotlinx.serialization.internal.LinkedHashMapSerializer
import kotlinx.serialization.internal.NamedMapClassDescriptor
import kotlinx.serialization.json.JsonElementSerializer

@Serializer(forClass = L10n::class)
class L10nSerializer<T : Any>(
    private val dataSerializer: KSerializer<T>
) : KSerializer<L10n<T>> {
    override val descriptor: SerialDescriptor =
        NamedMapClassDescriptor(
            "L10n",
            LocaleSerializer.descriptor, JsonElementSerializer.descriptor
        )

    override fun serialize(encoder: Encoder, obj: L10n<T>) {
        LinkedHashMapSerializer(LocaleSerializer, dataSerializer).serialize(encoder, obj.values)
    }

    override fun deserialize(decoder: Decoder): L10n<T> {
        return L10n(LinkedHashMapSerializer(LocaleSerializer, dataSerializer).deserialize(decoder))
    }
}
