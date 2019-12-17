package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.CouchbaseAsyncCluster
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment
import com.couchbase.client.java.view.ViewQuery
import com.couchbase.client.java.view.ViewResult

const val COUCHBASE_CONNECT_TIMEOUT = 15000L
const val COUCHBASE_NODES_VARIABLE = "HPI_CLOUD_COUCHBASE_NODES"
const val COUCHBASE_USERNAME_VARIABLE = "HPI_CLOUD_COUCHBASE_USERNAME"
const val COUCHBASE_PASSWORD_VARIABLE = "HPI_CLOUD_COUCHBASE_PASSWORD"

fun openCouchbase(nodesOverride: List<String> = emptyList()): CouchbaseCluster {
    val nodes = nodesOverride.takeIf { it.isNotEmpty() }
        ?: System.getenv(COUCHBASE_NODES_VARIABLE)?.split(',')
        ?: listOf(CouchbaseAsyncCluster.DEFAULT_HOST)

    return CouchbaseCluster.create(
        DefaultCouchbaseEnvironment.Builder().connectTimeout(COUCHBASE_CONNECT_TIMEOUT).build(),
        nodes
    ).apply {
        val username = System.getenv(COUCHBASE_USERNAME_VARIABLE)
            ?: throw IllegalStateException("Couchbase username must be provided via the environment variable $COUCHBASE_USERNAME_VARIABLE")
        val password = System.getenv(COUCHBASE_PASSWORD_VARIABLE)
            ?: throw IllegalStateException("Couchbase password must be provided via the environment variable $COUCHBASE_PASSWORD_VARIABLE")
        authenticate(username, password)
    }
}

fun withBucket(bucket: String, nodesOverride: List<String> = emptyList(), runnable: (Bucket) -> Unit) {
    openCouchbase(nodesOverride).also { cluster ->
        cluster.openBucket(bucket)
            .also(runnable)
            .close()
    }.disconnect()
}

fun ViewQuery.execute(bucket: Bucket): ViewResult = bucket.query(this)
