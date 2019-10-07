package de.hpi.cloud.course.crawler

class HpiCourseListCrawler(
    val csi: HpiArchiveIdentifier
) {
    constructor(studyPathName: String, degree: Degree, sem: Semester = CURRENT_SEMESTER)
            : this(HpiArchiveIdentifier(StudyPathDegree(studyPathName, degree), sem))

    val courseListBaseURI = HPI_BASE_URI.resolve("studium/lehrveranstaltungen/")

    val studyPathId = csi.spd.studyPathName.replace(' ', '-').toLowerCase()
    val queryURL = courseListBaseURI.resolve("$studyPathId-${csi.spd.degree.abbr.toLowerCase()}/").toURL()

    fun listCourses(): Sequence<HpiCourseDetailPageCrawler> {
        val doc = createDocumentQuery(queryURL).get()

        val links = doc.select("table.contenttable tr a.courselink")
        return links.asSequence()
            .map { el -> el.attr("href") }
            .map { HpiCourseDetailPageCrawler(HPI_BASE_URI.resolve(it).toURL()) }
    }

    override fun toString() = csi.toString()
}
