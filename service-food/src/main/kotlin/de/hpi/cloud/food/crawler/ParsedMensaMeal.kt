package de.hpi.cloud.food.crawler

import de.hpi.cloud.food.crawler.openmensa.CanteenData
import de.hpi.cloud.food.crawler.openmensa.OpenMensaMeal
import java.time.LocalDate

data class ParsedMensaMeal(
    private val canteenData: CanteenData,
    val openMensaMeal: OpenMensaMeal,
    val date: LocalDate,
    val counter: String?,
    val title: String,
    val offerName: String,
    val labelIds: List<String>,
    val uniqueIdSuffix: Int? = null
) {
    companion object {
        fun parseMeals(canteenData: CanteenData, date: LocalDate, openMensaMeal: OpenMensaMeal) =
            ParsedMensaMeal(
                canteenData = canteenData,
                openMensaMeal = openMensaMeal,
                date = date,
                counter = canteenData.findCounter(openMensaMeal),
                title = openMensaMeal.name.replace("\n", "").trim(),
                offerName = canteenData.findOfferName(openMensaMeal),
                labelIds = canteenData.findLabels(openMensaMeal)
            ).let { listOf(it) }
    }

    val id get() = canteenData.id.value +
                "-${date.format(OpenMensaCrawler.DATE_FORMAT_COMPACT)}" +
                "-${counter ?: openMensaId}" +
                (uniqueIdSuffix?.let { "_$it" } ?: "")
    val openMensaId get() = openMensaMeal.id

    val prices: Map<String, Double> = openMensaMeal.prices
        .mapKeys {
            if (it.key == "others") "default"
            else it.key
        }
}
