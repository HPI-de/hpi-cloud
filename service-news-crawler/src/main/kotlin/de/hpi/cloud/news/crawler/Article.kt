package de.hpi.cloud.news.crawler

import com.couchbase.client.java.document.json.JsonObject
import de.hpi.cloud.common.Entity
import de.hpi.cloud.common.utils.couchbase.i18nSingle
import de.hpi.cloud.common.utils.protobuf.ImageSize
import de.hpi.cloud.common.utils.protobuf.timestampNow
import de.hpi.cloud.common.utils.protobuf.toDbMap
import de.hpi.cloud.common.utils.protobuf.toTimestamp
import java.net.URL

data class Article(
    val preview: ArticlePreview,
    val content: String,
    val cover: ArticleCover?
) : Entity("article", 1) {
    companion object {
        private const val ID_MAX_LENGTH = 64
    }

    override val id get() = preview.id.take(ID_MAX_LENGTH)

    data class ArticleCover(
        val alt: String,
        val sources: Map<ImageSize, URL>
    ) {
        fun asMap(article: Article) = mapOf<String, Any>(
            "source" to sources.map { it.key to it.value.toString() }.toMap(),
            "alt" to i18nSingle(alt, article.preview.language)
        )
    }

    override fun toJsonObject(): JsonObject = JsonObject.from(
        mapOf(
            "type" to type,
            "version" to version,
            "id" to id,
            "fetchedAt" to timestampNow(),
            "value" to mapOf(
                "title" to i18nSingle(preview.title, preview.language),
                "link" to i18nSingle(preview.url.toString(), preview.language),
                "sourceId" to preview.source.id,
                "publishedAt" to preview.publishedAt.toTimestamp().toDbMap(),
                "cover" to cover?.asMap(this),
                "teaser" to i18nSingle(preview.teaser, preview.language),
                "content" to i18nSingle(content, preview.language)
            )
        )
    )
}
