package de.hpi.cloud.food.crawler

import com.beust.klaxon.Klaxon
import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.i18nMap
import de.hpi.cloud.common.utils.protobuf.euros
import de.hpi.cloud.common.utils.protobuf.toDbMap
import de.hpi.cloud.common.utils.protobuf.toTimestamp
import de.hpi.cloud.food.crawler.utils.openStreamWith
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenMensaCrawler(
    val canteenData: CanteenData
) {
    companion object {
        private const val OPENMENSA_API_VERSION = "v2"

        val BASE_URI: URI = URI("https://openmensa.org/api/$OPENMENSA_API_VERSION/")
        val DATE_FORMAT_ISO8601 = DateTimeFormatter.ISO_LOCAL_DATE!!
        val DATE_FORMAT_COMPACT = DateTimeFormatter.ofPattern("yyyyMMdd")!!
    }

    private val klaxon = Klaxon()

    private fun canteenQuery() = "canteens/${canteenData.openMensaId}"
    private fun daysQuery() = "${canteenQuery()}/days"
    private fun mealsQuery(date: LocalDate) = "${daysQuery()}/${date.format(DATE_FORMAT_ISO8601)}/meals"

    private fun streamJsonApi(query: String) = BASE_URI.resolve(query).toURL()
        .openStreamWith(
            "User-agent" to USER_AGENT,
            "Accept" to "application/json, text/plain"
        )

    fun queryDays() = streamJsonApi(daysQuery())
        .bufferedReader()
        .use { reader ->
            klaxon.parseJsonArray(reader)
                .mapChildrenObjectsOnly {
                    DateStatus(
                        date = LocalDate.parse(it.string("date")!!, DATE_FORMAT_ISO8601),
                        isOpen = !it.boolean("closed")!!
                    )
                }
                .sortedBy { it.date }
        }


    fun queryMeals(date: LocalDate) = streamJsonApi(mealsQuery(date))
        .bufferedReader()
        .use { reader ->
            klaxon.parseArray<OpenMensaMeal>(reader)!!
                .flatMap { MensaMeal.parseMeals(canteenData, date, it) }
                .let { canteenData.mapReduce(it) }
                .let { canteenData.deduplicate(it) }
        }
}

data class DateStatus(
    val date: LocalDate,
    val isOpen: Boolean
)

data class MensaMeal(
    private val canteenData: CanteenData,
    val openMensaMeal: OpenMensaMeal,
    val date: LocalDate,
    val counter: String?,
    val title: String,
    val offerName: String,
    val labelIds: List<String>,
    val uniqueIdSuffix: Int? = null
) : Entity("menuItem", 1) {
    companion object {
        fun parseMeals(canteenData: CanteenData, date: LocalDate, openMensaMeal: OpenMensaMeal) =
            MensaMeal(
                canteenData,
                openMensaMeal,
                date,
                canteenData.findCounter(openMensaMeal),
                openMensaMeal.name.replace("\n", ""),
                canteenData.findOfferName(openMensaMeal),
                canteenData.findLabels(openMensaMeal)
            ).let { listOf(it) }
    }

    override val id
        get() = canteenData.id +
                "-${date.format(OpenMensaCrawler.DATE_FORMAT_COMPACT)}" +
                "-${counter ?: openMensaId}" +
                (uniqueIdSuffix?.let { "_$it" } ?: "")
    private val openMensaId get() = openMensaMeal.id

    private val prices = openMensaMeal.prices
        .mapKeys {
            if (it.key == "others") "default"
            else it.key
        }
        .mapValues {
            it.value.euros().toDbMap()
        }

    override fun valueToMap() = mapOf(
        "restaurantId" to canteenData.id,
        "openMensaId" to openMensaId,
        "date" to date.toTimestamp().toDbMap(),
        "offerName" to offerName,
        "title" to i18nMap(de = title),
        "counter" to i18nMap(de = counter),
        "labelIds" to labelIds,
        "prices" to prices
    )
}
