package de.hpi.cloud.food.utils

import java.net.URL

fun URL.openConnectionWith(vararg properties: Pair<String, String>) = this.openConnection()
    .apply {
        properties.forEach {
            setRequestProperty(it.first, it.second)
        }
    }

fun URL.openStreamWith(vararg properties: Pair<String, String>) = this.openConnectionWith(*properties).inputStream
