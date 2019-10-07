package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.i18nSingle
import java.net.URL

data class CourseDetail(
    val course: Course,
    val teletask: URL?,
    val programs: Map<StudyPathDegree, Set<String>>,
    val description: String?,
    val requirements: String?,
    val learning: String?,
    val examination: String?,
    val dates: String?,
    val literature: String?,
    val entityLanguage: String
) : Entity("courseDetail", 1) {

    override val id get() = course.id

    override fun valueToMap() = mapOf(
        "teletask" to teletask?.toString(),
        "programs" to programs
            .mapKeys { entry -> entry.key.toString() }
            .mapValues { entry -> entry.value.toList() },
        "description" to i18nSingle(description, entityLanguage),
        "requirements" to i18nSingle(requirements, entityLanguage),
        "learning" to i18nSingle(learning, entityLanguage),
        "examination" to i18nSingle(examination, entityLanguage),
        "dates" to i18nSingle(dates, entityLanguage),
        "literature" to i18nSingle(literature, entityLanguage)
    )

    companion object {
        inline fun build(block: Builder.() -> Unit) =
            Builder().apply(block).build()
    }

    class Builder {
        lateinit var courseDetailEntityLanguage: String
        lateinit var course: Course
        var teletask: URL? = null
        lateinit var programs: Map<StudyPathDegree, Set<String>>
        var description: String? = null
        var requirements: String? = null
        var learning: String? = null
        var examination: String? = null
        var dates: String? = null
        var literature: String? = null
        fun build() = CourseDetail(
            course,
            teletask,
            programs,
            description,
            requirements,
            learning,
            examination,
            dates,
            literature,
            courseDetailEntityLanguage
        )
    }
}
