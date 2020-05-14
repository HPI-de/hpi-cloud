package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.error.DocumentAlreadyExistsException
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.grpc.throwAlreadyExists

inline fun <reified E : Entity<E>, reified Proto : GeneratedMessageV3> Bucket.tryInsert(wrapper: Wrapper<E>) {
    try {
        insert(wrapper)
    } catch (e: DocumentAlreadyExistsException) {
        throwAlreadyExists<Proto>(wrapper.id)
    }
}
