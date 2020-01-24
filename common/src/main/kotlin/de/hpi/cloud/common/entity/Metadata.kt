package de.hpi.cloud.common.entity

import kotlinx.serialization.Serializable

@Serializable
data class Metadata(
    val permissions: Permissions,
    val events: List<Event>
) {
    inline fun <reified E : Event> eventsOfType(): List<E> = events.filterIsInstance<E>()

    val isDeleted: Boolean
        get() = isDelayedEventEffective<DeletedChangeEvent>(false) { it.isDeleted }
    val isPublished: Boolean
        get() = isDelayedEventEffective<PublishedChangeEvent>(true) { it.isPublished }
}
