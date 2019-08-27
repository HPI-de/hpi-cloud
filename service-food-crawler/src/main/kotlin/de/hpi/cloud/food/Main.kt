package de.hpi.cloud.food

const val NAME = "HPI-MobileDev-Crawler[Food]"
const val VERSION = "0.0.1"
val CRAWLER_INFO
    get() = "$NAME/$VERSION"
val USER_AGENT = "$CRAWLER_INFO klaxon/5.0.11 Kotlin-runtime/${KotlinVersion.CURRENT}"

val KNOWN_CANTEENS = setOf(
    StaticCanteenData(
        canteenOpenMensaId = 62,
        canteenId = "mensaGriebnitzsee",
        counterFinder = { meal ->
            when {
                // TODO: translations
                meal.categoryMatches("Angebot 1") -> "1"
                meal.categoryMatches("Angebot 2") -> "2"
                meal.categoryMatches("Angebot 3") -> "3"
                meal.categoryMatches("Angebot 4") -> "4"
                meal.categoryMatches("Angebot 6") -> "Terrasse" // Hamburger
                meal.categoryMatches("Nudeltheke") -> "Nudeltheke"
                meal.categoryMatches("Tagessuppe") -> "Suppentheke"
                // Idk, ob Salattheke erwähnt wird
                else -> {
                    println("Unknown meal category/counter \"${meal.category}\" in ${meal.id}")
                    null
                }
            }
        }
    ),
    StaticCanteenData(
        canteenOpenMensaId = 112,
        canteenId = "ulfsCafe",
        counterFinder = { _ -> null }
    )
)

fun main(args: Array<String>) {
    // TODO: flag allDays/today
    println("~~~ $NAME ~~~")

    KNOWN_CANTEENS
        .map { { OpenMensaCrawler(it) } }
        .first().let {
            val crawler = it()
            println("Starting crawler $CRAWLER_INFO")
            println("Using User-Agent=\"$USER_AGENT\"")
            val days = crawler.queryDays()
            days.filter { it.isOpen }
                .forEach {
                    println("Date ${it.date}:")
                    crawler.queryMeals(it.date)
                        .forEach { meal ->
                            println(meal.toJsonDocument())
                        }
                }
        }
}
