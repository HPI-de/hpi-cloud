package de.hpi.cloud.news.crawler

import de.hpi.cloud.news.ArticleCrawler
import java.net.URI
import java.net.URL
import java.util.regex.Pattern

class HpiMediaArchiveCrawler() : ArticleCrawler() {

    enum class HpiMediaSource(val id: String) {
        NEWS("hpi-news"),
        PRESS("hpi-press")
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

//        val baseURL = doc.selectFirst("head > base").attr("href")
        baseUri = URI(doc.baseUri())

        val listForm = doc.selectFirst("form.tx_dscoverview_filterForm")
        val archivePath = doc.getElementById("content")
            .select("script")
            .map { AJAX_REQUEST_URL_REGEX.matcher(it.data()) }
            .filter { it.find() }
            .map { it.group(1) }
            .first()

        val params = mutableMapOf<String, String>()
        for (input in listForm.select("input")) {
            params[input.attr("name")] = input.attr("value")
        }

        archive = HpiMediaArchive(
            crawler = this,
            queryUrl = baseUri.resolve(archivePath).toURL(),
            queryParams = params.toMap()
        )
        resolver = HpiMediaArticleResolver(
            crawler = this
        )
    }

    override fun query(indexOffset: Int) = archive
        .query(indexOffset)
        .map { resolver.resolvePreview(it) }
}
