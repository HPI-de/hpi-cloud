package de.hpi.cloud.common.utils

import java.net.URI
import java.net.URISyntaxException

fun String.parseUri(): URI = URI(this)
fun String.tryParseUri(): URI? {
    return try {
        parseUri()
    } catch (e: URISyntaxException) {
        null
    }
}
