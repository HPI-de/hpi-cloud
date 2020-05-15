package de.hpi.cloud.food.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.entity.asId
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.protobuf.toLocalDate
import de.hpi.cloud.common.protobuf.toProtoDate
import de.hpi.cloud.common.serializers.json.LocalDateSerializer
import de.hpi.cloud.common.serializers.proto.parse
import de.hpi.cloud.common.serializers.proto.toProto
import de.hpi.cloud.common.types.L10n
import de.hpi.cloud.common.types.Money
import de.hpi.cloud.common.types.l10n
import kotlinx.serialization.Serializable
import java.time.LocalDate
import de.hpi.cloud.food.v1test.MenuItem as ProtoMenuItem

@Serializable
data class MenuItem(
    val openMensaId: String?,
    val date: @Serializable(LocalDateSerializer::class) LocalDate,
    val restaurantId: Id<Restaurant>,
    val offerTitle: L10n<String>,
    val title: L10n<String>,
    val counterId: Id<Counter>?,
    val labelIds: List<Id<Label>>,
    val prices: Map<String, Money>
) : Entity<MenuItem>() {
    companion object : Entity.Companion<MenuItem>("menuItem")

    object ProtoSerializer : Entity.ProtoSerializer<MenuItem, ProtoMenuItem, ProtoMenuItem.Builder>() {
        override fun fromProto(proto: ProtoMenuItem, context: Context): MenuItem =
            MenuItem(
                openMensaId = null,
                date = proto.date.toLocalDate(),
                restaurantId = proto.restaurantId.asId(),
                offerTitle = proto.offerTitle.l10n(context),
                title = proto.title.l10n(context),
                counterId = proto.counterId.asId(),
                labelIds = proto.labelIdsList.map { it.asId<Label>() },
                prices = proto.pricesMap.mapValues { (_, money) -> money.parse(context) }
            )

        override fun toProtoBuilder(entity: MenuItem, context: Context): ProtoMenuItem.Builder =
            ProtoMenuItem.newBuilder().builder(entity) {
                date = it.date.toProtoDate()
                restaurantId = it.restaurantId.value
                offerTitle = it.offerTitle[context]
                title = it.title[context]
                counterId = it.counterId?.value
                addAllLabelIds(it.labelIds.map { label -> label.value })
                putAllPrices(it.prices.mapValues { (_, money) -> money.toProto(context) })
            }
    }
}

fun ProtoMenuItem.parse(context: Context): MenuItem =
    MenuItem.ProtoSerializer.fromProto(this, context)

fun Wrapper<MenuItem>.toProto(context: Context): ProtoMenuItem =
    MenuItem.ProtoSerializer.toProto(this, context)
