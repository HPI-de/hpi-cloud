package crawler

import crawler.utils.days
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.couchbase.toJsonDocument
import de.hpi.cloud.common.couchbase.upsert
import de.hpi.cloud.common.couchbase.withBucket
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

const val NAME = "HPI-MobileDev-Crawler[News]"
val USER_AGENT_TEMPLATE = "$NAME%s jsoup/1.12.1 Kotlin-runtime/${KotlinVersion.CURRENT}"

val CRAWLERS = setOf(
    { HpiMediaArchiveCrawler() }
)

val CRAWLER_CONTEXT = Context.forService("crawler-news")

fun main(args: Array<String>) {
    val updatePeriod = args.firstOrNull()?.toIntOrNull()?.days
    println("Starting $NAME")
    println("Crawling " + (updatePeriod?.let { "the last ${it.days} days" } ?: "everything"))

    val BERLIN_ZONE = ZoneId.of("Europe/Berlin")
    TimeZone.setDefault(TimeZone.getTimeZone(BERLIN_ZONE))
    val now = LocalDate.now()
    fun Instant.isInUpdatePeriod(): Boolean =
        if (updatePeriod == null) true
        else (now - updatePeriod < LocalDate.ofInstant(this, BERLIN_ZONE))

    CRAWLERS.forEach { createCrawler ->
        val crawler = createCrawler()
        println("Starting crawler [${crawler.serviceName}]/${crawler.crawlerVersion}")
        println("Using User-Agent=\"${crawler.userAgent}\"")

        val articles = crawler
            .archive
            .query()
            .takeWhile { preview -> preview.publishedAt.isInUpdatePeriod() }
            .map { crawler.resolver.resolvePreview(it) }
        withBucket("news") { bucket ->
            var count = 0
            articles.take(1) // TODO
                .forEach { (id, article) ->
                println("Parsed article with ID $id")
                bucket.upsert(
                    article.createNewWrapper(
                        context = CRAWLER_CONTEXT,
                        id = id
                    )
                )
                count++
            }
            println("Upserted $count articles")
        }
        println("Crawler used ${crawler.metrics.requestCount} server requests")
    }
}
