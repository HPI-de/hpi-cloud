package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.utils.splitAsPair

data class StudyPathDegree(
    val studyPathName: String,
    val degree: Degree
) {
    override fun toString(): String = "${studyPathName}-${degree.abbr}"

    companion object {
        fun parse(string: String) = string.splitAsPair(" ", fromEnd = true)
            .let { (spn, deg) -> StudyPathDegree(spn, Degree.parse(deg)) }
    }
}
