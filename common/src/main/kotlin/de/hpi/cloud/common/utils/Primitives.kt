package de.hpi.cloud.common.utils

fun <R> Boolean.thenTake(block: () -> R): R? {
    return if (this) block() else null
}

fun <T, R> Boolean.thenWith(obj: T, block: (T) -> R): R? {
    return if (this) block(obj) else null
}
