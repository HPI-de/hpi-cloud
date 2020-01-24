package de.hpi.cloud.common.utils

import de.hpi.cloud.common.types.Image

fun <T> Map<Image.Size, T>.bestMatch(size: Image.Size): T {
    require(isNotEmpty()) { "At least one size must be available" }

    return this[size]
        ?: this[Image.Size.ORIGINAL]
        ?: values.first()
}
