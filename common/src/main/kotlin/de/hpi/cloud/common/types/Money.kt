package de.hpi.cloud.common.types

import de.hpi.cloud.common.serializers.json.CurrencySerializer
import kotlinx.serialization.Serializable
import java.util.*

@Serializable
data class Money(
    @Serializable(CurrencySerializer::class)
    val currencyCode: Currency,
    val units: Long,
    val nanos: Int
) {
    companion object {
        const val CURRENCY_EUR = "EUR"

        private const val NANOS_IN_UNIT = 1_000_000_000

        fun eur(value: Number): Money = Money(
            currencyCode = Currency.getInstance(CURRENCY_EUR),
            units = value.toLong(),
            nanos = ((value.toDouble() % 1) * NANOS_IN_UNIT).toInt()
        )
    }
}
