package de.hpi.cloud.common.utils.grpc

import io.grpc.BindableService
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.stub.StreamObserver

fun <Req : Any, Res : Any> BindableService.catchErrors(
    request: Req?,
    responseObserver: StreamObserver<Res>?,
    lambda: (Req, StreamObserver<Res>) -> Unit
) {
    try {
        if (request == null) throw IllegalArgumentException("request is null")
        if (responseObserver == null) throw IllegalArgumentException("responseObserver is null")

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
    lambda: (Req) -> Res
) = catchErrors(request, responseObserver) { req, res ->
    println("${this::class.java.simpleName}.$methodName called")
    res.onNext(lambda(req))
    res.onCompleted()
}
