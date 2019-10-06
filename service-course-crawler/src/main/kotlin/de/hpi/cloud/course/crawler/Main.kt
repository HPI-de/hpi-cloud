package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.withBucket
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.PrintWriter
import java.net.URI
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

const val NAME = "HPI-MobileDev-Crawler[Courses]"
val USER_AGENT_TEMPLATE = "$NAME%s jsoup/1.12.1 Kotlin-runtime/${KotlinVersion.CURRENT}"
val USER_AGENT_STRING = USER_AGENT_TEMPLATE.format(NAME)

val HPI_BASE_URI = URI("https://hpi.de/")

val CURRENT_SEMESTER = Semester(2019, Semester.Term.WINTER)
val CRAWLERS = setOf(
    HpiCourseListCrawler("IT-Systems Engineering", Degree.BACHELOR),
    HpiCourseListCrawler("IT-Systems Engineering", Degree.MASTER),
    HpiCourseListCrawler("Digital Health", Degree.MASTER),
    HpiCourseListCrawler("Data Engineering", Degree.MASTER),
    HpiCourseListCrawler("Cybersecurity", Degree.MASTER)
)

val requestCount = AtomicInteger(0)
fun createDocumentQuery(url: URL): Connection {
    requestCount.incrementAndGet()
    return Jsoup
        .connect(url.toString())
        .userAgent(USER_AGENT_STRING)
}

fun main() {
    println("Starting $NAME")
    println("Crawling current semester") // TODO: implement archive crawler
    println("Using User-Agent=\"$USER_AGENT_STRING\"")

    withBucket("course") { bucket ->
        var count = 0
        CRAWLERS
            .stream()
            .flatMap {
                println("Starting crawler for ${it}")
                it.listCourses()
            }
            .map { it.query() }
            .parallel()
            .forEach {
                val name = it.id

                println("Parsed course page with ID ${it.id}")
                bucket.upsert(it.toJsonDocument())
                bucket.upsert(it.course.toJsonDocument())
                bucket.upsert(it.course.courseSeries.toJsonDocument())
                count++
            }
        println("Upserted $count course page")
    }
    println("Crawler used ${requestCount} server requests")
}
