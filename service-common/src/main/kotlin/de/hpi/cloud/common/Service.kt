package de.hpi.cloud.common

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.CouchbaseCluster
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.utils.couchbase.openCouchbase
import de.hpi.cloud.common.utils.removeFirst
import io.grpc.*


class Service<S : BindableService>(
    val name: String,
    portOverride: Int?,
    createServiceImpl: (Bucket) -> S
) {
    companion object {
        const val PORT_DEFAULT = 50051
        const val PORT_VARIABLE = "HPI_CLOUD_PORT"

        private val requestMetadata = mutableListOf<RequestWithMetadata<GeneratedMessageV3>>()
        fun metadataForRequest(request: GeneratedMessageV3): Metadata? =
            requestMetadata.firstOrNull { it.request === request }?.metadata
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
        println("Starting $name on port $port")

        // Database
        cluster = openCouchbase()
        bucket = cluster.openBucket(name)

        // Server
        server = ServerBuilder.forPort(port)
            .intercept(object : ServerInterceptor {
                override fun <ReqT : Any?, RespT : Any?> interceptCall(
                    call: ServerCall<ReqT, RespT>?,
                    headers: Metadata?,
                    next: ServerCallHandler<ReqT, RespT>?
                ): ServerCall.Listener<ReqT> {
                    val listener = next!!.startCall(call, headers)
                    return object : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(listener) {
                        lateinit var request: GeneratedMessageV3

                        override fun onMessage(message: ReqT) {
                            require(message is GeneratedMessageV3)

                            request = message
                            requestMetadata.add(RequestWithMetadata(message, headers))
                            super.onMessage(message)
                        }

                        override fun onComplete() {
                            super.onComplete()
                            requestMetadata.removeFirst { it.request === request }
                        }

                        override fun onCancel() {
                            super.onCancel()
                            requestMetadata.removeFirst { it.request === request }
                        }
                    }
                }
            })
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

    data class RequestWithMetadata<ReqT : GeneratedMessageV3>(
        val request: ReqT,
        val metadata: Metadata?
    )
}
