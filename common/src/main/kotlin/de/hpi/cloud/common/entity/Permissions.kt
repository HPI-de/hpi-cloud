package de.hpi.cloud.common.entity

import de.hpi.cloud.common.Party
import kotlinx.serialization.Serializable

@Serializable
data class Permission(
    val read: Boolean? = null,
    val write: Boolean? = null,
    val create: Boolean? = null
)
typealias Permissions = Map<Id<Party>, Permission>
