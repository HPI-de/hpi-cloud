package de.hpi.cloud.course.crawler

enum class Degree(
    val abbr: String
) {
    BACHELOR("BA"),
    MASTER("MA");

    companion object {
        fun parse(string: String) = values().firstOrNull { it.abbr.equals(string, ignoreCase = true) }
            ?: values().first { it.name.equals(string, ignoreCase = true) }
    }
}
