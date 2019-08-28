package de.hpi.cloud.common.utils

import java.net.URI
import java.net.URISyntaxException

fun String.tryParseUri(): URI? {
    return try {
        URI(this)
    } catch (e: URISyntaxException) {
        null
    }
}
