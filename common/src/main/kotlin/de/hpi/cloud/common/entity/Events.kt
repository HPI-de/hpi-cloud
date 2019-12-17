package de.hpi.cloud.common.entity

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Party
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.types.LocalDateTime
import kotlinx.serialization.Serializable
import java.net.URI
import java.time.LocalDateTime as RawLocalDateTime

@Serializable
sealed class Event(
    val author: Id<Party>,
    val timestamp: LocalDateTime = LocalDateTime.now()
)

class CreateEvent private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now()
) : Event(author, timestamp) {
    companion object {
        fun create(context: Context): CreateEvent =
            CreateEvent(
                author = context.author
            )
    }
}

class UpdateEvent<E : Entity<E>> private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now(),
    val oldValue: E
) : Event(author, timestamp) {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>): UpdateEvent<E> =
            UpdateEvent(
                author = context.author,
                oldValue = wrapper.value
            )
    }
}

class KeepAliveEvent private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now()
) : Event(author, timestamp) {
    companion object {
        fun create(context: Context): KeepAliveEvent =
            KeepAliveEvent(
                author = context.author
            )
    }
}

class SourcesChangeEvent private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now(),
    val oldSources: List<L10n<URI>>
) : Event(author, timestamp) {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>): SourcesChangeEvent =
            SourcesChangeEvent(
                author = context.author,
                oldSources = wrapper.metadata.sources
            )
    }
}

class PermissionsChangeEvent private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now(),
    val oldPermissions: Permissions
) : Event(author, timestamp) {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>): PermissionsChangeEvent =
            PermissionsChangeEvent(
                author = context.author,
                oldPermissions = wrapper.metadata.permissions
            )
    }
}

abstract class DelayedEvent(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now(),
    val effectiveFrom: LocalDateTime? = null
) : Event(author, timestamp) {
    val isEffectiveNow: Boolean
        get() = effectiveFrom == null || effectiveFrom.value < RawLocalDateTime.now()
}

class DeletedChangeEvent private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now(),
    val isDeleted: Boolean,
    effectiveFrom: LocalDateTime? = null
) : DelayedEvent(author, timestamp, effectiveFrom) {
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

class PublishedChangeEvent private constructor(
    author: Id<Party>,
    timestamp: LocalDateTime = LocalDateTime.now(),
    val isPublished: Boolean,
    effectiveFrom: LocalDateTime? = null
) : DelayedEvent(author, timestamp, effectiveFrom) {
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
