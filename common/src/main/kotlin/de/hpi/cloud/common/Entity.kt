package de.hpi.cloud.common

import de.hpi.cloud.common.utils.couchbase.buildJsonDocument
import de.hpi.cloud.common.utils.protobuf.timestampNow
import de.hpi.cloud.common.utils.protobuf.toDbMap

abstract class Entity(val type: String, val version: Int) {
    abstract val id: String
    open val fetchedAt = timestampNow()

    // region JSON
    abstract fun valueToMap(): Map<String, Any?>

    open fun metaToMap(): Map<String, Any?> = mapOf("fetchedAt" to fetchedAt.toDbMap())
    fun toJsonDocument() = buildJsonDocument(id, type, version, valueToMap(), metaToMap())
    // endregion
}
