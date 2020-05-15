package de.hpi.cloud.food.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.serializers.json.UriSerializer
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.Serializable
import java.net.URI
import de.hpi.cloud.food.v1test.Counter as ProtoCounter

@Serializable
data class Counter(
    val restaurantId: Id<Restaurant>,
    val title: L10n<String>,
    val iconUri: @Serializable(UriSerializer::class) URI? = null
) : Entity<Counter>() {
    companion object : Entity.Companion<Counter>("counter")

    object ProtoSerializer : Entity.ProtoSerializer<Counter, ProtoCounter, ProtoCounter.Builder>() {
        override fun fromProto(proto: ProtoCounter, context: Context): Counter =
            throw UnsupportedOperationException("Counter cannot be created via protobuf")

        override fun toProtoBuilder(entity: Counter, context: Context): ProtoCounter.Builder =
            ProtoCounter.newBuilder().builder(entity) {
                restaurantId = it.restaurantId.value
                title = it.title[context]
                icon = it.iconUri.toString()
            }
    }
}

fun ProtoCounter.parse(context: Context): Counter =
    Counter.ProtoSerializer.fromProto(this, context)

fun Wrapper<Counter>.toProto(context: Context): ProtoCounter =
    Counter.ProtoSerializer.toProto(this, context)
