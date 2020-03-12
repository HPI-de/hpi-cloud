package de.hpi.cloud.news.crawler

import de.hpi.cloud.news.crawler.HpiMediaArchiveCrawler.HpiMediaSource
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.types.Image
import de.hpi.cloud.common.types.MarkupContent
import de.hpi.cloud.common.types.l10n
import de.hpi.cloud.common.utils.removeFirst
import de.hpi.cloud.news.entities.Article
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

    fun resolvePreview(preview: ArticlePreview): Pair<Id<Article>, Article> {
        fun <T : Any> T?.l10n() = l10n(preview.locale)

        val doc = crawler.createDocumentQuery(preview.url).get()
        val content = doc.selectFirst("div#content")
            .children()
            .drop(1)
            .toMutableList()

        val dateAndSourceSection = content.removeFirst {
            it.hasClass("news102")
        }

        val contentElements = content
            .flatMap { unboxContent(it) }
            .toMutableList()

        val sourceString = dateAndSourceSection?.let { section ->
            unboxContent(section)
                .find { it.`is`("h3") }
                ?.text()?.trim()
                ?: "News"
        }
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

                Image(
                    alt = coverAlt.l10n(),
                    source = mapOf(
                        Image.Size.ORIGINAL to crawler.baseUri.resolve(coverSrc).toURL()
                    )
                )
            }
        } else null

        val articleContent = contentElements.joinToString(separator = "\n") { it.toString().trim() }
        val id = preview.id.truncated()

        return id to Article(
            sourceId = preview.source.id,
            link = preview.url.l10n(),
            title = preview.title.l10n(),
            publishDate = preview.publishedAt,
            cover = cover,
            teaser = preview.teaser.l10n(),
            content = MarkupContent.html(articleContent).l10n()
        )
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
