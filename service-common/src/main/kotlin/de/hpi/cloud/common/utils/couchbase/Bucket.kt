package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.view.ViewQuery
import com.couchbase.client.java.view.ViewRow

fun Bucket.querySingle(query: ViewQuery): ViewRow? {
    return query(query.limit(1)).allRows().firstOrNull()
}

fun Bucket.get(design: String, view: String, key: String): ViewRow? {
    return querySingle(ViewQuery.from(design, view).key(key))
}
