package de.hpi.cloud.common.entity

import kotlinx.serialization.Serializable
import java.util.*

@Suppress("unused")
@Serializable
data class Id<E : Entity<E>>(val value: String) {
    companion object {
        fun <E : Entity<E>> random(): Id<E> = Id(UUID.randomUUID().toString())
    }
}
