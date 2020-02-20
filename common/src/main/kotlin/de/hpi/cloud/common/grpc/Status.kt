package de.hpi.cloud.common.grpc

import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.entity.Id
import io.grpc.Status

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

fun throwInvalidArgument(message: String, value: String): Nothing =
    Status.INVALID_ARGUMENT.throwException("$message, was: \"$value\"")


inline fun <reified Proto : GeneratedMessageV3> throwAlreadyExists(id: Id<*>): Nothing =
    Status.ALREADY_EXISTS.throwException("${Proto::class.java.simpleName} with ID $id already exists")


inline fun <reified Proto : GeneratedMessageV3> throwNotFound(id: String): Nothing {
    Status.NOT_FOUND.throwException("${Proto::class.java.simpleName} with ID $id not found")
}

fun Status.throwException(description: String? = null, cause: Throwable? = null): Nothing {
    throw run { if (description != null) withDescription(description) else this }
        .run { if (cause != null) withCause(cause) else this }
        .asRuntimeException()
}
