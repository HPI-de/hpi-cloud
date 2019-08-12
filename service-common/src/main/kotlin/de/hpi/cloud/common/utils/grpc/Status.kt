package de.hpi.cloud.common.utils.grpc

import io.grpc.Status

fun Status.throwException(description: String? = null, cause: Throwable? = null): Nothing {
    throw let { if (description != null) withDescription(description) else this }
        .let { if (cause != null) withCause(cause) else this }
        .asRuntimeException()
}
