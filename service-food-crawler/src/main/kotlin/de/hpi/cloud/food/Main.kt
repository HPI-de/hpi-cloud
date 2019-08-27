package de.hpi.cloud.food

import de.hpi.cloud.common.utils.couchbase.withBucket
import java.time.LocalDate

const val NAME = "HPI-MobileDev-Crawler[Food]"
const val VERSION = "1.0.0"
val CRAWLER_INFO
    get() = "$NAME/$VERSION"
val USER_AGENT = "$CRAWLER_INFO klaxon/5.0.11 Kotlin-runtime/${KotlinVersion.CURRENT}"

val KNOWN_CANTEENS = setOf(
    StaticCanteenData(
        canteenOpenMensaId = 62,
        canteenId = "mensaGriebnitzsee",
        counterFinder = { meal ->
            when {
                // TODO: translations
                meal.categoryMatches("Angebot 1") -> "1"
                meal.categoryMatches("Angebot 2") -> "2"
                meal.categoryMatches("Angebot 3") -> "3"
                meal.categoryMatches("Angebot 4") -> "4"
                meal.categoryMatches("Angebot 6") -> "Terrasse" // Hamburger
                meal.categoryMatches("Nudeltheke") -> "Nudeltheke"
                meal.categoryMatches("Tagessuppe") -> "Suppentheke"
                // Idk, ob Salattheke erwähnt wird
                else -> {
                    println("Unknown meal category/counter \"${meal.category}\" in ${meal.id}")
                    null
                }
            }
        }
    ),
    StaticCanteenData(
        canteenOpenMensaId = 112,
        canteenId = "ulfsCafe",
        counterFinder = { _ -> null }
    )
)

fun main(args: Array<String>) {
    val todayOnly = args.any { it.equals("--today", true) }

    println("~~~ $NAME ~~~")
    withBucket("food") {bucket ->
        // TODO: clear previous data
        KNOWN_CANTEENS
            .map { OpenMensaCrawler(it) }
            .forEach { crawler ->
                println("Starting crawler $CRAWLER_INFO - ${crawler.canteenData.canteenId}")
                println("Using User-Agent=\"$USER_AGENT\"")

                val updateDay = { date: LocalDate ->
                    println("Date: $date")
                    crawler.queryMeals(date)
                        .forEach { meal ->
                            println("> ${meal.id}")
                            bucket.upsert(meal.toJsonDocument())
                        }
                }

                if (todayOnly) {
                    updateDay(LocalDate.now())
                } else {
                    crawler.queryDays()
                        .filter { it.isOpen }
                        .forEach { updateDay(it.date) }
                }
            }
    }
}
