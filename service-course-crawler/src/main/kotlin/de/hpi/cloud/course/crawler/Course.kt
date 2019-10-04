package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.i18nSingle
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Course(
    val courseSeries: CourseSeries,
    val semester: Semester,
    val lecturers: List<String>,
    val assistants: List<String>,
    val attendance: Int? = null,
    val enrollmentDeadline: LocalDate,
    val website: URL,
    val entityLanguage: String
) : Entity("course", 1) {

    override val id get() = "${courseSeries.id}_${semester.id}"

    override fun valueToMap() = mapOf(
        "courseSeriesId" to courseSeries.id,
        "semesterId" to semester.id,
        "lecturers" to lecturers,
        "assistants" to assistants,
        "attendance" to attendance,
        "enrollmentDeadline" to enrollmentDeadline.format(DateTimeFormatter.ISO_DATE),
        "website" to i18nSingle(website.toString(), courseSeries.entityLanguage)
    )


    companion object {
        inline fun build(entityLanguage: String, block: Builder.() -> Unit) =
            Builder(entityLanguage).apply(block).build()
    }

    data class Builder(
        val entityLanguage: String
    ) {
        lateinit var courseSeries: CourseSeries
        lateinit var semester: Semester
        lateinit var lecturers: List<String>
        lateinit var assistants: List<String>
        var attendance: Int? = null
        lateinit var enrollmentDeadline: LocalDate
        lateinit var website: URL

        fun build() = Course(
            courseSeries,
            semester,
            lecturers,
            assistants,
            attendance,
            enrollmentDeadline,
            website,
            entityLanguage
        )
    }
}
