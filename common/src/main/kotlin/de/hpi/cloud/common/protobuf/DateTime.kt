package de.hpi.cloud.common.protobuf

import com.google.type.Date
import java.time.LocalDate

fun Date.toLocalDate(): LocalDate = LocalDate.of(year, month, day)
fun LocalDate.toProtoDate(): Date = Date.newBuilder()
    .setYear(year)
    .setMonth(monthValue)
    .setDay(dayOfMonth)
    .build()
