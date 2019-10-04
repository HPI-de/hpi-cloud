package de.hpi.cloud.common.utils

import java.net.MalformedURLException
import java.net.URL


fun String.tryParseUrl(): URL? {
    return try {
        URL(this)
    } catch (e: MalformedURLException) {
        null
    }
}

fun String.toUrl() = URL(this)
