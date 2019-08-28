package de.hpi.cloud.food.crawler.utils

import java.net.URL

fun URL.openConnectionWith(vararg properties: Pair<String, String>) =
    openConnection()!!
        .apply {
            properties.forEach { setRequestProperty(it.first, it.second) }
        }

fun URL.openStreamWith(vararg properties: Pair<String, String>) = openConnectionWith(*properties).inputStream!!
