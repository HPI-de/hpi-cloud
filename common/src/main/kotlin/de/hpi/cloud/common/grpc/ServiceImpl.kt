package de.hpi.cloud.common.grpc

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Service
import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver

@Suppress("unused") // Receiver is used for scoping
fun <Req : Any, Res : Any> BindableService.catchErrors(
    request: Req?,
    responseObserver: StreamObserver<Res>?,
    lambda: (Req, StreamObserver<Res>) -> Unit
) {
    try {
        request ?: Status.INVALID_ARGUMENT.throwException("request is null")
        responseObserver ?: Status.INVALID_ARGUMENT.throwException("responseObserver is null")

        lambda(request, responseObserver)
    } catch (e: Throwable) {
        val status =
            if (e is StatusRuntimeException) e.status
            else when (e) {
                is NotImplementedError -> Status.UNIMPLEMENTED
                else -> Status.UNKNOWN
            }.withCause(e)
        val exception = status.asRuntimeException()
        exception.printStackTrace()
        responseObserver?.onError(exception)
    }
}

fun <Req : Any, Res : Any> BindableService.unary(
    request: Req?,
    responseObserver: StreamObserver<Res>?,
    methodName: String,
    lambda: (Context, Req) -> Res
) = catchErrors(request, responseObserver) { req, res ->
    println("${this::class.java.simpleName}.$methodName called")
    val context = Service.contextForRequest(req) ?: throw IllegalStateException("Metadata not found")
    res.onNext(lambda(context, req))
    res.onCompleted()
}
