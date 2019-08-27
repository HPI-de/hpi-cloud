package de.hpi.cloud.food

import com.beust.klaxon.Klaxon
import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.i18nMap
import de.hpi.cloud.common.utils.protobuf.euro
import de.hpi.cloud.common.utils.protobuf.toDbMap
import de.hpi.cloud.common.utils.protobuf.toTimestamp
import de.hpi.cloud.food.utils.openStreamWith
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenMensaCrawler(
    val canteenData: StaticCanteenData
) {

    val canteenQuery = "canteens/${canteenData.canteenOpenMensaId}"
    val daysQuery = "$canteenQuery/days"
    val mealsQuery = { date: LocalDate -> "$daysQuery/${date.format(ISO8601_DATE_FORMAT)}/meals" }

    private fun streamJsonApi(query: String) = baseUri.resolve(query).toURL()
        .openStreamWith(
            "User-agent" to USER_AGENT,
            "Accept" to "application/json, text/plain"
        )

    val openMensaInfo: OpenMensaInformation by lazy {
        streamJsonApi(canteenQuery)
            .use {
                KLAXON.parse<OpenMensaInformation>(it)!!
            }
    }

    companion object {
        private const val OPENMENSA_API_VERSION = "v2"

        val KLAXON = Klaxon()
        val baseUri: URI = URI("https://openmensa.org/api/$OPENMENSA_API_VERSION/")
        val ISO8601_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE!!
        val COMPACT_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")!!
    }

    data class DateStatus(
        val date: LocalDate,
        val isOpen: Boolean
    ) {}

    fun queryDays() = streamJsonApi(daysQuery)
        .bufferedReader()
        .use { reader ->
            KLAXON.parseJsonArray(reader)
                .mapChildrenObjectsOnly {
                    DateStatus(
                        date = LocalDate.parse(
                            it.string("date")!!,
                            ISO8601_DATE_FORMAT
                        ),
                        isOpen = !it.boolean("closed")!!
                    )
                }
                .sortedBy { dateStatus -> dateStatus.date }
        }

    inner class MensaMeal(
        val openMensaMeal: OpenMensaMeal,
        val date: LocalDate
    ) : Entity("menuItem", 1) {

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
            "Alkohol" to "alcohol"
        )

        var uniquifyIdSuffix: Int? = null

        private val counter = canteenData.counterFinder(openMensaMeal)
        override val id
            get() = "${canteenData.canteenId}-${date.format(COMPACT_DATE_FORMAT)}-${counter
                ?: openMensaId}${"_${uniquifyIdSuffix}".takeIf { uniquifyIdSuffix != null } ?: ""}"
        private val openMensaId get() = openMensaMeal.id
        private val offerName = openMensaMeal.category
        private val title = openMensaMeal.name.replace("\n", "")
        private val labelIds = openMensaMeal.notes.map { note ->
            LABEL_MAPPING[note] ?: error("Unknown label \"${note}\"")
        }

        override fun valueToMap() = mapOf(
            "restaurantId" to canteenData.canteenId,
            "openMensaId" to openMensaId,
            "date" to date.toTimestamp().toDbMap(), // TODO: maybe use https://github.com/googleapis/googleapis/blob/master/google/type/date.proto
            "offerName" to offerName,
            "title" to i18nMap(de = title),
            "counter" to i18nMap(de = counter),
            "labelIds" to labelIds,
            "prices" to prices(openMensaMeal.prices)
        )

        private fun prices(prices: Map<String, Double>) = prices
            .mapKeys {
                when (it.key) {
                    "others" -> "default"
                    else -> it.key
                }
            }
            .mapValues {
                it.value.euro().toDbMap()
            }
    }

    fun queryMeals(date: LocalDate) = streamJsonApi(mealsQuery(date))
        .bufferedReader()
        .use { reader ->
            KLAXON.parseArray<OpenMensaMeal>(reader)!!
                .map { mealData ->
                    MensaMeal(
                        openMensaMeal = mealData,
                        date = date
                    )
                }
                // meals with the same counter have the same id - let's fix this
                .groupBy { it.id }
                .flatMap { duplicateMenuItems ->
                    if (duplicateMenuItems.value.size > 1)
                        duplicateMenuItems.value.mapIndexed { index, meal ->
                            meal.apply { uniquifyIdSuffix = index + 1 }
                        }
                    else // no duplicate
                        duplicateMenuItems.value
                }
        }
}
