package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.RawJsonDocument
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import rx.Observable

val jsonConfiguration = JsonConfiguration.Stable.copy(
    strictMode = false,
    allowStructuredMapKeys = false
)
val json = Json(jsonConfiguration)

inline fun <reified E : Entity<E>> RawJsonDocument.parseWrapper(): Wrapper<E> {
    return json.parse(Wrapper.jsonSerializerFor<E>(), content())
}

inline fun <reified E : Entity<E>> Bucket.get(id: Id<E>): Wrapper<E>? {
    return get(id.documentId<E>(), RawJsonDocument::class.java)?.parseWrapper()
}

inline fun <reified E : Entity<E>> AsyncBucket.get(id: Id<E>): Observable<Wrapper<E>> {
    return get(id.documentId<E>(), RawJsonDocument::class.java)
        .map { it.parseWrapper<E>() }
}

inline fun <reified E : Entity<E>> Wrapper<E>.toJsonDocument() : RawJsonDocument {
    return RawJsonDocument.create(
        documentId,
        json.stringify(Wrapper.jsonSerializerFor(), this)
    )
}

inline fun <reified E : Entity<E>> Bucket.upsert(entityWrapper: Wrapper<E>) {
    upsert(entityWrapper.toJsonDocument())
}
