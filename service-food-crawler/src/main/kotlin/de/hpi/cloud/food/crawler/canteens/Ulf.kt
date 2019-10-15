package de.hpi.cloud.food.crawler.canteens

import de.hpi.cloud.food.crawler.CanteenData
import de.hpi.cloud.food.crawler.MensaMeal

object Ulf : CanteenData("ulfsCafe", 112) {
    override fun mapReduce(meals: List<MensaMeal>): List<MensaMeal> = meals
        // ask Kotlin why I have to specify the type for R here
        .flatMap {
            val alternativeOffer = it.openMensaMeal.notes
                .joinToString(separator = " ")
                .trim()
            if (alternativeOffer.startsWith("oder ", ignoreCase = true)) {
                listOf(it, it.copy(title = alternativeOffer.substring(5)))
            } else
                listOf(it)
        }
}
