package de.hpi.cloud.food.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.Serializable
import de.hpi.cloud.food.v1test.Restaurant as ProtoRestaurant

@Serializable
data class Restaurant(
    val title: L10n<String>
) : Entity<Restaurant>() {
    companion object : Entity.Companion<Restaurant>("restaurant")

    object ProtoSerializer : Entity.ProtoSerializer<Restaurant, ProtoRestaurant, ProtoRestaurant.Builder>() {
        override fun fromProto(proto: ProtoRestaurant, context: Context): Restaurant =
            throw UnsupportedOperationException("Restaurants cannot be created via protobuf")

        override fun toProtoBuilder(entity: Restaurant, context: Context): ProtoRestaurant.Builder =
            ProtoRestaurant.newBuilder().builder(entity) {
                title = it.title[context]
            }
    }
}

fun ProtoRestaurant.parse(context: Context): Restaurant =
    Restaurant.ProtoSerializer.fromProto(this, context)

fun Wrapper<Restaurant>.toProto(context: Context): ProtoRestaurant =
    Restaurant.ProtoSerializer.toProto(this, context)
