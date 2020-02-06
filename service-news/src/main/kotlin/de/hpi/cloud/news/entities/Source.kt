package de.hpi.cloud.news.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.serializers.json.UriSerializer
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.types.l10n
import de.hpi.cloud.common.utils.parseUri
import kotlinx.serialization.Serializable
import java.net.URI
import de.hpi.cloud.news.v1test.Source as ProtoSource

@Serializable
data class Source(
    val title: L10n<String>,
    val link: L10n<@Serializable(UriSerializer::class) URI>
) : Entity<Source>() {
    companion object : Entity.Companion<Source>("source")

    object ProtoSerializer : Entity.ProtoSerializer<Source, ProtoSource, ProtoSource.Builder>() {
        override fun fromProto(proto: ProtoSource, context: Context): Source = Source(
            title = proto.title.l10n(context),
            link = proto.link.parseUri().l10n(context)
        )

        override fun toProtoBuilder(entity: Source, context: Context): ProtoSource.Builder =
            ProtoSource.newBuilder().builder(entity) {
                title = it.title[context]
                link = it.link[context].toString()
            }
    }
}

fun Wrapper<Source>.toProto(context: Context): ProtoSource = Source.ProtoSerializer.toProto(this, context)
