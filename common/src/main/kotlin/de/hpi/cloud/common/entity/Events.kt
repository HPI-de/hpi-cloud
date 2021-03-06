package de.hpi.cloud.common.entity

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Party
import de.hpi.cloud.common.serializers.json.InstantSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
sealed class Event {
    abstract val author: Id<Party>
    abstract val timestamp: Instant
}

@SerialName("create")
@Serializable
class CreateEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: @Serializable(InstantSerializer::class) Instant = Instant.now()
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
    override val timestamp: @Serializable(InstantSerializer::class) Instant = Instant.now(),
    val oldValue: E?
) : Event() {
    companion object {
        fun <E : Entity<E>> create(context: Context, wrapper: Wrapper<E>, newValue: E): UpdateEvent<E> =
            UpdateEvent(
                author = context.author,
                oldValue = wrapper.value.takeUnless { it == newValue }
            )
    }
}

@SerialName("permissionsChange")
@Serializable
class PermissionsChangeEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: @Serializable(InstantSerializer::class) Instant = Instant.now(),
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
    abstract val effectiveFrom: @Serializable(InstantSerializer::class) Instant?

    val isEffectiveNow: Boolean
        get() {
            val value = effectiveFrom ?: return true
            return value < Instant.now()
        }
}

/**
 * Calculates whether the delayed event is effective now.
 *
 * An event is considered effective when of all currently effective events of that type the active ones outweigh the
 * inactive ones.
 *
 * ## Example:
 * An entity has four events of type [DeletedChangeEvent]:
 * - `effectiveFrom = <now - 1 h>, isDeleted = true`
 * - `effectiveFrom = <now - 1 h>, isDeleted = true`
 * - `effectiveFrom = <now - 10 min>, isDeleted = false`
 * - `effectiveFrom = <now + 1 min>, isDeleted = false`
 *
 * We`re only considering currently effective events, so the last event is ignored for now. By default, entities are not
 * deleted — hence [activeByDefault] is false.
 *
 * We now have two deletions vs one restore. `2 + (-1) = 1 >= 1`, hence the entity is deleted.
 *
 * When the fourth event becomes effective, our sum becomes `0` and the entity will be restored.
 *
 * > **Note:** We use this behaviour to stay predictable on multi-user environments.
 */
internal inline fun <reified E : DelayedEvent> Metadata.isDelayedEventEffective(
    activeByDefault: Boolean,
    isActive: (E) -> Boolean
): Boolean {
    // If it defaults to active, no or balanced events means active. Otherwise, we need at least one more active event.
    val threshold = if (activeByDefault) 0 else 1
    return eventsOfType<E>()
        .filter { it.isEffectiveNow }
        .sumBy { if (isActive(it)) 1 else -1 } >= threshold
}

@SerialName("deletedChange")
@Serializable
class DeletedChangeEvent private constructor(
    override val author: Id<Party>,
    override val timestamp: @Serializable(InstantSerializer::class) Instant = Instant.now(),
    val isDeleted: Boolean,
    override val effectiveFrom: @Serializable(InstantSerializer::class) Instant? = null
) : DelayedEvent() {
    companion object {
        fun create(
            context: Context,
            isDeleted: Boolean,
            effectiveFrom: Instant? = null
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
    override val timestamp: @Serializable(InstantSerializer::class) Instant = Instant.now(),
    val isPublished: Boolean,
    override val effectiveFrom: @Serializable(InstantSerializer::class) Instant? = null
) : DelayedEvent() {
    companion object {
        fun create(
            context: Context,
            isPublished: Boolean,
            effectiveFrom: Instant? = null
        ): PublishedChangeEvent = PublishedChangeEvent(
            author = context.author,
            isPublished = isPublished,
            effectiveFrom = effectiveFrom
        )
    }
}
