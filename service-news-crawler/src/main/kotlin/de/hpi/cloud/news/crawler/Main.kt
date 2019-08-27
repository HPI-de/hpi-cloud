package de.hpi.cloud.news.crawler

import de.hpi.cloud.common.utils.couchbase.withBucket
import de.hpi.cloud.news.crawler.utils.days
import java.time.LocalDate

const val NAME = "HPI-MobileDev-Crawler[News]"
val USER_AGENT_TEMPLATE = "$NAME%s jsoup/1.12.1 Kotlin-runtime/${KotlinVersion.CURRENT}"

val CRAWLERS = setOf(
    { HpiMediaArchiveCrawler() }
)

fun main(args: Array<String>) {
    val updatePeriod = args.firstOrNull()?.toIntOrNull()?.days
    println("Starting $NAME")
    println("Crawling " + (updatePeriod?.let { "the last ${it.days} days" } ?: "everything"))

    val now = LocalDate.now()

    CRAWLERS.forEach {
        val crawler = it()
        println("Starting crawler [${crawler.serviceName}]/${crawler.crawlerVersion}")
        println("Using User-Agent=\"${crawler.userAgent}\"")

        val articles = crawler
            .archive
            .query()
            .takeWhile { updatePeriod == null || (now - updatePeriod < it.publishedAt.toLocalDate()) }
            .map { crawler.resolver.resolvePreview(it) }
        withBucket("news") { bucket ->
            var count = 0
            articles.forEach {
                println("Parsed article with ID ${it.id}")
                bucket.upsert(it.toJsonDocument())
                count++
            }
            println("Upserted $count articles")
        }
        println("Crawler used ${crawler.metrics.requestCount} server requests")
    }
}
