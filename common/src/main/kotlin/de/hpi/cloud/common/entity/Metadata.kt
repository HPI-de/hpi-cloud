package de.hpi.cloud.common.entity

import de.hpi.cloud.common.serializers.UriSerializer
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.Serializable
import java.net.URI

@Serializable
data class Metadata(
    val sources: List<L10n<@Serializable(UriSerializer::class) URI>>,
    val permissions: Permissions,
    val events: List<Event>
) {
    inline fun <reified E : Event> eventsOfType(): List<E> = events.filterIsInstance<E>()
}
