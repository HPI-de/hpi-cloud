package de.hpi.cloud.news

import de.hpi.cloud.news.crawler.HpiMediaArchiveCrawler.HpiMediaSource
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.regex.Pattern

data class ArticlePreview(
    val url: URL,
    val date: LocalDate,
    val title: String,
    val teaser: String,
    val hasCover: Boolean,
    val language: String
) {

    companion object {
        private val URL_SOURCE_REGEX = Pattern.compile("hpi\\.de/(.*?)/")
        private val ID_UNUSABLE_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9_\\-\\+]")
        private val SORTABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")

        private val SOURCE_MAPPING = mapOf(
            "pressemitteilungen" to HpiMediaSource.PRESS,
            "news" to HpiMediaSource.NEWS
        )
    }

    val id: String
    val source: HpiMediaSource

    init {
        val matcher = URL_SOURCE_REGEX.matcher(url.toString())
        if (!matcher.find()) error("Source not in URL")
        source = SOURCE_MAPPING[matcher.group(1)] ?: error("Unknown source from URL")

        val dateString = date.format(SORTABLE_DATE_FORMAT)
        val titleString = title
            .replace(' ', '-')
            .replace(ID_UNUSABLE_CHARS_REGEX.toRegex(), "")
            .replace(Regex("-+"), "-")
            .toLowerCase()
        id = "${source.id}_${dateString}_${titleString}"

    }

    override fun toString(): String {
        return "ArticlePreview[\"${id}\" from ${date} - ${url}]"
    }
}
