package de.hpi.cloud.common.utils.grpc

import io.grpc.Status

fun checkArgRequired(arg: String?, argName: String) {
    if (arg.isNullOrBlank()) Status.INVALID_ARGUMENT.throwException("Argument $argName is required")
}

inline fun <reified M : com.google.protobuf.GeneratedMessageV3> notFound(id: String): Nothing {
    Status.NOT_FOUND.throwException("${M::class.java.simpleName} with ID $id not found")
}

fun Status.throwException(description: String? = null, cause: Throwable? = null): Nothing {
    throw run { if (description != null) withDescription(description) else this }
        .run { if (cause != null) withCause(cause) else this }
        .asRuntimeException()
}
