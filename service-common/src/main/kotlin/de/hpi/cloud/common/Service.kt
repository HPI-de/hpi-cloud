package de.hpi.cloud.common

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment
import io.grpc.Server
import io.grpc.ServerBuilder


class Service<S : io.grpc.BindableService>(
    val name: String,
    port: Int,
    createServiceImpl: (Bucket) -> S
) {
    companion object {
        const val COUCHBASE_CONNECT_TIMEOUT = 10000L
        const val COUCHBASE_USERNAME_VARIABLE = "HPI_CLOUD_COUCHBASE_USERNAME"
        const val COUCHBASE_PASSWORD_VARIABLE = "HPI_CLOUD_COUCHBASE_PASSWORD"
    }

    protected val server: Server
    protected val cluster: CouchbaseCluster
    protected val bucket: Bucket

    var isStopped = false
        private set

    init {
        println("Starting $name on port $port")

        // Database
        cluster = CouchbaseCluster.create(
            DefaultCouchbaseEnvironment.Builder().connectTimeout(COUCHBASE_CONNECT_TIMEOUT).build()
        ).apply {
            val username = System.getenv(COUCHBASE_USERNAME_VARIABLE)
                ?: throw IllegalStateException("Couchbase username must be provided via the environment variable $COUCHBASE_USERNAME_VARIABLE")
            val password = System.getenv(COUCHBASE_PASSWORD_VARIABLE)
                ?: throw IllegalStateException("Couchbase password must be provided via the environment variable $COUCHBASE_PASSWORD_VARIABLE")
            authenticate(username, password)
        }
        bucket = cluster.openBucket(name)

        // Server
        @Suppress("LeakingThis")
        server = ServerBuilder.forPort(port)
            .addService(createServiceImpl(bucket))
            .build()
        server.start()

        println("$name started")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("Stopping ${this@Service.name} due to runtime shutdown")
                this@Service.stop()
            }
        })
    }

    fun stop() {
        if (isStopped) throw IllegalStateException("$name is already stopped")
        isStopped = true

        println("Stopping $name")

        // Server
        server.shutdown()

        // Database
        bucket.close()
        cluster.disconnect()

        println("$name stopped")
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
