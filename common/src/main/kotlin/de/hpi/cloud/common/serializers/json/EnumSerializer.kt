package de.hpi.cloud.common.serializers.json

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlin.reflect.KClass

abstract class EnumSerializer<E : Enum<E>>(
    private val kClass: KClass<E>,
    private val fallback: E,
    private val valueToString: (E) -> String = { it.name.toLowerCase() }
) : KSerializer<E> {
    override val descriptor: SerialDescriptor = StringDescriptor

    override fun serialize(encoder: Encoder, obj: E) = encoder.encodeString(valueToString(obj))

    override fun deserialize(decoder: Decoder): E {
        val value = decoder.decodeString()
        return kClass.enumMembers()
            .firstOrNull { valueToString(it).equals(value, ignoreCase = true) }
            ?: fallback
    }
}
