package de.hpi.cloud.news.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.types.l10n
import kotlinx.serialization.Serializable
import de.hpi.cloud.news.v1test.Tag as ProtoTag

@Serializable
data class Tag(
    val title: L10n<String>,
    val articleCount: Int = 0
) : Entity<Tag>() {
    companion object : Entity.Companion<Tag>("tag")

    object ProtoSerializer : Entity.ProtoSerializer<Tag, ProtoTag, ProtoTag.Builder>() {
        override fun fromProto(proto: ProtoTag, context: Context) = Tag(
            title = proto.title.l10n(context),
            articleCount = proto.articleCount
        )

        override fun toProtoBuilder(entity: Tag, context: Context) =
            ProtoTag.newBuilder().builder(entity) {
                title = it.title[context]
                articleCount = it.articleCount
            }
    }
}

fun ProtoTag.parse(context: Context): Tag =
    Tag.ProtoSerializer.fromProto(this, context)

fun Wrapper<Tag>.toProto(context: Context): ProtoTag =
    Tag.ProtoSerializer.toProto(this, context)
