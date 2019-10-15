package de.hpi.cloud.food.crawler

import de.hpi.cloud.common.utils.couchbase.withBucket
import de.hpi.cloud.food.crawler.canteens.Griebnitzsee
import de.hpi.cloud.food.crawler.canteens.Ulf
import java.time.LocalDate

const val NAME = "HPI-MobileDev-Crawler[Food]"
const val VERSION = "1.0.0"
const val CRAWLER_INFO = "$NAME/$VERSION"
val USER_AGENT = "$CRAWLER_INFO klaxon/5.0.11 Kotlin-runtime/${KotlinVersion.CURRENT}"

val KNOWN_CANTEENS = setOf(
    Griebnitzsee,
    Ulf
)

fun main(args: Array<String>) {
    val todayOnly = args.any { it.equals("--today", true) }

    println("Starting $NAME")
    withBucket("food") { bucket ->
        // TODO: clear previous data
        KNOWN_CANTEENS
            .map { OpenMensaCrawler(it) }
            .forEach { crawler ->
                println("Starting crawler $CRAWLER_INFO - ${crawler.canteenData.id}")
                println("Using User-Agent=\"$USER_AGENT\"")

                val days =
                    if (todayOnly) listOf(LocalDate.now())
                    else crawler.queryDays()
                        .filter { it.isOpen }
                        .map { it.date }
                days.forEach { date ->
                    print("Date $date: ")

                    val meals = crawler.queryMeals(date)
                    meals.forEach { bucket.upsert(it.toJsonDocument()) }
                    println("${meals.size} meals")
                }
            }
    }
}
