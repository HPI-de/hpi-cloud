package de.hpi.cloud.news

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.couchbase.VIEW_BY_ID
import de.hpi.cloud.common.couchbase.get
import de.hpi.cloud.common.couchbase.paginate
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.grpc.checkArgRequired
import de.hpi.cloud.common.grpc.notFound
import de.hpi.cloud.common.grpc.unary
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.news.entities.*
import de.hpi.cloud.news.entities.Article
import de.hpi.cloud.news.entities.Category
import de.hpi.cloud.news.entities.Source
import de.hpi.cloud.news.entities.Tag
import de.hpi.cloud.news.v1test.*
import io.grpc.stub.StreamObserver
import de.hpi.cloud.news.v1test.Article as ProtoArticle
import de.hpi.cloud.news.v1test.Category as ProtoCategory
import de.hpi.cloud.news.v1test.Source as ProtoSource
import de.hpi.cloud.news.v1test.Tag as ProtoTag

fun main(args: Array<String>) {
    val service = Service("news", args.firstOrNull()?.toInt()) { NewsServiceImpl(it) }
    service.blockUntilShutdown()
}

class NewsServiceImpl(private val bucket: Bucket) : NewsServiceGrpc.NewsServiceImplBase() {
    companion object {
        const val DESIGN_ARTICLE = "article"
        const val DESIGN_CATEGORY = "category"
        const val DESIGN_TAG = "tag"
    }

    // region Article
    override fun listArticles(
        request: ListArticlesRequest?,
        responseObserver: StreamObserver<ListArticlesResponse>?
    ) = unary(request, responseObserver, "listArticles") { req ->
        val (articles, newToken) = ViewQuery.from(DESIGN_ARTICLE, VIEW_BY_ID)
            .paginate<Article>(bucket, req.pageSize, req.pageToken)

        ListArticlesResponse.newBuilder().build {
            addAllArticles(articles.map { it.toProto(this@unary) })
            nextPageToken = newToken
        }
    }

    override fun getArticle(request: GetArticleRequest?, responseObserver: StreamObserver<ProtoArticle>?) =
        unary(request, responseObserver, "getArticle") { req ->
            checkArgRequired(req.id, "id")

            bucket.get<Article>(Id(req.id))?.toProto(this)
                ?: notFound<ProtoArticle>(req.id)
        }
    // endregion

    // region Source
    override fun listSources(request: ListSourcesRequest?, responseObserver: StreamObserver<ListSourcesResponse>?) =
        unary(request, responseObserver, "listSources") { req ->
            val (sources, newToken) = ViewQuery.from(Source.type, VIEW_BY_ID)
                .paginate<Source>(bucket, req.pageSize, req.pageToken)

            ListSourcesResponse.newBuilder().build {
                addAllSources(sources.map { it.toProto(this@unary) })
                nextPageToken = newToken
            }
        }

    override fun getSource(request: GetSourceRequest?, responseObserver: StreamObserver<ProtoSource>?) =
        unary(request, responseObserver, "getSource") { req ->
            checkArgRequired(req.id, "id")

            bucket.get<Source>(Id(req.id))?.toProto(this)
                ?: notFound<ProtoSource>(req.id)
        }
    // endregion

    // region Category
    override fun listCategories(
        request: ListCategoriesRequest?,
        responseObserver: StreamObserver<ListCategoriesResponse>?
    ) = unary(request, responseObserver, "listCategories") { req ->
        val (categories, newToken) = ViewQuery.from(DESIGN_CATEGORY, VIEW_BY_ID)
            .paginate<Category>(bucket, req.pageSize, req.pageToken)

        ListCategoriesResponse.newBuilder().build {
            addAllCategories(categories.map { it.toProto(this@unary) })
            nextPageToken = newToken
        }
    }

    override fun getCategory(request: GetCategoryRequest?, responseObserver: StreamObserver<ProtoCategory>?) =
        unary(request, responseObserver, "getCategory") { req ->
            checkArgRequired(req.id, "id")

            bucket.get<Category>(Id(req.id))?.toProto(this)
                ?: notFound<ProtoCategory>(req.id)
        }
    // endregion

    // region Tag
    override fun listTags(request: ListTagsRequest?, responseObserver: StreamObserver<ListTagsResponse>?) =
        unary(request, responseObserver, "listTags") { req ->
            val (tags, newToken) = ViewQuery.from(DESIGN_TAG, VIEW_BY_ID)
                .paginate<Tag>(bucket, req.pageSize, req.pageToken)

            ListTagsResponse.newBuilder().build {
                addAllTags(tags.map { it.toProto(this@unary) })
                nextPageToken = newToken
            }
        }

    override fun getTag(request: GetTagRequest?, responseObserver: StreamObserver<ProtoTag>?) =
        unary(request, responseObserver, "getTag") { req ->
            checkArgRequired(req.id, "id")

            bucket.get<Tag>(Id(req.id))?.toProto(this)
                ?: notFound<ProtoTag>(req.id)
        }
    // endregion
}
