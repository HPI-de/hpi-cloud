package de.hpi.cloud.club.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Party
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.Serializable
import de.hpi.cloud.club.v1test.Club as ProtoClub

@Serializable
data class Club(
    val title: L10n<String>,
    val abbreviation: String,
    val headGroupId: Id<Party>,
    val memberGroupId: Id<Party>
) : Entity<Club>() {
    companion object : Entity.Companion<Club>("club")

    object ProtoSerializer : Entity.ProtoSerializer<Club, ProtoClub, ProtoClub.Builder>() {
        override fun fromProto(proto: ProtoClub, context: Context): Club = Club(
            title = L10n.single(context, proto.title),
            abbreviation = proto.abbreviation,
            headGroupId = Id(proto.headGroupId),
            memberGroupId = Id(proto.memberGroupId)
        )

        override fun toProtoBuilder(entity: Club, context: Context): ProtoClub.Builder =
            ProtoClub.newBuilder().builder(entity) {
                title = it.title[context]
                abbreviation = it.abbreviation
                headGroupId = it.headGroupId.value
                memberGroupId = it.memberGroupId.value
            }
    }
}

fun Wrapper<Club>.toProto(context: Context): ProtoClub = Club.ProtoSerializer.toProto(this, context)
