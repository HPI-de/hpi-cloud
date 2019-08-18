package de.hpi.cloud.news

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.N1qlParams
import com.couchbase.client.java.query.N1qlQuery
import com.couchbase.client.java.query.Select.select
import com.couchbase.client.java.query.dsl.Expression.s
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.view.ViewQuery
import com.google.protobuf.UInt32Value
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.news.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

const val PORT_DEFAULT = 50050

fun main(args: Array<String>) {
    val service = Service("news", args.firstOrNull()?.toInt() ?: PORT_DEFAULT) { NewsServiceImpl(it) }
    service.blockUntilShutdown()
}

class NewsServiceImpl(val bucket: Bucket) : NewsServiceGrpc.NewsServiceImplBase() {
    companion object {
        const val DESIGN_ARTICLE = "article"
        const val DESIGN_SOURCE = "source"
        const val DESIGN_CATEGORY = "category"
        const val DESIGN_TAG = "tag"
    }

    // region Article
    override fun listArticles(
        request: ListArticlesRequest?,
        responseObserver: StreamObserver<ListArticlesResponse>?
    ) = unary(request, responseObserver, "listArticles") { req ->
        val sourceId = req.sourceId?.trim()?.takeIf { it.isNotEmpty() }
        if (!req.categoryId.isNullOrBlank()) TODO("Filtering by category_id is not yet supported")
        if (!req.tagId.isNullOrBlank()) TODO("Filtering by tag_id is not yet supported")

        val articles =
            if (sourceId.isNullOrEmpty())
                bucket.query(ViewQuery.from(DESIGN_ARTICLE, VIEW_BY_ID)).allRows()
                    .map { it.document().content() }
            else {
                val statement = select("*")
                    .from(bucket.name())
                    .where(
                        x(KEY_TYPE).eq(s("article"))
                            .and(n(KEY_VALUE, "sourceId").eq(s(sourceId)))
                    )
                    .orderBy(*descTimestamp(n(KEY_VALUE, "publishedAt")))
                bucket.query(N1qlQuery.simple(statement, N1qlParams.build().adhoc(false))).allRows()
                    .map { it.value().getObject(bucket.name()) }
            }

        ListArticlesResponse.newBuilder()
            .addAllArticles(articles.map { it.parseArticle() })
            .build()
    }

    override fun getArticle(request: GetArticleRequest?, responseObserver: StreamObserver<Article>?) =
        unary(request, responseObserver, "getArticle") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_ARTICLE, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseArticle()
                ?: Status.NOT_FOUND.throwException("Article with ID ${req.id} not found")
        }

    private fun JsonObject.parseArticle(): Article {
        val value = getObject(KEY_VALUE)
        return Article.newBuilder()
            .setId(getString(KEY_ID))
            .setSourceId(value.getString("sourceId"))
            .setLink(value.getI18nString("link"))
            .setTitle(value.getI18nString("title"))
            .setPublishDate(value.getTimestamp("publishedAt"))
            .addAllAuthorIds(value.getStringArray("authorIds").filterNotNull())
            .setCover(value.getImage("cover"))
            .setTeaser(value.getI18nString("teaser"))
            .setContent(value.getI18nString("content"))
            .addAllCategories(value.getStringArray("categories").filterNotNull().mapNotNull { getCategory(it) })
            .addAllTags(value.getStringArray("tags").filterNotNull().mapNotNull { getTag(it) })
            .apply {
                value.getInt("viewCount")?.let {
                    viewCount = UInt32Value.of(it)
                }
            }
            .build()
    }
    // endregion

    // region Source
    override fun listSources(request: ListSourcesRequest?, responseObserver: StreamObserver<ListSourcesResponse>?) =
        unary(request, responseObserver, "listSources") { req ->
            val sources = bucket.query(ViewQuery.from(DESIGN_SOURCE, VIEW_BY_ID)).allRows()
                .map { it.document().content().parseSource() }
            ListSourcesResponse.newBuilder()
                .addAllSources(sources)
                .build()
        }

    override fun getSource(request: GetSourceRequest?, responseObserver: StreamObserver<Source>?) =
        unary(request, responseObserver, "getSource") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_SOURCE, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseSource()
                ?: Status.NOT_FOUND.throwException("Source with ID ${req.id} not found")
        }

    private fun JsonObject.parseSource(): Source? {
        val value = getObject(KEY_VALUE)
        return Source.newBuilder()
            .setId(getString(KEY_ID))
            .setTitle(value.getI18nString("title"))
            .setLink(value.getI18nString("link"))
            .build()
    }
    // endregion

    // region Category
    override fun listCategories(
        request: ListCategoriesRequest?,
        responseObserver: StreamObserver<ListCategoriesResponse>?
    ) = unary(request, responseObserver, "listCategories") { req ->
        val categories = bucket.query(ViewQuery.from(DESIGN_CATEGORY, VIEW_BY_ID)).allRows()
            .map { it.document().content().parseCategory() }
        ListCategoriesResponse.newBuilder()
            .addAllCategories(categories)
            .build()
    }

    override fun getCategory(request: GetCategoryRequest?, responseObserver: StreamObserver<Category>?) =
        unary(request, responseObserver, "getCategory") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Category ID is required")

            getCategory(req.id)
                ?: Status.NOT_FOUND.throwException("Category with ID ${req.id} not found")
        }

    private fun getCategory(id: String): Category? {
        return bucket.get(DESIGN_CATEGORY, VIEW_BY_ID, id)
            ?.document()?.content()?.parseCategory()
    }

    private fun JsonObject.parseCategory(): Category? {
        val value = getObject(KEY_VALUE)
        return Category.newBuilder()
            .setId(getString(KEY_ID))
            .setTitle(value.getI18nString("title"))
            .build()
    }
    // endregion

    // region Tag
    override fun listTags(request: ListTagsRequest?, responseObserver: StreamObserver<ListTagsResponse>?) =
        unary(request, responseObserver, "listTags") { req ->
            val tags = bucket.query(ViewQuery.from(DESIGN_TAG, VIEW_BY_ID)).allRows()
                .map { it.document().content().parseTag() }
            ListTagsResponse.newBuilder()
                .addAllTags(tags)
                .build()
        }

    override fun getTag(request: GetTagRequest?, responseObserver: StreamObserver<Tag>?) =
        unary(request, responseObserver, "getTag") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            getTag(req.id)
                ?: Status.NOT_FOUND.throwException("Tag with ID ${req.id} not found")
        }

    private fun getTag(id: String): Tag? {
        return bucket.get(DESIGN_TAG, VIEW_BY_ID, id)
            ?.document()?.content()?.parseTag()
    }

    private fun JsonObject.parseTag(): Tag? {
        val value = getObject(KEY_VALUE)
        return Tag.newBuilder()
            .setId(getString(KEY_ID))
            .setTitle(value.getI18nString("title"))
            .setArticleCount(value.getInt("articleCount") ?: 0)
            .build()
    }
    // endregion
}
