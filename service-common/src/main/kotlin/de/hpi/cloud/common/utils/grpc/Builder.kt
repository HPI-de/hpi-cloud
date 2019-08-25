package de.hpi.cloud.common.utils.grpc

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.GeneratedMessageV3.Builder
import de.hpi.cloud.common.utils.couchbase.KEY_ID
import de.hpi.cloud.common.utils.couchbase.KEY_TYPE
import de.hpi.cloud.common.utils.couchbase.KEY_VALUE
import de.hpi.cloud.common.utils.couchbase.KEY_VERSION

fun buildJsonDocument(id: String, type: String, version: Int, value: Map<String, Any?>): JsonDocument {
    return JsonDocument.create(
        "$type:$id",
        JsonObject.from(mapOf(
            KEY_TYPE to type,
            KEY_VERSION to version,
            KEY_ID to id,
            KEY_VALUE to value
        ))
    )
}

fun <M : Any, B : Builder<B>> B.buildWith(json: JsonObject?, builder: B.(JsonObject) -> Unit): M? {
    val value = json?.getObject(KEY_VALUE) ?: return null
    builder(value)
    @Suppress("UNCHECKED_CAST")
    return build() as M
}
