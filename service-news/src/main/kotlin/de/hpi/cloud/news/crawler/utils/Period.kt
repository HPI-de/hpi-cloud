package de.hpi.cloud.news.crawler.utils

import java.time.Period

val Int.days get() = Period.ofDays(this)
