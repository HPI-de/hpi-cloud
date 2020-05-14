package de.hpi.cloud.food.crawler.openmensa.canteens

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.food.crawler.ParsedMensaMeal
import de.hpi.cloud.food.crawler.openmensa.CanteenData
object Ulf : CanteenData(Id("ulfsCafe"), 112) {

    override fun mapReduce(meals: List<ParsedMensaMeal>) = meals
        .flatMap {
            val alternativeOffer = it.openMensaMeal.notes
                .joinToString(separator = " ")
                .trim()
            if (alternativeOffer.startsWith("oder ", ignoreCase = true))
                listOf(it, it.copy(title = alternativeOffer.substring(5)))
            else
                listOf(it)
        }
}
