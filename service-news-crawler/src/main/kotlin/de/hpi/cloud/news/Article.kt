package de.hpi.cloud.news

import com.couchbase.client.java.document.JsonDocument
import com.couchbase.client.java.document.json.JsonObject
import java.net.URL
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

data class Article(
    val preview: ArticlePreview,
    val content: String,
    val cover: ArticleCover?
) {

    companion object {
        private const val ID_MAX_LENGTH = 64
        private const val TYPE = "article"
        private const val VERSION = 1
    }

    val id get() = preview.id.take(ID_MAX_LENGTH)
    val documentId = "${TYPE}_${id}"

    data class ArticleCover(
        val alt: String,
        val sources: Map<String, URL>
    ) {

        fun asMap(article: Article) = mapOf<String, Any>(
            "source" to sources.map { it.key to it.value.toString() }.toMap(),
            "alt" to article.i18n(alt, article.preview.language)
        )
    }

    fun toJsonObject() = JsonObject.from(
        mapOf(
            "type" to TYPE,
            "version" to VERSION,
            "id" to id,
            "fetchedAt" to timestamp(),
            "value" to mapOf(
                "title" to i18n(preview.title, preview.language),
                "link" to i18n(preview.url.toString(), preview.language),
                "sourceId" to preview.source.id,
                "publishedAt" to timestamp(preview.date),
                "cover" to cover?.asMap(this),
                "teaser" to i18n(preview.teaser, preview.language),
                "content" to i18n(content, preview.language)
            )
        )
    )

    fun toJsonDocument() = JsonDocument.create(documentId, toJsonObject())

    private fun <T> i18n(value: T, language: String = "de"): Map<String, T> {
        return mapOf(
            language.toLowerCase() to value
        )
    }

    private fun timestamp(date: LocalDate) = timestamp(days = date.toEpochDay())
    private fun timestamp(days: Long) = timestamp(millis = days * 24 * 60 * 60 * 1000)
    private fun timestamp(millis: Long = System.currentTimeMillis(), nanos: Int = 0): Map<String, Number> {
        return mapOf(
            "millis" to millis,
            "nanos" to nanos
        )
    }
}
