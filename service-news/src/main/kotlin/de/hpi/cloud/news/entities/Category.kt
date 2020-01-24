package de.hpi.cloud.news.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.utils.l10n
import kotlinx.serialization.Serializable
import de.hpi.cloud.news.v1test.Category as ProtoCategory

@Serializable
data class Category(
    val title: L10n<String>
) : Entity<Category>() {
    companion object : Entity.Companion<Category>("category")

    object ProtoSerializer : Entity.ProtoSerializer<Category, ProtoCategory, ProtoCategory.Builder>() {
        override fun fromProto(proto: ProtoCategory, context: Context): Category = Category(
            title = proto.title.l10n(context)
        )

        override fun toProtoBuilder(entity: Category, context: Context): ProtoCategory.Builder =
            ProtoCategory.newBuilder().builder(entity) {
                title = it.title[context]
            }
    }
}

fun Wrapper<Category>.toProto(context: Context): ProtoCategory = Category.ProtoSerializer.toProto(this, context)
