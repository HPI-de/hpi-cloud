package de.hpi.cloud.common.utils.grpc

import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.GeneratedMessageV3.Builder
import de.hpi.cloud.common.utils.couchbase.KEY_VALUE

fun <M : Any, B : Builder<B>> B.buildWith(json: JsonObject?, builder: B.(JsonObject) -> Unit): M? {
    val value = json?.getObject(KEY_VALUE) ?: return null
    builder(value)
    @Suppress("UNCHECKED_CAST")
    return build() as M
}
