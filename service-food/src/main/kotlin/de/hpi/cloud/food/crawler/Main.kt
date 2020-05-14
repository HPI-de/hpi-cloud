package de.hpi.cloud.food.crawler

import de.hpi.cloud.common.couchbase.upsert
import de.hpi.cloud.common.couchbase.withBucket
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.types.Money
import de.hpi.cloud.common.types.l10n
import de.hpi.cloud.food.FOOD_CONTEXT
import de.hpi.cloud.food.crawler.openmensa.canteens.Griebnitzsee
import de.hpi.cloud.food.crawler.openmensa.canteens.Ulf
import de.hpi.cloud.food.entities.Counter
import de.hpi.cloud.food.entities.Label
import de.hpi.cloud.food.entities.MenuItem
import java.io.IOException
import java.net.URL
import java.time.LocalDate
import java.util.Locale.GERMAN

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
                println("Starting crawler $CRAWLER_INFO - ${crawler.canteenData}")
                println("Using User-Agent=\"$USER_AGENT\"")

                try {
                    val days =
                        if (todayOnly) listOf(LocalDate.now())
                        else crawler.queryDays()
                            .filter { it.isOpen }
                            .map { it.date }
                    days.forEach { date ->
                        print("Date $date: ")

                        val meals = crawler.queryMeals(date)
                        meals.forEach { meal ->
                            val counterId = meal.counter ?: "unbekannt"

                            // populate counters
                            // TODO: remove later
                            val counter = Counter(
                                crawler.canteenData.id,
                                title = counterId.l10n(GERMAN),
                                iconUrl = URL("")
                            )
                            bucket.upsert(counter.createNewWrapper(FOOD_CONTEXT, Id(counterId)))

                            val menuItem = MenuItem(
                                openMensaId = meal.openMensaId.toString(),
                                date = meal.date,
                                restaurantId = crawler.canteenData.id,
                                offerTitle = meal.offerName.l10n(GERMAN),
                                title = meal.title.l10n(GERMAN),
                                counterId = Id(meal.counter ?: "null"),
                                labelIds = meal.labelIds.map { Id<Label>(it) },
                                prices = meal.prices.mapValues { (_, price) -> Money.eur(price) }
                            )
                            println(meal)
                            println(menuItem)
                            bucket.upsert(menuItem.createNewWrapper(FOOD_CONTEXT, Id(meal.id)))
                        }
                        println("${meals.size} meals")
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
    }
}
