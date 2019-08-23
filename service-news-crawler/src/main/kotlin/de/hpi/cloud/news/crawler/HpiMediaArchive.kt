package de.hpi.cloud.news.crawler

import de.hpi.cloud.news.ArticleCrawler
import de.hpi.cloud.news.ArticlePreview
import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import java.net.URL
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class HpiMediaArchive(
    val crawler: ArticleCrawler,
    val queryUrl: URL,
    val queryParams: Map<String, String>
) {

    companion object {
        private const val ARCHIVE_QUERY_PARAMETER_LIST_OFFSET = "tx_dscoverview_list[limitItemsOffset]"
        private const val ARCHIVE_QUERY_PARAMETER_LIST_LIMIT = "tx_dscoverview_list[limitItemsLength]"

        private val GERMAN_DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy")

        private const val NO_COVER_SOURCE = "/fileadmin/_processed_/3/d/csm_hpi_logo_srgb_wb_sl1_web80_e8008335bd.png"
    }

    fun query(indexOffset: Int = 0, limit: Int = 10) = sequence {
        var page = indexOffset / limit
        val offsetInPage = indexOffset % limit

        var currentPreviews = queryArchive(page, limit)
            .drop(offsetInPage)
            .map { extractArticlePreview(it) }
        var lastId: String
        do {
            yieldAll(currentPreviews)
            lastId = currentPreviews.last().id
            currentPreviews = queryArchive(++page, limit)
                .map { extractArticlePreview(it) }
        } while (currentPreviews.isNotEmpty() && currentPreviews.all { it.id != lastId })
    }

    private fun queryArchive(page: Int, pageSize: Int): Elements {
        val params = queryParams.toMutableMap()
        params[ARCHIVE_QUERY_PARAMETER_LIST_LIMIT] = pageSize.toString()
        params[ARCHIVE_QUERY_PARAMETER_LIST_OFFSET] = page.toString()
        return crawler.createDocumentQuery(queryUrl)
            .data(params)
            .post()
            .selectFirst("div.tx_dscoverview_items")
            .children()
    }

    private fun extractArticlePreview(element: Element): ArticlePreview {
        val articleLinkWithCover = element.selectFirst("a")
        val url = crawler.baseUri.resolve(articleLinkWithCover.attr("href")).toURL()
        val hasCover = articleLinkWithCover.selectFirst("img").attr("src") != NO_COVER_SOURCE

        val dateString = element.selectFirst("p.date")!!.text().trim()
        val date = LocalDate.parse(dateString, GERMAN_DATE_FORMAT)

        val title = element.selectFirst("h1")!!.text().trim()
        val teaser = element.selectFirst("p:not(.date)")!!.ownText().trim()

        return ArticlePreview(
            url = url,
            date = date,
            title = title,
            teaser = teaser,
            hasCover = hasCover,
            language = "de"
        )
    }
}
