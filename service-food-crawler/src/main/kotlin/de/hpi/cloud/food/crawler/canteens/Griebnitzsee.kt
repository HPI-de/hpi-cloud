package de.hpi.cloud.food.crawler.canteens

import de.hpi.cloud.common.utils.getOrElse
import de.hpi.cloud.food.crawler.CanteenData
import de.hpi.cloud.food.crawler.OpenMensaMeal

object Griebnitzsee : CanteenData("mensaGriebnitzsee", 62) {
    private val LABEL_MAPPING = mapOf(
        "Vital" to "vital",
        "Vegetarisch" to "vegetarian",
        "Vegan" to "vegan",
        "Schweinefleisch" to "pork",
        "Rindfleisch" to "beef",
        "Lamm" to "lamb",
        "Knoblauch" to "garlic",
        "Gefluegel" to "poultry",
        "Fisch" to "fish",
        "Alkohol" to "alcohol",
        "Outdoor" to null // ignore this label
    )

    override fun findLabels(meal: OpenMensaMeal): List<String> = meal
        .notes
        .map { it.trim() }
        .mapNotNull { note ->
            LABEL_MAPPING.getOrElse(note) {
                println("Unknown label \"${note}\"")
                null
            }
        }

    fun OpenMensaMeal.categoryMatches(string: String) = category.startsWith(string, ignoreCase = true)
    override fun findCounter(meal: OpenMensaMeal): String? = when {
        // TODO: translations
        meal.categoryMatches("Angebot 1") -> "1"
        meal.categoryMatches("Angebot 2") -> "2"
        meal.categoryMatches("Angebot 3") -> "3"
        meal.categoryMatches("Angebot 4") -> "4"
        meal.categoryMatches("Angebot 6") -> "Terrasse" // Hamburger
        meal.categoryMatches("Nudeltheke") -> "Nudeltheke"
        meal.categoryMatches("Tagessuppe") -> "Suppentheke"
        else -> {
            println("Unknown meal category/counter \"${meal.category}\" in ${meal.id}")
            null
        }
    }
}
