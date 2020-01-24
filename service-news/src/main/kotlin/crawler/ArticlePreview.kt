package crawler

import crawler.HpiMediaArchiveCrawler.HpiMediaSource
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.news.entities.Article
import java.net.URL
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.regex.Pattern

data class ArticlePreview(
    val url: URL,
    val publishedAt: Instant,
    val title: String,
    val teaser: String,
    val hasCover: Boolean,
    val locale: Locale,
    val zoneId: ZoneId
) {

    companion object {
        private val URL_SOURCE_REGEX = Pattern.compile("hpi\\.de/(.*?)/")
        private val ID_UNUSABLE_CHARS_REGEX = Pattern.compile("[^a-zA-Z0-9_\\-+]")
        private val SORTABLE_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd")

        private val SOURCE_MAPPING = mapOf(
            "pressemitteilungen" to HpiMediaSource.PRESS,
            "news" to HpiMediaSource.NEWS
        )
    }

    val id: Id<Article>
    val source: HpiMediaSource

    init {
        val matcher = URL_SOURCE_REGEX.matcher(url.toString())
        if (!matcher.find()) error("Source not in URL")
        source = SOURCE_MAPPING[matcher.group(1)] ?: error("Unknown source from URL")

        val dateString = SORTABLE_DATE_FORMAT.format(publishedAt.atZone(zoneId))
        val titleString = title
            .replace(' ', '-')
            .replace(ID_UNUSABLE_CHARS_REGEX.toRegex(), "")
            .replace(Regex("-+"), "-")
            .toLowerCase()
        id = Id.fromParts(source.id.value, dateString, titleString)

    }

    override fun toString(): String {
        return "ArticlePreview[\"$id\" from $publishedAt - ${url}]"
    }
}
