package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.then
import io.grpc.Status


private const val PAGINATION_TOKEN_SEPARATOR = ";"
fun <T : Any> ViewQuery.paginate(
    bucket: Bucket,
    reqPageSize: Int,
    reqPageToken: String,
    defaultPageSize: Int = 20,
    maxPageSize: Int = 100,
    mapper: ((JsonObject) -> T?)
): Pair<Collection<T>, String> {
    require(defaultPageSize > 0) { "defaultPageSize must be positive" }
    require(maxPageSize >= defaultPageSize) { "maxPageSize must be at least as large as defaultPageSize" }

    // Determine actual page size
    val pageSize = when {
        reqPageSize < 0 -> Status.INVALID_ARGUMENT.throwException("page_size must be at least 1 if set, was: $reqPageSize")
        reqPageSize == 0 -> defaultPageSize
        reqPageSize > maxPageSize -> Status.INVALID_ARGUMENT.throwException("page_size must be at most $maxPageSize if set, was: $reqPageSize")
        else -> reqPageSize
    }

    // Apply pagination to query
    if (reqPageToken.isNotBlank()) {
        val (startKey, startKeyDocId) = reqPageToken.split(PAGINATION_TOKEN_SEPARATOR)
        startKey(startKey)
        startKeyDocId(startKeyDocId)
    }
    limit(pageSize + 1)

    // Execute query
    val items = execute(bucket)
        .allRows()

    val objects = items.take(pageSize)
        .map { it.document().content() }
    val nextPageToken = (items.size == pageSize + 1)
        .then { items.last() }
        ?.let { it.key().toString() + PAGINATION_TOKEN_SEPARATOR + it.id() }
        ?: "" // Empty string makes building the protobuf easier
    return objects.mapNotNull { mapper(it) } to nextPageToken
}
