package de.hpi.cloud.common.utils.grpc

import io.grpc.Status

fun Status.throwException(description: String? = null, cause: Throwable? = null): Nothing {
    throw run { if (description != null) withDescription(description) else this }
        .run { if (cause != null) withCause(cause) else this }
        .asRuntimeException()
}
