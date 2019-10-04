package de.hpi.cloud.common.utils

fun <R> Boolean.thenTake(block: () -> R): R? {
    return if (this) block() else null
}
