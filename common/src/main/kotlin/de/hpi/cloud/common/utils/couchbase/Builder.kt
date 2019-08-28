package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject

fun documentId(id: String, type: String) = "$type:$id"

fun buildJsonDocument(
    id: String,
    type: String,
    version: Int,
    value: Map<String, Any?>,
    meta: Map<String, Any?>? = null
): JsonDocument {
    return JsonDocument.create(
        documentId(id, type),
        JsonObject.from(
            mapOf(
                KEY_TYPE to type,
                KEY_VERSION to version,
                KEY_ID to id,
                KEY_METADATA to meta,
                KEY_VALUE to value
            )
        )
    )
}
