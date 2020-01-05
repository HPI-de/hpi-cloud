package de.hpi.cloud.common.entity

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.types.LocalDateTime
import kotlinx.serialization.*
import kotlinx.serialization.internal.SerialClassDescImpl
import java.net.URI

@Serializable(with = Wrapper.JsonSerializer::class)
data class Wrapper<E : Entity<E>>(
    val type: String,
    val version: Int = 1,
    val id: Id<E>,
    val metadata: Metadata,
    val value: E
) {
    companion object {
        fun <E : Entity<E>> create(
            context: Context,
            companion: Entity.Companion<E>,
            id: Id<E>,
            sources: List<L10n<URI>> = emptyList(),
            permissions: Permissions = emptyMap(),
            value: E,
            published: Boolean = true
        ): Wrapper<E> {
            return Wrapper(
                type = companion.type,
                version = companion.version,
                id = id,
                metadata = Metadata(
                    sources = sources,
                    permissions = permissions,
                    events = listOf(CreateEvent.create(context))
                ),
                value = value
            )
        }

        inline fun <reified E : Entity<E>> jsonSerializerFor(): JsonSerializer<E> =
            JsonSerializer(E::class.jsonSerializer())
    }

    @Serializer(forClass = Wrapper::class)
    class JsonSerializer<E : Entity<E>>(
        private val entitySerializer: KSerializer<E>
    ) : KSerializer<Wrapper<E>> {
        override val descriptor = object : SerialClassDescImpl("Wrapper") {
            init {
                addElement("type")
                addElement("version")
                addElement("id")
                addElement("metadata")
                addElement("value")
            }
        }

        override fun serialize(encoder: Encoder, obj: Wrapper<E>) {
            encoder.beginStructure(descriptor).let {
                it.encodeStringElement(descriptor, 0, obj.type)
                it.encodeIntElement(descriptor, 1, obj.version)
                it.encodeStringElement(descriptor, 2, obj.id.value)
                it.encodeSerializableElement(descriptor, 3, Metadata.serializer(), obj.metadata)
                it.encodeSerializableElement(descriptor, 4, entitySerializer, obj.value)
                it.endStructure(descriptor)
            }
        }

        override fun deserialize(decoder: Decoder): Wrapper<E> {
            val dec = decoder.beginStructure(descriptor)
            var type: String? = null
            var version: Int? = null
            var id: Id<E>? = null
            var metadata: Metadata? = null
            var value: E? = null

            loop@ while (true) {
                when (val i = dec.decodeElementIndex(descriptor)) {
                    CompositeDecoder.READ_DONE -> break@loop
                    0 -> type = dec.decodeStringElement(descriptor, i)
                    1 -> version = dec.decodeIntElement(descriptor, i)
                    2 -> id = Id(dec.decodeStringElement(descriptor, i))
                    3 -> metadata = dec.decodeSerializableElement(descriptor, i, Metadata.serializer())
                    4 -> value = dec.decodeSerializableElement(descriptor, i, entitySerializer)
                    else -> throw SerializationException("Unknown index $i")
                }
            }
            dec.endStructure(descriptor)
            return Wrapper(
                type ?: throw MissingFieldException("type"),
                version ?: throw MissingFieldException("version"),
                id ?: throw MissingFieldException("id"),
                metadata ?: throw MissingFieldException("metadata"),
                value ?: throw MissingFieldException("value")
            )
        }
    }

    val documentId: String
        get() = id.documentId(type)

    // region Mutation
    fun withValue(context: Context, newValue: E): Wrapper<E> {
        return if (value == newValue) withKeepAlive(context)
        else copy(
            metadata = metadata.copy(
                events = metadata.events + UpdateEvent.create(context, this)
            ),
            value = newValue
        )
    }

    fun withKeepAlive(context: Context): Wrapper<E> {
        return copy(
            metadata = metadata.copy(
                events = metadata.events + KeepAliveEvent.create(context)
            )
        )
    }

    fun withSources(context: Context, newSources: List<L10n<URI>>): Wrapper<E> {
        return if (metadata.sources == newSources) this
        else copy(
            metadata = metadata.copy(
                sources = newSources,
                events = metadata.events + SourcesChangeEvent.create(context, this)
            )
        )
    }

    fun withPermissions(context: Context, newPermissions: Permissions): Wrapper<E> {
        return if (metadata.permissions == newPermissions) this
        else copy(
            metadata = metadata.copy(
                permissions = newPermissions,
                events = metadata.events + PermissionsChangeEvent.create(context, this)
            )
        )
    }

    val isDeleted: Boolean
        get() = metadata.eventsOfType<DeletedChangeEvent>()
            .filter { it.isEffectiveNow }
            .sumBy { if (it.isDeleted) -1 else 1 } < 0

    fun withDeleted(
        context: Context,
        isDeleted: Boolean,
        effectiveFrom: LocalDateTime = LocalDateTime.now()
    ): Wrapper<E> {
        return copy(
            metadata = metadata.copy(
                events = metadata.events + DeletedChangeEvent.create<E>(context, isDeleted, effectiveFrom)
            )
        )
    }

    val isPublished: Boolean
        get() = metadata.eventsOfType<PublishedChangeEvent>()
            .filter { it.isEffectiveNow }
            .sumBy { if (it.isPublished) 1 else -1 } > 0

    fun withPublished(
        context: Context,
        isPublished: Boolean,
        effectiveFrom: LocalDateTime = LocalDateTime.now()
    ): Wrapper<E> {
        return copy(
            metadata = metadata.copy(
                events = metadata.events + PublishedChangeEvent.create<E>(context, isPublished, effectiveFrom)
            )
        )
    }
    // endregion
}
