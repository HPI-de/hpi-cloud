package de.hpi.cloud.common.utils

fun <R> Boolean.then(block: () -> R): R? {
    return if (this) block() else null
}
