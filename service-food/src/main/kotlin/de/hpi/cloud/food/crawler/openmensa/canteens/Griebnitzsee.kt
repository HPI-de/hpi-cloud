package de.hpi.cloud.food.crawler.openmensa.canteens

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.food.crawler.openmensa.CanteenData
import de.hpi.cloud.food.crawler.openmensa.OpenMensaMeal
import de.hpi.cloud.food.fixtures.labelFixture

object Griebnitzsee : CanteenData(Id("mensaGriebnitzsee"), 62) {
    private val LABEL_IGNORE_LIST = setOf(
        "ostern", "ball", "wm"
    )

    override fun findLabels(meal: OpenMensaMeal): List<String> = meal.notes
        .map { it.trim().toLowerCase() }
        .filterNot { it in LABEL_IGNORE_LIST }
        .mapNotNull { note ->
            val label = labelFixture.find { label -> label.matches(note) }
            if (label == null) println("Unknown label \"$note\"")
            label
        }
        .map { it.id.value }

    private fun OpenMensaMeal.categoryMatches(string: String) = category.startsWith(string, ignoreCase = true)
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
