package de.hpi.cloud.common.serializers

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import kotlin.reflect.KClass

abstract class EnumSerializer<E : Enum<E>>(
    private val kClass: KClass<E>,
    private val fallback: E
) : KSerializer<E> {
    override val descriptor: SerialDescriptor = StringDescriptor

    override fun serialize(encoder: Encoder, obj: E) = encoder.encodeString(obj.name.toLowerCase())

    override fun deserialize(decoder: Decoder): E {
        val value = decoder.decodeString()
        return kClass.enumMembers()
            .firstOrNull { it.name.equals(value, ignoreCase = true) }
            ?: fallback
    }
}
