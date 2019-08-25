package de.hpi.cloud.common

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.CouchbaseCluster
import de.hpi.cloud.common.utils.couchbase.openCouchbase
import io.grpc.Server
import io.grpc.ServerBuilder


class Service<S : io.grpc.BindableService>(
    val name: String,
    portOverride: Int?,
    createServiceImpl: (Bucket) -> S
) {
    companion object {
        const val PORT_DEFAULT = 50051
        const val PORT_VARIABLE = "HPI_CLOUD_PORT"
    }

    private val server: Server
    private val cluster: CouchbaseCluster
    private val bucket: Bucket

    var isStopped = false
        private set

    init {
        val port = portOverride
            ?: System.getenv(PORT_VARIABLE)?.toInt()
            ?: PORT_DEFAULT
        println("Starting $name on portOverride $port")

        // Database
        cluster = openCouchbase()
        bucket = cluster.openBucket(name)

        // Server
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
