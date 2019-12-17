package de.hpi.cloud.common.types

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.serializers.CurrencySerializer
import kotlinx.serialization.Serializable
import java.util.*
import com.google.type.Money as ProtoMoney

@Serializable
data class Money(
    @Serializable(CurrencySerializer::class)
    val currencyCode: Currency,
    val units: Long,
    val nanos: Int
) : Persistable<Money>() {
    companion object {
        const val CURRENCY_EUR = "EUR"
        const val NANOS_IN_UNIT = 1_000_000_000

        fun eur(value: Number): Money = Money(
            currencyCode = Currency.getInstance(CURRENCY_EUR),
            units = value.toLong(),
            nanos = ((value.toDouble() % 1) * NANOS_IN_UNIT).toInt()
        )
    }

    object ProtoSerializer : Persistable.ProtoSerializer<Money, ProtoMoney> {
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
}

fun Money.toProto(context: Context): ProtoMoney = Money.ProtoSerializer.toProto(this, context)
