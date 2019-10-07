package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.N1qlParams
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.Select.select
import com.couchbase.client.java.query.dsl.path.AsPath
import com.couchbase.client.java.query.dsl.path.LimitPath
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.thenTake
import io.grpc.Status


private const val PAGINATION_TOKEN_SEPARATOR = ";"
private const val PAGINATION_TOKEN_VARIANT_1 = "1" // "<startKey>;<startKeyDocId>"
private const val PAGINATION_TOKEN_VARIANT_2 = "2" // "<offset>"

fun <T : Any> ViewQuery.paginate(
    bucket: Bucket,
    reqPageSize: Int,
    reqPageToken: String,
    defaultPageSize: Int = 20,
    maxPageSize: Int = 100,
    mapper: ((JsonObject) -> T?)
): Pair<Collection<T>, String> {
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
    val items = execute(bucket).allRows()

    val objects = items.take(pageSize)
        .mapNotNull { mapper(it.document().content()) }
    val nextPageToken = (items.size == pageSize + 1)
        .thenTake { items.last() }
        ?.let { PAGINATION_TOKEN_VARIANT_1 + PAGINATION_TOKEN_SEPARATOR + it.key().toString() + PAGINATION_TOKEN_SEPARATOR + it.id() }
        ?: "" // Empty string makes building the protobuf easier
    return objects to nextPageToken
}

fun <T : Any> paginate(
    bucket: Bucket,
    queryBuilder: AsPath.() -> LimitPath,
    reqPageSize: Int,
    reqPageToken: String,
    defaultPageSize: Int = 20,
    maxPageSize: Int = 100,
    mapper: ((JsonObject) -> T?)
): Pair<Collection<T>, String> {
    val pageSize = getPageSize(reqPageSize, defaultPageSize, maxPageSize)
    val off = (reqPageToken.isNotBlank()).thenTake {
        if (!reqPageToken.startsWith(PAGINATION_TOKEN_VARIANT_2)) invalidPaginationToken()
        reqPageToken.split(PAGINATION_TOKEN_SEPARATOR).getOrNull(1)?.toIntOrNull()
            ?: invalidPaginationToken()
    } ?: 0

    // Build query
    val query = select("*").from(bucket.name()).queryBuilder()
        .run { limit(pageSize + 1) }
        .run { offset(off) }
        .let { N1qlQuery.simple(it, N1qlParams.build().adhoc(false)) }

    // Execute query
    val items = bucket.query(query).allRows()

    val objects = items.take(pageSize)
        .mapNotNull { mapper(it.value().getObject(bucket.name())) }
    val nextPageToken = (items.size == pageSize + 1)
        .thenTake { PAGINATION_TOKEN_VARIANT_2 + PAGINATION_TOKEN_SEPARATOR + (off + pageSize) }
        ?: "" // Empty string makes building the protobuf easier
    return objects to nextPageToken
}

private fun getPageSize(
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

private fun invalidPaginationToken(): Nothing {
    Status.INVALID_ARGUMENT.throwException("Invalid pagination token")
}
