package de.hpi.cloud.common.utils

fun <R> Boolean.then(block: () -> R): R? {
    return if (this) block() else null
}
fun <T, R> Boolean.then(obj: T, block: (T) -> R): R? {
    return if (this) block(obj) else null
}
