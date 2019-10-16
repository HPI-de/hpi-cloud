package de.hpi.cloud.food.crawler

data class OpenMensaMeal(
    val id: Int,
    val name: String,
    val category: String,
    val prices: Map<String, Double>,
    val notes: List<String>
)

open class CanteenData(
    val id: String,
    val openMensaId: Int
) {
    open fun findOfferName(meal: OpenMensaMeal): String = meal.category
    open fun findLabels(meal: OpenMensaMeal): List<String> = listOf()
    open fun findCounter(meal: OpenMensaMeal): String? = null

    open fun mapReduce(meals: List<MensaMeal>): List<MensaMeal> = meals
    open fun deduplicate(meals: List<MensaMeal>): List<MensaMeal> = meals
        // meals with the same counter have the same id - let's fix this
        .groupBy { it.id }
        .flatMap {
            if (it.value.size > 1)
                it.value.mapIndexed { index, meal ->
                    meal.copy(uniqueIdSuffix = index + 1)
                }
            else // no duplicate
                it.value
        }
}
