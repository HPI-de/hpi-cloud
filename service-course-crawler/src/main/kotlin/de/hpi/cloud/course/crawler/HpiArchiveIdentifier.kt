package de.hpi.cloud.course.crawler

data class HpiArchiveIdentifier(
    val spd: StudyPathDegree,
    val sem: Semester
) {
    override fun toString() = "$spd $sem"
}
