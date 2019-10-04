package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.cut

data class Semester(
    val year: Int,
    val term: Term
) : Entity("semester", 1) {

    override val id get() = "$year${term.abbreviation}"

    override fun valueToMap() = mapOf(
        "year" to year,
        "term" to term.toString()
    )

    override fun toString() = id

    enum class Term(
        val abbreviation: String,
        val title: String
    ) {
        WINTER("ws", "Wintersemester"),
        SUMMER("ss", "Sommersemester");

        override fun toString(): String {
            return name.toLowerCase()
        }

        companion object {
            fun parse(term: String) = values().firstOrNull { t -> term.startsWith(t.title, ignoreCase = true) }
                ?: values().firstOrNull { t -> term.startsWith(t.name, ignoreCase = true) }
                ?: values().first { t -> term.startsWith(t.abbreviation, ignoreCase = true) }
        }
    }

    companion object {
        fun parse(year: String, term: String) = Semester(
            year.cut('/').toInt(),
            Term.parse(term)
        )
    }
}
