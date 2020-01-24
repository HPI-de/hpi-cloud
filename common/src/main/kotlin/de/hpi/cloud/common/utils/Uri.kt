package de.hpi.cloud.common.utils

import java.net.URI
import java.net.URISyntaxException
import java.net.URL

fun String.parseUri(): URI = URI(this)
fun String.tryParseUri(): URI? {
    return try {
        parseUri()
    } catch (e: URISyntaxException) {
        null
    }
}

fun String.parseUrl(): URL = URL(this)
fun String.tryParseUrl(): URL? {
    return try {
        parseUrl()
    } catch (e: URISyntaxException) {
        null
    }
}
