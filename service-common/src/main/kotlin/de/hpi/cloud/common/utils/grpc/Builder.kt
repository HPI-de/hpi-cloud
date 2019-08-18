package de.hpi.cloud.common.utils.grpc

import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.GeneratedMessageV3.Builder

fun <M, B : Builder<B>> B.build(json: JsonObject, builder: B.(JsonObject) -> Unit): M {
    builder(json)
    @Suppress("UNCHECKED_CAST")
    return build() as M
}
