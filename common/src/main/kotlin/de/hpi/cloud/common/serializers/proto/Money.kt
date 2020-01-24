package de.hpi.cloud.common.serializers.proto

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.types.Money
import java.util.*
import com.google.type.Money as ProtoMoney

object MoneySerializer : Persistable.ProtoSerializer<Money, ProtoMoney> {
    override fun fromProto(proto: ProtoMoney, context: Context): Money {
        return Money(
            currencyCode = Currency.getInstance(proto.currencyCode),
            units = proto.units,
            nanos = proto.nanos
        )
    }

    override fun toProto(persistable: Money, context: Context): ProtoMoney =
        ProtoMoney.newBuilder().build(persistable) {
            currencyCode = it.currencyCode.toString()
            units = it.units
            nanos = it.nanos
        }
}

fun Money.toProto(context: Context): ProtoMoney =
    MoneySerializer.toProto(this, context)
