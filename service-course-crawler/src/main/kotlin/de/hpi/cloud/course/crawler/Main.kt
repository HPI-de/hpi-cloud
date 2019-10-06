package de.hpi.cloud.course.crawler

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.utils.couchbase.VIEW_BY_ID
import de.hpi.cloud.common.utils.couchbase.withBucket
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.net.URI
import java.net.URL
import java.util.concurrent.atomic.AtomicInteger

const val NAME = "HPI-MobileDev-Crawler[Courses]"
val USER_AGENT_STRING = "$NAME jsoup/1.12.1 Kotlin-runtime/${KotlinVersion.CURRENT}"

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

fun main(args: Array<String>) {
    println("Starting $NAME")
    withBucket("course") { bucket ->
        if (args.contains("--clear"))
            deleteOldData(bucket)
        else {
            println("Crawling current semester")
            println("Using User-Agent=\"$USER_AGENT_STRING\"")

            var count = 0
            CRAWLERS.asSequence()
                .flatMap {
                    println("Starting crawler for $it")
                    it.listCourses()
                }
                .map {
                    try {
                        it.query()
                    } catch (ex: Exception) {
                        println("error: $ex")
                        null
                    }
                }
                .filterNotNull()
                .forEach {
                    println("Parsed course page with ID ${it.id}")
                    bucket.upsert(it.toJsonDocument())
                    bucket.upsert(it.course.toJsonDocument())
                    bucket.upsert(it.course.courseSeries.toJsonDocument())
                    count++
                }
            println("Upserted $count course page")
        }
    }
    println("Crawler used ${requestCount} server requests")
}

fun deleteOldData(bucket: Bucket) {
    println("Entered database cleanup mode\n")
    bucket.query(ViewQuery.from("course", VIEW_BY_ID))
        .allRows()
        .forEach {
            println("Remove Course ${it.id()}")
            try {
                bucket.remove(it.id())
            } catch (ex: Exception) {
            }
        }
    println()
    bucket.query(ViewQuery.from("courseSeries", VIEW_BY_ID))
        .allRows()
        .forEach {
            println("Remove CourseSeries ${it.id()}")
            try {
                bucket.remove(it.id())
            } catch (ex: Exception) {
            }
        }
}
