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
        KNOWN_CANTEENS
            .map { OpenMensaCrawler(it) }
            .forEach { crawler ->
                println("\nStarting crawler $CRAWLER_INFO - ${crawler.canteenData}")
                println("Using User-Agent=\"$USER_AGENT\"")

                try {
                    val days =
                        if (todayOnly) listOf(LocalDate.now())
                        else crawler.queryDays()
                            .filter { it.isOpen }
                            .map { it.date }
                    if (days.isEmpty())
                        println("No days available")
                    else days.forEach { date ->
                        println("\nQuerying date $date:")

                        val meals = crawler.queryMeals(date)
                        meals.forEach { meal ->
                            // populate counters
                            // TODO: remove when we've gathered all counters in the database
                            val counterId = meal.counterName?.let { Id<Counter>(it) }
                            val counter = meal.counterName?.let { name ->
                                Counter(
                                    crawler.canteenData.id,
                                    title = name.l10n(GERMAN)
                                )
                            }
                            if (counter != null)
                                bucket.upsert(counter.createNewWrapper(FOOD_CONTEXT, counterId!!))

                            val menuItem = MenuItem(
                                openMensaId = meal.openMensaId.toString(),
                                date = meal.date,
                                restaurantId = crawler.canteenData.id,
                                offerTitle = meal.offerName.l10n(GERMAN),
                                title = meal.title.l10n(GERMAN),
                                counterId = counter?.let { counterId },
                                labelIds = meal.labelIds.map { Id<Label>(it) },
                                prices = meal.prices.mapValues { (_, price) -> Money.eur(price) }
                            )
                            println("- $meal\n$menuItem\n")
                            bucket.upsert(menuItem.createNewWrapper(FOOD_CONTEXT, Id(meal.id)))
                        }
                        println("${meals.size} meals found for $date")
                    }
                } catch (ex: IOException) {
                    ex.printStackTrace()
                }
            }
    }
}
