package de.hpi.cloud.news.crawler

import de.hpi.cloud.common.utils.protobuf.ImageSize
import de.hpi.cloud.common.utils.removeFirst
import de.hpi.cloud.news.crawler.HpiMediaArchiveCrawler.HpiMediaSource
import org.jsoup.nodes.Element

class HpiMediaArticleResolver(
    val crawler: ArticleCrawler
) {
    companion object {
        private val SOURCE_MAPPING = mapOf(
            "Pressemitteilung" to HpiMediaSource.PRESS,
            "News" to HpiMediaSource.NEWS
        )
    }

    fun resolvePreview(preview: ArticlePreview): Article {
        val doc = crawler.createDocumentQuery(preview.url).get()
        val contentElements = doc.selectFirst("div#content")
            .children()
            .drop(1)
            .flatMap { unboxContent(it) }
            .toMutableList()

        val sourceString = contentElements.removeFirst { it.`is`("h3") }!!.text().trim()
        val source = SOURCE_MAPPING[sourceString] ?: error("Unknown article source")
        if (source != preview.source) error("Mismatching sources detected")

        // date is known from preview
        contentElements.removeFirst { it.`is`("p.date") }
        // title is known from preview
        contentElements.removeFirst { it.`is`("h1") }

        val cover = if (preview.hasCover) {
            contentElements.removeFirst { it.`is`("figure") }?.let { figure ->
                val coverSrc = figure.selectFirst("img")?.attr("src") ?: error("Invalid cover image source")
                val coverAlt = figure.selectFirst("figcaption")?.text()
                    ?: figure.selectFirst("img")?.attr("alt")?.takeIf { it.isNotBlank() }

                Article.ArticleCover(
                    alt = coverAlt,
                    sources = mapOf(
                        ImageSize.ORIGINAL to crawler.baseUri.resolve(coverSrc).toURL()
                    )
                )
            }
        } else null

        val articleContent = contentElements.joinToString(separator = "\n") { it.toString().trim() }

        return Article(preview, articleContent, cover)
    }

    private fun unboxContent(element: Element): List<Element> {
        return when {
            !element.`is`("div") -> listOf(element)
            element.classNames().contains("alertbox") || element.classNames().contains("alert-info") ->
                listOf(createInfoBox(element))
            else -> element.children().flatMap { unboxContent(it) }
        }
    }

    private fun createInfoBox(boxContainer: Element): Element {
        val alertBox = Element("section")
        alertBox.addClass("infobox")
        val content = Element("div")
        content.appendChildren(boxContainer.children())
        alertBox.appendChildren(unboxContent(content))
        return alertBox
    }

    private fun Element.appendChildren(elements: List<Element>) {
        elements.forEach { this.appendChild(it) }
    }
}
