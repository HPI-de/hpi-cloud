package de.hpi.cloud.news

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.view.ViewQuery
import com.google.protobuf.UInt32Value
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.buildWith
import de.hpi.cloud.common.utils.grpc.buildWithDocument
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.common.utils.protobuf.getImage
import de.hpi.cloud.common.utils.protobuf.getTimestamp
import de.hpi.cloud.news.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun main(args: Array<String>) {
    val service = Service("news", args.firstOrNull()?.toInt()) { NewsServiceImpl(it) }
    service.blockUntilShutdown()
}

class NewsServiceImpl(private val bucket: Bucket) : NewsServiceGrpc.NewsServiceImplBase() {
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
        val (articles, newToken) = ViewQuery.from(DESIGN_ARTICLE, VIEW_BY_ID)
            .paginate(bucket, req.pageSize, req.pageToken)

        ListArticlesResponse.newBuilder().buildWith {
            addAllArticles(articles.map { it.parseArticle() })
            newToken?.let { t -> nextPageToken = t }
        }
    }

    override fun getArticle(request: GetArticleRequest?, responseObserver: StreamObserver<Article>?) =
        unary(request, responseObserver, "getArticle") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_ARTICLE, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseArticle()
                ?: Status.NOT_FOUND.throwException("Article with ID ${req.id} not found")
        }

    private fun JsonObject.parseArticle(): Article? {
        return Article.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            sourceId = it.getString("sourceId")
            link = it.getI18nString("link")
            title = it.getI18nString("title")
            publishDate = it.getTimestamp("publishedAt")
            addAllAuthorIds(it.getStringArray("authorIds").filterNotNull())
            it.getImage("cover")?.let { i -> cover = i }
            teaser = it.getI18nString("teaser")
            content = it.getI18nString("content")
            addAllCategories(it.getStringArray("categories").filterNotNull().mapNotNull { c -> getCategory(c) })
            addAllTags(it.getStringArray("tags").filterNotNull().mapNotNull { t -> getTag(t) })
            it.getInt("viewCount")?.let { c -> viewCount = UInt32Value.of(c) }
        }
    }
    // endregion

    // region Source
    override fun listSources(request: ListSourcesRequest?, responseObserver: StreamObserver<ListSourcesResponse>?) =
        unary(request, responseObserver, "listSources") { _ ->
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
        return Source.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title")
            link = it.getI18nString("link")
        }
    }
    // endregion

    // region Category
    override fun listCategories(
        request: ListCategoriesRequest?,
        responseObserver: StreamObserver<ListCategoriesResponse>?
    ) = unary(request, responseObserver, "listCategories") { _ ->
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
        return Category.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title")
        }
    }
    // endregion

    // region Tag
    override fun listTags(request: ListTagsRequest?, responseObserver: StreamObserver<ListTagsResponse>?) =
        unary(request, responseObserver, "listTags") { _ ->
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
        return Tag.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title")
            articleCount = it.getInt("articleCount") ?: 0
        }
    }
    // endregion
}
