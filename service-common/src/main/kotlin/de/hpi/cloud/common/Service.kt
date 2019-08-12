package de.hpi.cloud.common

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.CouchbaseCluster
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment
import io.grpc.Server
import io.grpc.ServerBuilder


class Service<S : io.grpc.BindableService>(
    couchbaseBucket: String,
    port: Int,
    createServiceImpl: (Bucket) -> S
) {
    companion object {
        const val COUCHBASE_CONNECT_TIMEOUT = 10000L
    }

    protected val server: Server
    protected val cluster: CouchbaseCluster
    protected val bucket: Bucket

    var isStopped = false
        private set

    init {
        println("Starting ${this::class.java.simpleName} on port $port")

        // Database
        cluster = CouchbaseCluster.create(
            DefaultCouchbaseEnvironment.Builder().connectTimeout(COUCHBASE_CONNECT_TIMEOUT).build()
        ).apply {
            authenticate("Test", "asdfgh")
        }
        bucket = cluster.openBucket(couchbaseBucket)

        // Server
        @Suppress("LeakingThis")
        server = ServerBuilder.forPort(port)
            .addService(createServiceImpl(bucket))
            .build()
        server.start()

        println("${this::class.java.simpleName} started")

        Runtime.getRuntime().addShutdownHook(object : Thread() {
            override fun run() {
                System.err.println("Stopping ${this::class.java.simpleName} due to runtime shutdown")
                this@Service.stop()
            }
        })
    }

    fun stop() {
        if (isStopped) throw IllegalStateException("${this::class.java.simpleName} is already stopped")
        isStopped = true

        println("Stopping ${this::class.java.simpleName}")

        // Server
        server.shutdown()

        // Database
        bucket.close()
        cluster.disconnect()

        println("${this::class.java.simpleName} stopped")
    }

    fun blockUntilShutdown() {
        server.awaitTermination()
    }
}
