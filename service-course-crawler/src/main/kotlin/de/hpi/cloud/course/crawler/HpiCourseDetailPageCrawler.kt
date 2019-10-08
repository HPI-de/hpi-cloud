package de.hpi.cloud.course.crawler

import de.hpi.cloud.common.utils.groupingSections
import de.hpi.cloud.common.utils.splitAsPair
import de.hpi.cloud.common.utils.thenTake
import de.hpi.cloud.common.utils.trim
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.util.*

class HpiCourseDetailPageCrawler(
    val url: URL
) {

    companion object {
        val TITLE_REGEX = Regex("^(.*) \\((\\w+?) (.*?)\\)\$")
        val LECTURER_ASSISTANTS_REGEX = Regex(
            "</h1>(.*?)<p>",
            setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.MULTILINE)
        )
        val SIMPLE_GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        val LONG_GERMAN_DATE_FORMAT = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.GERMAN)
    }

    private enum class RelevantSection(
        val queryValues: List<String>
    ) {
        GENERAL_INFORMATION("Allgemeine Information"),
        PROGRAMS("Studiengänge & Module", "Studiengänge"),
        DESCRIPTION("Beschreibung"),
        REQUIREMENTS("Voraussetzungen"),
        LITERATURE("Literatur"),
        EXAMINATION("Leistungserfassung"),
        DATES("Termine"),
        LEARNING("Lern- und Lehrformen");

        constructor(vararg queryValues: String) : this(queryValues.asList())

        companion object {
            val REVERSE_MAP = values().flatMap { section ->
                section.queryValues.map { it to section }
            }.toMap()

            fun parse(h1name: String) = REVERSE_MAP[h1name]

            fun isValid(string: String) = parse(string) != null
        }
    }

    fun query() = read(createDocumentQuery(url).get())

    fun read(doc: Document): CourseDetail {
        println(url)
        val container = doc.selectFirst(".tx-ciuniversity-course")
        val containerHtml = container.html()

        return CourseDetail.build {
            course = Course.build {
                courseSeries = CourseSeries.build {
                    website = url

                    val titleMatch = TITLE_REGEX.matchEntire(container.selectFirst("h1").text().trim())!!
                    title = titleMatch.groups[1]!!.value

                    // add shortTitle and abbreviation manually
                    shortTitle = title
                    abbreviation = ""

                    semester = Semester.parse(
                        titleMatch.groups[3]!!.value,
                        titleMatch.groups[2]!!.value
                    )

                    val laa = readLecturerAndAssistants(containerHtml)
                    lecturers = laa.lecturers
                    assistants = laa.assistants

                    container.children()
                        .groupingSections(true) { node ->
                            node.`is`("h2").thenTake {
                                node.text().trim().takeIf {
                                    RelevantSection.isValid(it)
                                }
                            }
                        }
                        .mapValues { (_, elements) -> Elements(elements) }
                        .forEach { (key, elements) ->
                            //                            println(key + " " + "-".repeat(64 - key.length))
                            when (RelevantSection.parse(key)) {
                                RelevantSection.GENERAL_INFORMATION -> {
                                    elements
                                        .select("ul.tx-ciuniversity-course-general-info > li")
                                        .map { it.text().splitAsPair(":").trim() }
                                        .forEach { (key, value) ->
                                            when (key.toLowerCase()) {
                                                "semesterwochenstunden" -> hoursPerWeek = value.toInt()
                                                "ects" -> ects = value.toInt()
                                                "benotet" -> {
                                                } // ignored
                                                "einschreibefrist" -> enrollmentDeadline = parseDeadline(value)
                                                "maximale teilnehmerzahl" -> attendance = value.toInt()
                                                "lehrform" -> types = CourseSeries.Type.parse(value)
                                                "belegungsart" -> compulsory = CourseSeries.Compulsory.parse(value)
                                                "lehrsprache" -> CourseLanguage.parse(value).apply {
                                                    courseLanguage = this
                                                    courseEntityLanguage = name
                                                    courseDetailEntityLanguage = name
                                                    courseSeriesEntityLanguage = name
                                                }
                                                else -> println("Unknown entry \"$key\"=\"$value\"")
                                            }
                                        }
                                }
                                RelevantSection.DESCRIPTION -> {
                                    description = elements.outerHtml()
                                }
                                RelevantSection.PROGRAMS -> {
                                    programs = elements
                                        .select(".tx_dscclipclap")
                                        .map { readStudyPathModules(it) }
                                        .toMap()
                                }
                                RelevantSection.REQUIREMENTS -> {
                                    requirements = elements.outerHtml()
                                }
                                RelevantSection.LEARNING -> {
                                    learning = elements.outerHtml()
                                }
                                RelevantSection.EXAMINATION -> {
                                    examination = elements.outerHtml()
                                }
                                RelevantSection.DATES -> {
                                    dates = elements.outerHtml()
                                }
                                RelevantSection.LITERATURE -> {
                                    literature = elements.outerHtml()
                                }
                                else -> {
                                    elements.forEach { println(it.text()) }
                                }
                            }
                        }
                }
            }
        }
    }

    private fun parseDeadline(string: String): LocalDate? {
        if (string.isBlank())
            return null
        try {
            return LocalDate.parse(
                string
                    .replaceBefore("-", "")
                    .trim('-', ' '),
                SIMPLE_GERMAN_DATE_FORMAT
            )
        } catch (ex: DateTimeParseException) {
            try {
                return LocalDate.parse(
                    string.split(" ")
                        .subList(0, 3)
                        .joinToString(separator = " "),
                    LONG_GERMAN_DATE_FORMAT
                )
            } catch (ex: IndexOutOfBoundsException) {
                // error handling below
            } catch (ex: DateTimeParseException) {
                // error handling below
            }
        }
        println("Could not parse enrollment deadline \"$string\". Please check manually.")
        return null
    }

    private fun readStudyPathModules(element: Element): Pair<StudyPathDegree, Set<String>> {
        val studyPathDegree = StudyPathDegree.parse(element.selectFirst(".tx_dscclipclap_header").text())
        val modules = element.selectFirst(".tx_dscclipclap_content")
            .children()
            .map { it.text() }
            .toSet()
        return studyPathDegree to modules
    }

    private fun readLecturerAndAssistants(containerHtml: String): LecturerAndAssistants = LECTURER_ASSISTANTS_REGEX
        .find(containerHtml)!!
        .groups[1]!!
        .value.trim()
        .let { croppedHtml ->
            LecturerAndAssistants.parse(croppedHtml)
        }

    private data class LecturerAndAssistants(
        val lecturers: List<String>,
        val assistants: List<String> = listOf()
    ) {
        companion object {
            fun parse(string: String): LecturerAndAssistants = string.run {
                indexOf("Tutoren:").let { i ->
                    if (i == -1) {
                        LecturerAndAssistants(
                            readLecturers(this)
                        )
                    } else {
                        LecturerAndAssistants(
                            readLecturers(this.substring(0, i)),
                            readAssistants(this.substring(i))
                        )
                    }
                }
            }

            private fun readLecturers(htmlSnippet: String) = htmlSnippet
                .replaceAfter("<br>", "") // Hacky hack to counter "Website zum Kurs:" and others
                .let { Jsoup.parse(it, HPI_BASE_URI.toString()) }
                .select("i")
                .map { it.selectFirst("a") }
                .map { it.text() }
                .filter { !it.startsWith("http", ignoreCase = true) }

            private fun readAssistants(htmlSnippet: String) = htmlSnippet
                .let { Jsoup.parse(it, HPI_BASE_URI.toString()) }
                .select("i > a")
                .map { it.text() }
                .filter { !it.startsWith("http", ignoreCase = true) }
        }
    }
}
