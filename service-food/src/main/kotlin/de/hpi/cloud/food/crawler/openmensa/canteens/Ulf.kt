package de.hpi.cloud.food.crawler.openmensa.canteens

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.food.crawler.ParsedMensaMeal
import de.hpi.cloud.food.crawler.openmensa.CanteenData
import de.hpi.cloud.food.fixtures.labelFixture

object Ulf : CanteenData(Id("ulfsCafe"), 112) {
    val PRICE_REGEX = Regex("^(.*?)\\s+(\\d,\\d\\d?)\\s?€?\$")

    override fun mapReduce(meals: List<ParsedMensaMeal>) = meals
        .flatMap { meal ->
            val alternativeOffer = meal.openMensaMeal.notes
                .joinToString(separator = " ")
                .trim()
                .substringAfter("oder ")

            if (alternativeOffer.isNotEmpty()) {
                // example: https://openmensa.org/c/112/2020-04-23
                PRICE_REGEX.matchEntire(alternativeOffer)
                    ?.let { match ->
                        match.groupValues[2]
                            .replace(',', '.')
                            .toDoubleOrNull()
                            ?.let { price ->
                                val alternativeOMM = meal.openMensaMeal.copy(prices = mapOf("others" to price))
                                val alternative =
                                    meal.copy(title = match.groupValues[1], openMensaMeal = alternativeOMM)
                                listOf(meal, alternative)
                            }
                    }
                    ?: listOf(meal, meal.copy(title = alternativeOffer))
            } else {
                listOf(meal)
            }
        }
        .map { meal ->
            val titleWords = meal.title.split(' ')

            val foundLabels = labelFixture
                .filter { label -> titleWords.any { word -> label.matches(word, startOnly = true) } }

            if (foundLabels.isNotEmpty())
                meal.copy(labelIds = foundLabels.map { it.id.value })
            else
                meal
        }
}
