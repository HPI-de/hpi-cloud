package de.hpi.cloud.common.utils

import java.time.Instant
import java.time.format.DateTimeFormatter

fun Instant.toIsoString() = DateTimeFormatter.ISO_INSTANT.format(this)
