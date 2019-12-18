package de.hpi.cloud.common.entity

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Party
import de.hpi.cloud.common.serializers.UriSerializer
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.types.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.net.URI
import java.time.LocalDateTime as RawLocalDateTime

@Serializable
sealed class Event {
    abstract val author: Id<Party>
    abstract val timestamp: LocalDateTime
}

@SerialName("create")
@Serializable
class CreateEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : Event() {
    companion object {
        fun create(context: Context): CreateEvent =
            CreateEvent(
                author = context.author
            )
    }
}

@SerialName("update")
@Serializable
class UpdateEvent<E : Entity<E>> private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val oldValue: E
) : Event() {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>): UpdateEvent<E> =
            UpdateEvent(
                author = context.author,
                oldValue = wrapper.value
            )
    }
}
@SerialName("keepAlive")
@Serializable
class KeepAliveEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now()
) : Event() {
    companion object {
        fun create(context: Context): KeepAliveEvent =
            KeepAliveEvent(
                author = context.author
            )
    }
}

@SerialName("sourceChanged")
@Serializable
class SourcesChangeEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val oldSources: List<L10n<@Serializable(UriSerializer::class) URI>>
) : Event() {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>): SourcesChangeEvent =
            SourcesChangeEvent(
                author = context.author,
                oldSources = wrapper.metadata.sources
            )
    }
}

@SerialName("permissionsChange")
@Serializable
class PermissionsChangeEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val oldPermissions: Permissions
) : Event() {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>): PermissionsChangeEvent =
            PermissionsChangeEvent(
                author = context.author,
                oldPermissions = wrapper.metadata.permissions
            )
    }
}

@SerialName("delayed")
@Serializable
sealed class DelayedEvent : Event() {
    abstract val effectiveFrom: LocalDateTime?

    val isEffectiveNow: Boolean
        get() = effectiveFrom?.let { it.value < RawLocalDateTime.now() } ?: true
}

@SerialName("deletedChange")
@Serializable
class DeletedChangeEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean,
    override val effectiveFrom: LocalDateTime? = null
) : DelayedEvent() {
    companion object {
        fun <E : Entity<E>> create(
            context: Context,
            isDeleted: Boolean,
            effectiveFrom: LocalDateTime? = null
        ): DeletedChangeEvent = DeletedChangeEvent(
            author = context.author,
            isDeleted = isDeleted,
            effectiveFrom = effectiveFrom
        )
    }
}

@SerialName("publishedChange")
@Serializable
class PublishedChangeEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: LocalDateTime = LocalDateTime.now(),
    val isPublished: Boolean,
    override val effectiveFrom: LocalDateTime? = null
) : DelayedEvent() {
    companion object {
        fun <E : Entity<E>> create(
            context: Context,
            isPublished: Boolean,
            effectiveFrom: LocalDateTime? = null
        ): PublishedChangeEvent = PublishedChangeEvent(
            author = context.author,
            isPublished = isPublished,
            effectiveFrom = effectiveFrom
        )
    }
}
