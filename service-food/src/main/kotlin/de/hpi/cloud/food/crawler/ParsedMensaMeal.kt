package de.hpi.cloud.food.crawler

import de.hpi.cloud.food.crawler.openmensa.CanteenData
import de.hpi.cloud.food.crawler.openmensa.OpenMensaMeal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ParsedMensaMeal(
    private val canteenData: CanteenData,
    val openMensaMeal: OpenMensaMeal,
    val date: LocalDate,
    val counterName: String?,
    val title: String,
    val offerName: String,
    val labelIds: List<String>,
    val uniqueIdSuffix: Int? = null
) {
    companion object {
        val DATE_FORMAT_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd")!!

        fun parseMeals(canteenData: CanteenData, date: LocalDate, openMensaMeal: OpenMensaMeal) =
            ParsedMensaMeal(
                canteenData = canteenData,
                openMensaMeal = openMensaMeal,
                date = date,
                counterName = canteenData.findCounter(openMensaMeal),
                title = openMensaMeal.name.replace("\n", "").trim(),
                offerName = canteenData.findOfferName(openMensaMeal),
                labelIds = canteenData.findLabels(openMensaMeal)
            ).let { listOf(it) }
    }

    val id get() = canteenData.id.value +
                "-${date.format(DATE_FORMAT_COMPACT)}" +
                "-${counterName ?: openMensaId}" +
                (uniqueIdSuffix?.let { "_$it" } ?: "")
    val openMensaId get() = openMensaMeal.id

    val prices: Map<String, Double> = openMensaMeal.prices
        .mapKeys {
            if (it.key == "others") "default"
            else it.key
        }
}
