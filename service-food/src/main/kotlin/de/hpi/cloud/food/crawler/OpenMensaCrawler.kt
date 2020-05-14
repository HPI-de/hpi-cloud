package de.hpi.cloud.food.crawler

import com.beust.klaxon.Klaxon
import de.hpi.cloud.food.crawler.openmensa.CanteenData
import de.hpi.cloud.food.crawler.openmensa.OpenMensaMeal
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
                        date = LocalDate.parse(
                            it.string("date")!!,
                            DATE_FORMAT_ISO8601
                        ),
                        isOpen = !it.boolean("closed")!!
                    )
                }
                .sortedBy { it.date }
        }


    fun queryMeals(date: LocalDate) = streamJsonApi(mealsQuery(date))
        .bufferedReader()
        .use { reader ->
            klaxon.parseArray<OpenMensaMeal>(reader)!!
                .flatMap {
                    ParsedMensaMeal.parseMeals(
                        canteenData,
                        date,
                        it
                    )
                }
                .let { canteenData.mapReduce(it) }
                .let { canteenData.deduplicate(it) }
        }
}

data class DateStatus(
    val date: LocalDate,
    val isOpen: Boolean
)

