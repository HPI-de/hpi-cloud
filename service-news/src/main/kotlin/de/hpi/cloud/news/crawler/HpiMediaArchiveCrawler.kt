package de.hpi.cloud.news.crawler

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.news.entities.Source
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

class HpiMediaArchiveCrawler : ArticleCrawler() {
    enum class HpiMediaSource(val id: Id<Source>) {
        NEWS("hpi-news"),
        PRESS("hpi-press");

        constructor(id: String) : this(Id(id))
    }

    companion object {
        private val SITE_URL = URL("https://hpi.de/medien/presseinformationen/archiv.html")

        private val AJAX_REQUEST_URL_REGEX = Pattern.compile("ajaxRequestUrl ?= ?['\"](.*?)['\"];")
    }

    override val baseUri: URI
    override val serviceName = "hpi-archive"
    override val crawlerVersion = "1.0.0"
    val archive: HpiMediaArchive
    val resolver: HpiMediaArticleResolver

    init {
        val doc = createDocumentQuery(SITE_URL).get()

        baseUri = URI(doc.baseUri())

        val listForm = doc.selectFirst("form.tx_dscoverview_filterForm")
        val archivePath = doc.getElementById("content")
            .select("script")
            .map { AJAX_REQUEST_URL_REGEX.matcher(it.data()) }
            .filter { it.find() }
            .map { it.group(1) }
            .first()

        val params = mutableMapOf<String, String>()
        listForm.select("input").forEach { input ->
            params[input.attr("name")] = input.attr("value")
        }

        archive = HpiMediaArchive(
            this,
            queryUrl = baseUri.resolve(archivePath).toURL(),
            queryParams = params.toMap()
        )
        resolver = HpiMediaArticleResolver(this)
    }

    override fun query(indexOffset: Int) = archive
        .query(indexOffset)
        .map { resolver.resolvePreview(it) }
}
