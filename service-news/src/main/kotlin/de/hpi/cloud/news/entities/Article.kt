package de.hpi.cloud.news.entities

import com.google.protobuf.UInt32Value
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.entity.parse
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.serializers.UriSerializer
import de.hpi.cloud.common.types.*
import de.hpi.cloud.common.utils.parseUri
import kotlinx.serialization.Serializable
import java.net.URI
import de.hpi.cloud.news.v1test.Article as ProtoArticle

@Serializable
data class Article(
    val sourceId: Id<Source>,
    val link: L10n<@Serializable(UriSerializer::class) URI>,
    val title: L10n<String>,
    val publishDate: LocalDateTime,
    val authorIds: Set<String> = emptySet(),
    val cover: Image? = null,
    val teaser: L10n<String>,
    val content: L10n<MarkupContent>,
    val categories: Set<Id<Category>> = emptySet(),
    val tags: Set<Id<Tag>> = emptySet(),
    val viewCount: Int? = null
) : Entity<Article>() {
    companion object : Entity.Companion<Article>("article")

    object ProtoSerializer : Entity.ProtoSerializer<Article, ProtoArticle, ProtoArticle.Builder>() {
        override fun fromProto(proto: ProtoArticle, context: Context): Article = Article(
            sourceId = Id(proto.id),
            link = L10n.single(context, proto.link.parseUri()),
            title = L10n.single(context, proto.title),
            publishDate = proto.publishDate.parse(context),
            authorIds = proto.authorIdsList.toSet(),
            cover = proto.cover.parse<Image>(context),
            teaser = L10n.single(context, proto.teaser),
            content = L10n.single(context, proto.content.parse(context)),
            categories = proto.categoryIdsList.map { Id<Category>(it) }.toSet(),
            tags = proto.tagIdsList.map { Id<Tag>(it) }.toSet(),
            viewCount = proto.viewCount.value.takeIf { proto.hasViewCount() }
        )

        override fun toProtoBuilder(entity: Article, context: Context): ProtoArticle.Builder =
            ProtoArticle.newBuilder().builder(entity) {
                sourceId = it.sourceId.value
                link = it.link[context].toString()
                title = it.title[context]
                publishDate = it.publishDate.toProto(context)
                addAllAuthorIds(it.authorIds)
                it.cover?.let { c -> cover = c.toProto(context) }
                teaser = it.teaser[context]
                content = it.content[context].toProto(context)
                addAllCategoryIds(it.categories.map { c -> c.value })
                addAllTagIds(it.tags.map { t -> t.value })
                it.viewCount?.let { v -> viewCount = UInt32Value.of(v) }
            }
    }
}

fun Wrapper<Article>.toProto(context: Context): ProtoArticle = Article.ProtoSerializer.toProto(this, context)
