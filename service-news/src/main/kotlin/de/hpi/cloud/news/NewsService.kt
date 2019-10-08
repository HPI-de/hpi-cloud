package de.hpi.cloud.news

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.view.ViewQuery
import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.UInt32Value
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.getI18nString
import de.hpi.cloud.common.utils.grpc.*
import de.hpi.cloud.common.utils.protobuf.getImage
import de.hpi.cloud.common.utils.protobuf.getTimestamp
import de.hpi.cloud.news.v1test.*
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
            .paginate(bucket, req.pageSize, req.pageToken) { it.parseArticle(req) }

        ListArticlesResponse.newBuilder().buildWith {
            addAllArticles(articles)
            nextPageToken = newToken
        }
    }

    override fun getArticle(request: GetArticleRequest?, responseObserver: StreamObserver<Article>?) =
        unary(request, responseObserver, "getArticle") { req ->
            checkArgRequired(req.id, "id")

            bucket.getContent(DESIGN_ARTICLE, VIEW_BY_ID, req.id)?.parseArticle(req)
                ?: notFound<Article>(req.id)
        }

    private fun JsonObject.parseArticle(request: GeneratedMessageV3) =
        Article.newBuilder().buildWithDocument<Article, Article.Builder>(this) {
            id = getString(KEY_ID)
            sourceId = it.getString("sourceId")
            link = it.getI18nString("link", request)
            title = it.getI18nString("title", request)
            publishDate = it.getTimestamp("publishedAt")
            addAllAuthorIds(it.getStringArray("authorIds").filterNotNull())
            it.getImage("cover", request)?.let { i -> cover = i }
            teaser = it.getI18nString("teaser", request)
            content = it.getI18nString("content", request)
            addAllCategories(it.getStringArray("categories").filterNotNull()
                .mapNotNull { c -> getCategory(c, request) })
            addAllTags(it.getStringArray("tags").filterNotNull().mapNotNull { t -> getTag(t, request) })
            it.getInt("viewCount")?.let { c -> viewCount = UInt32Value.of(c) }
        }
    // endregion

    // region Source
    override fun listSources(request: ListSourcesRequest?, responseObserver: StreamObserver<ListSourcesResponse>?) =
        unary(request, responseObserver, "listSources") { req ->
            val (sources, newToken) = ViewQuery.from(DESIGN_SOURCE, VIEW_BY_ID)
                .paginate(bucket, req.pageSize, req.pageToken) { it.parseSource(req) }

            ListSourcesResponse.newBuilder().buildWith {
                addAllSources(sources)
                nextPageToken = newToken
            }
        }

    override fun getSource(request: GetSourceRequest?, responseObserver: StreamObserver<Source>?) =
        unary(request, responseObserver, "getSource") { req ->
            checkArgRequired(req.id, "id")

            bucket.getContent(DESIGN_SOURCE, VIEW_BY_ID, req.id)?.parseSource(req)
                ?: notFound<Source>(req.id)
        }

    private fun JsonObject.parseSource(request: GeneratedMessageV3) =
        Source.newBuilder().buildWithDocument<Source, Source.Builder>(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
            link = it.getI18nString("link", request)
        }
    // endregion

    // region Category
    override fun listCategories(
        request: ListCategoriesRequest?,
        responseObserver: StreamObserver<ListCategoriesResponse>?
    ) = unary(request, responseObserver, "listCategories") { req ->
        val (categories, newToken) = ViewQuery.from(DESIGN_CATEGORY, VIEW_BY_ID)
            .paginate(bucket, req.pageSize, req.pageToken) { it.parseCategory(req) }

        ListCategoriesResponse.newBuilder().buildWith {
            addAllCategories(categories)
            nextPageToken = newToken
        }
    }

    override fun getCategory(request: GetCategoryRequest?, responseObserver: StreamObserver<Category>?) =
        unary(request, responseObserver, "getCategory") { req ->
            checkArgRequired(req.id, "id")

            getCategory(req.id, req)
                ?: notFound<Category>(req.id)
        }

    private fun getCategory(id: String, request: GeneratedMessageV3): Category? {
        return bucket.getContent(DESIGN_CATEGORY, VIEW_BY_ID, id)?.parseCategory(request)
    }

    private fun JsonObject.parseCategory(request: GeneratedMessageV3) =
        Category.newBuilder().buildWithDocument<Category, Category.Builder>(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
        }
    // endregion

    // region Tag
    override fun listTags(request: ListTagsRequest?, responseObserver: StreamObserver<ListTagsResponse>?) =
        unary(request, responseObserver, "listTags") { req ->
            val (tags, newToken) = ViewQuery.from(DESIGN_TAG, VIEW_BY_ID)
                .paginate(bucket, req.pageSize, req.pageToken) { it.parseTag(req) }

            ListTagsResponse.newBuilder().buildWith {
                addAllTags(tags)
                nextPageToken = newToken
            }
        }

    override fun getTag(request: GetTagRequest?, responseObserver: StreamObserver<Tag>?) =
        unary(request, responseObserver, "getTag") { req ->
            checkArgRequired(req.id, "id")

            getTag(req.id, req)
                ?: notFound<Tag>(req.id)
        }

    private fun getTag(id: String, request: GeneratedMessageV3): Tag? {
        return bucket.getContent(DESIGN_TAG, VIEW_BY_ID, id)?.parseTag(request)
    }

    private fun JsonObject.parseTag(request: GeneratedMessageV3) =
        Tag.newBuilder().buildWithDocument<Tag, Tag.Builder>(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
            articleCount = it.getInt("articleCount") ?: 0
        }
    // endregion
}
