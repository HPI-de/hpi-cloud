package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.i18nSingle

data class CourseSeries(
    val title: String,
    val shortTitle: String,
    val abbreviation: String,
    val ects: Int?,
    val hoursPerWeek: Int?,
    val compulsory: Compulsory,
    val courseLanguage: CourseLanguage,
    val types: Set<Type>,
    val entityLanguage: String
) : Entity("courseSeries", 1) {
    init {
        fun invalidDataset(fieldName: String): Nothing = error("Invalid dataset: $fieldName not set")
        if (ects == null) invalidDataset("ECTS")
        if (hoursPerWeek == null) invalidDataset("Hours")
    }

    override val id
        get() = title.toLowerCase()
            .replace(Regex("[^a-z0-9]*"), "")

    override fun valueToMap() = mapOf(
        "title" to i18nSingle(title, entityLanguage),
        "shortTitle" to i18nSingle(shortTitle, entityLanguage),
        "abbreviation" to i18nSingle(abbreviation, entityLanguage),
        "ects" to ects,
        "hoursPerWeek" to hoursPerWeek,
        "compulsory" to compulsory.name,
        "language" to courseLanguage.name,
        "types" to types.map { it.toString() }
    )

    enum class Compulsory {
        NON_COMPULSORY,
        COMPULSORY,
        BRIDGE;

        companion object {
            fun parse(string: String) = when (string.toLowerCase()) {
                "wahlpflichtmodul" -> NON_COMPULSORY
                "pflichtmodul" -> COMPULSORY
                "brückenmodul" -> BRIDGE
                else -> error("Unknown occupancy status \"$string\"")
            }
        }
    }

    enum class Type {
        LECTURE,
        SEMINAR,
        PROJECT,
        EXERCISE,
        BLOCK_SEMINAR;

        override fun toString(): String {
            return name.toLowerCase()
        }

        companion object {
            fun parse(string: String) = string.toLowerCase()
                .split('/')
                .flatMap {
                    when (it.trim()) {
                        "vorlesung" -> listOf(LECTURE)
                        "seminar" -> listOf(SEMINAR)
                        "projekt" -> listOf(PROJECT)
                        "übung" -> listOf(EXERCISE)
                        "blockseminar" -> listOf(BLOCK_SEMINAR)
                        "projektseminar" -> listOf(PROJECT, SEMINAR)
                        "vu" -> listOf(LECTURE, EXERCISE)
                        "sp" -> listOf(SEMINAR, PROJECT)
                        "s" -> listOf(SEMINAR)
                        "" -> listOf()
                        else -> {
                            println("Unknown type value \"$string\"")
                            listOf()
                        }
                    }
                }
                .filterNotNull()
                .distinct()
                .toSet()
        }
    }

    companion object {
        inline fun build(block: Builder.() -> Unit) =
            Builder().apply(block).build()
    }

    class Builder {
        lateinit var courseSeriesEntityLanguage: String
        lateinit var title: String
        lateinit var shortTitle: String
        lateinit var abbreviation: String
        var ects: Int? = null
        var hoursPerWeek: Int? = null
        lateinit var compulsory: Compulsory
        lateinit var courseLanguage: CourseLanguage
        lateinit var types: Set<Type>

        fun build() = CourseSeries(
            title,
            shortTitle,
            abbreviation,
            ects,
            hoursPerWeek,
            compulsory,
            courseLanguage,
            types,
            courseSeriesEntityLanguage
        )
    }
}
