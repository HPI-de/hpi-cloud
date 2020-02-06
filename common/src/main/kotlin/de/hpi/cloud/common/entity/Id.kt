package de.hpi.cloud.common.entity

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.util.*

@Suppress("unused")
@Serializable(with = Id.JsonSerializer::class)
data class Id<E : Entity<E>>(val value: String) {
    companion object {
        private const val ID_MAX_LENGTH = 64

        fun <E : Entity<E>> random(): Id<E> =
            Id(UUID.randomUUID().toString())

        fun <E : Entity<E>> fromParts(vararg parts: String): Id<E> =
            Id(parts.joinToString(separator = "_"))
    }

    fun truncated(maxLength: Int = ID_MAX_LENGTH): Id<E> =
        copy(value = value.take(maxLength))

    @Serializer(forClass = Id::class)
    class JsonSerializer<E : Entity<E>>(
        private val entitySerializer: KSerializer<E>
    ) : KSerializer<Id<E>> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName("Id")

        override fun serialize(encoder: Encoder, obj: Id<E>) = encoder.encodeString(obj.value)
        override fun deserialize(decoder: Decoder): Id<E> = Id(decoder.decodeString())
    }

    inline fun <reified E : Entity<E>> documentId(): String = "${E::class.entityCompanion().type}:$value"
    internal fun documentId(type: String): String = "$type:$value"
}
