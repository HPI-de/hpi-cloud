package de.hpi.cloud.food.crawler.openmensa

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.food.crawler.ParsedMensaMeal
import de.hpi.cloud.food.entities.Restaurant

open class CanteenData(
    val id: Id<Restaurant>,
    val openMensaId: Int
) {
    open fun findOfferName(meal: OpenMensaMeal): String = meal.category
    open fun findLabels(meal: OpenMensaMeal): List<String> = listOf()
    open fun findCounter(meal: OpenMensaMeal): String? = null

    open fun mapReduce(meals: List<ParsedMensaMeal>): List<ParsedMensaMeal> = meals
    open fun deduplicate(meals: List<ParsedMensaMeal>): List<ParsedMensaMeal> = meals
        // meals with the same counter have the same id - let's fix this
        .groupBy { it.id }
        .flatMap { (_, value) ->
            if (value.size > 1)
                value.mapIndexed { index, meal ->
                    meal.copy(uniqueIdSuffix = index + 1)
                }
            else // no duplicate
                value
        }

    override fun toString(): String = "$id[id=$openMensaId]"
}
