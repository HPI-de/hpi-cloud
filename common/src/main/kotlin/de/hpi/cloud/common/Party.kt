package de.hpi.cloud.common

import de.hpi.cloud.common.entity.Entity
import kotlinx.serialization.Serializable

@Serializable
data class Party(
    val name: String
) : Entity<Party>() {
    companion object : Entity.Companion<Party>("party")
}
