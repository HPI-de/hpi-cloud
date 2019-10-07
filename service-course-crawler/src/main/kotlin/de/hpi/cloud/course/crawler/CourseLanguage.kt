package de.hpi.cloud.course.crawler

enum class CourseLanguage(
    val title: String
) {
    DE("Deutsch"),
    EN("Englisch");

    companion object {
        fun parse(string: String) = values().first { cl -> cl.title.equals(string, ignoreCase = true) }
    }
}
