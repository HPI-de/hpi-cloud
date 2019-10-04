package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.Entity
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.io.File
import java.io.PrintWriter
import java.net.URI
import java.net.URL

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

fun createDocumentQuery(url: URL): Connection {
    return Jsoup
        .connect(url.toString())
        .userAgent(USER_AGENT_STRING)
}

fun main() {
    println("Starting $NAME")
    println("Crawling current semester") // TODO: implement archive crawler

    CRAWLERS
        .stream()
        .flatMap {
            println("Starting crawler for ${it}")
            println("Using User-Agent=\"$USER_AGENT_STRING\"")
            it.listCourses()
        }
        .map { it.query() }
        .forEach { cd ->
            val name = cd.id
            File("./output/$name.txt")
                .also { it.createNewFile() }
                .printWriter()
                .use { pw ->
                    println("writing $name")
                    pw.print("CourseDetail[${cd.id}]")
                    pw.printlnEntity(cd)
                    pw.print("Course[${cd.course.id}]")
                    pw.printlnEntity(cd.course)
                    pw.print("CourseSeries[${cd.course.courseSeries.id}]")
                    pw.printlnEntity(cd.course.courseSeries)
                }
        }

//        withBucket("news") { bucket ->
//            var count = 0
//            articles.forEach {
//                println("Parsed article with ID ${it.id}")
//                bucket.upsert(it.toJsonDocument())
//                count++
//            }
//            println("Upserted $count articles")
//        }
//        println("Crawler used ${crawler.metrics.requestCount} server requests")
}

fun PrintWriter.printlnEntity(entity: Entity) {
    printlnMap(entity.valueToMap())
}
fun PrintWriter.printlnMap(map: Map<String, Any?>) {
    println(map.entries.joinToString(
        separator = ",\n",
        prefix = "{\n",
        postfix = "\n}"
    ) { "\"${it.key}\": ${it.value}" })
}
