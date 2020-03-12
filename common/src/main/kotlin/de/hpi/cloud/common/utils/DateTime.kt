package de.hpi.cloud.common.utils

import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

fun Instant.toIsoString() = DateTimeFormatter.ISO_INSTANT.format(this)
fun LocalDate.toIsoString() = DateTimeFormatter.ISO_LOCAL_DATE.format(this)
