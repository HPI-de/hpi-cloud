package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.AsyncBucket
import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.RawJsonDocument
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.entity.entityCompanion
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import rx.Observable

val json = Json(JsonConfiguration.Stable)

inline fun <reified E : Entity<E>> RawJsonDocument.parseWrapper(): Wrapper<E> {
    return json.parse(Wrapper.jsonSerializerFor<E>(), content())
}

inline fun <reified E : Entity<E>> Bucket.get(id: Id<E>): Wrapper<E>? {
    val docId = Wrapper.createDocumentId(E::class.entityCompanion().type, id)
    return get(docId, RawJsonDocument::class.java)?.parseWrapper() ?: return null
}

inline fun <reified E : Entity<E>> AsyncBucket.get(id: Id<E>): Observable<Wrapper<E>> {
    val docId = Wrapper.createDocumentId(E::class.entityCompanion().type, id)
    return get(docId, RawJsonDocument::class.java)
        .map { it.parseWrapper<E>() }
}
