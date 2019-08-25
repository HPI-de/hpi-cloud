package de.hpi.cloud.common

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.protobuf.timestampNow

abstract class Entity(val type: String, val version: Int) {
    val documentId
        get() = "$type:$id"
    abstract val id: String
    open val fetchedAt = timestampNow()

    // region JSON
    abstract fun valueToMap(): Map<String, Any?>

    open fun metaToMap(): Map<String, Any?> = mapOf("fetchedAt" to fetchedAt)
    fun toJsonObject(): JsonObject =
        JsonObject.from(
            mapOf(
                KEY_TYPE to type,
                KEY_VERSION to version,
                KEY_ID to id,
                KEY_METADATA to metaToMap(),
                KEY_VALUE to valueToMap()
            )
        )

    fun toJsonDocument(): JsonDocument = JsonDocument.create(documentId, toJsonObject())
    // endregion
}
