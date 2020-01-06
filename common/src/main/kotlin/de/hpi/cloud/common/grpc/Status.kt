package de.hpi.cloud.common.grpc

import com.google.protobuf.GeneratedMessageV3
import io.grpc.Status

fun checkArgNotSet(arg: String?, argName: String) {
    if (!arg.isNullOrBlank()) Status.INVALID_ARGUMENT.throwException("Argument $argName must not be set")
}

fun checkArgRequired(hasArg: Boolean, argName: String, ifArgSet: String? = null) {
    if (!hasArg) argRequired(argName, ifArgSet)
}

fun checkArgRequired(arg: String?, argName: String, ifArgSet: String? = null) {
    if (arg.isNullOrBlank()) argRequired(argName, ifArgSet)
}

fun argRequired(argName: String, ifArgSet: String? = null) {
    Status.INVALID_ARGUMENT.throwException("Argument $argName is required"
            + (ifArgSet?.let { " if argument $it is set" } ?: ""))
}

inline fun <reified M : GeneratedMessageV3> notFound(id: String): Nothing {
    Status.NOT_FOUND.throwException("${M::class.java.simpleName} with ID $id not found")
}

fun Status.throwException(description: String? = null, cause: Throwable? = null): Nothing {
    throw run { if (description != null) withDescription(description) else this }
        .run { if (cause != null) withCause(cause) else this }
        .asRuntimeException()
}
