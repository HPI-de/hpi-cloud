package de.hpi.cloud.common

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject

abstract class Entity(val type: String, val version: Int) {
    val documentId
        get() = "${type}_$id"
    abstract val id: String

    abstract fun toJsonObject(): JsonObject

    fun toJsonDocument(): JsonDocument = JsonDocument.create(documentId, toJsonObject())
}
