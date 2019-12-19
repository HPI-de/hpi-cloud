package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.RawJsonDocument
import com.couchbase.client.java.query.N1qlParams
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.Select.select
import com.couchbase.client.java.query.dsl.functions.MetaFunctions.meta
import com.couchbase.client.java.query.dsl.path.AsPath
import com.couchbase.client.java.query.dsl.path.LimitPath
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.grpc.throwException
import de.hpi.cloud.common.utils.thenTake
import io.grpc.Status
import rx.Observable


const val PAGINATION_TOKEN_SEPARATOR = ";"
const val PAGINATION_TOKEN_VARIANT_1 = "1" // "<startKey>;<startKeyDocId>"
const val PAGINATION_TOKEN_VARIANT_2 = "2" // "<offset>"

const val FIELD_META_ID = "id"

inline fun <reified E : Entity<E>> ViewQuery.paginate(
    bucket: Bucket,
    reqPageSize: Int,
    reqPageToken: String,
    defaultPageSize: Int = 20,
    maxPageSize: Int = 100
): Pair<Collection<Wrapper<E>>, String> {
    val pageSize = getPageSize(reqPageSize, defaultPageSize, maxPageSize)

    // Apply pagination to query
    if (reqPageToken.isNotBlank()) {
        if (!reqPageToken.startsWith(PAGINATION_TOKEN_VARIANT_1)) invalidPaginationToken()
        val (_, startKey, startKeyDocId) = reqPageToken.split(PAGINATION_TOKEN_SEPARATOR)
        startKey(startKey)
        startKeyDocId(startKeyDocId)
    }
    limit(pageSize + 1)

    // Execute query
    val items = bucket.query(this).allRows()

    val entities = items.take(pageSize)
        .map { it.document(RawJsonDocument::class.java).parseWrapper<E>() }
    val nextPageToken = (items.size == pageSize + 1)
        .thenTake { items.last() }
        ?.let { PAGINATION_TOKEN_VARIANT_1 + PAGINATION_TOKEN_SEPARATOR + it.key().toString() + PAGINATION_TOKEN_SEPARATOR + it.id() }
        ?: "" // Empty string makes building the protobuf easier
    return entities to nextPageToken
}

inline fun <reified E : Entity<E>> paginate(
    bucket: Bucket,
    queryBuilder: AsPath.() -> LimitPath,
    reqPageSize: Int,
    reqPageToken: String,
    defaultPageSize: Int = 20,
    maxPageSize: Int = 100
): Pair<Collection<Wrapper<E>>, String> {
    val pageSize = getPageSize(reqPageSize, defaultPageSize, maxPageSize)
    val off = (reqPageToken.isNotBlank()).thenTake {
        if (!reqPageToken.startsWith(PAGINATION_TOKEN_VARIANT_2)) invalidPaginationToken()
        reqPageToken.split(PAGINATION_TOKEN_SEPARATOR).getOrNull(1)?.toIntOrNull()
            ?: invalidPaginationToken()
    } ?: 0

    // Build query
    val query = select(meta(bucket.name())[FIELD_META_ID]).from(bucket.name()).queryBuilder()
        .run { limit(pageSize + 1) }
        .run { offset(off) }
        .let { N1qlQuery.simple(it, N1qlParams.build().adhoc(false)) }

    // Execute query
    val items = bucket.query(query).allRows()
    val ids = items.take(pageSize)
        .map { it.value().getString(FIELD_META_ID) }
    val entities = Observable.from(ids)
        .flatMap { bucket.async().get<E>(Id(it)) }
        .toList()
        .toBlocking()
        .single()
    val nextPageToken = (items.size == pageSize + 1)
        .thenTake { PAGINATION_TOKEN_VARIANT_2 + PAGINATION_TOKEN_SEPARATOR + (off + pageSize) }
        ?: "" // Empty string makes building the protobuf easier
    return entities to nextPageToken
}

fun getPageSize(
    reqPageSize: Int,
    defaultPageSize: Int = 20,
    maxPageSize: Int = 100
): Int {
    require(defaultPageSize > 0) { "defaultPageSize must be positive" }
    require(maxPageSize >= defaultPageSize) { "maxPageSize must be at least as large as defaultPageSize" }

    return when {
        reqPageSize < 0 -> Status.INVALID_ARGUMENT.throwException("page_size must be at least 1 if set, was: $reqPageSize")
        reqPageSize == 0 -> defaultPageSize
        reqPageSize > maxPageSize -> Status.INVALID_ARGUMENT.throwException("page_size must be at most $maxPageSize if set, was: $reqPageSize")
        else -> reqPageSize
    }
}

fun invalidPaginationToken(): Nothing {
    Status.INVALID_ARGUMENT.throwException("Invalid pagination token")
}
