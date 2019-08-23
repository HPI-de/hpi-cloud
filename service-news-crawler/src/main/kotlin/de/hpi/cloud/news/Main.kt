package de.hpi.cloud.news

import de.hpi.cloud.news.crawler.HpiMediaArchiveCrawler
import java.time.Duration
import java.time.LocalDate
import java.time.Period

public val Int.days get() = Period.ofDays(this)
val NAME = "HPI-MobileDev-Crawler[News]"
val USER_AGENT_TEMPLATE = "$NAME%s jsoup/1.12.1 Kotlin-runtime/${KotlinVersion.CURRENT}"
val DEFAULT_MAX_UPDATE_PERIOD = 31.days

val CRAWLERS = setOf(
    { HpiMediaArchiveCrawler() }
)

fun main(args: Array<String>) {
    val MAX_UPDATE_PERIOD = args
        .firstOrNull()
        ?.toIntOrNull()
        ?.days
        ?: DEFAULT_MAX_UPDATE_PERIOD
    println("~~~ $NAME ~~~")
    println("Crawling the last ${MAX_UPDATE_PERIOD.days} days")

    val cbs = CouchbaseSyncer
    val now = LocalDate.now()

    CRAWLERS.forEach {
        val crawler = it()
        println("Starting crawler [${crawler.serviceName}]/${crawler.crawlerVersion}")
        println("Using User-Agent=\"${crawler.userAgent}\"")

        crawler
            .archive
            .query()
            .takeWhile { now - MAX_UPDATE_PERIOD < it.date }
            .map { crawler.resolver.resolvePreview(it) }
            .let { articles ->
                cbs.dbOps(CouchbaseSyncer.BUCKET_NAME) { bucket ->
                    var count = 0
                    articles.forEach {
                        // TODO: don't update if nothing changed
                        // TODO: generate etag
                        println("Parsed article \"${it.id}\"")
                        bucket.upsert(it.toJsonDocument())
                        count++
                    }
                    println("Upserted ${count} articles")
                }
            }
        println("Crawler used ${crawler.requestCount} server requests")
    }

//    val crawler = HpiMediaArchiveCrawler()
//    crawler.query()
//        .take(5)
//        .forEach {
//            println(it.toJsonObject())
//        }
}
