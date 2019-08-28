package de.hpi.cloud.common.utils.protobuf

abstract class Currency(
    val javaCurrency: java.util.Currency
) {
    abstract val units: Long
    abstract val subunits: Long
    abstract fun toDbMap(): Map<String, Any>
    override fun toString(): String = "%.2f${javaCurrency.symbol}".format(units + subunits / 1_000_000_000.0)
}

class EuroPrice(cents: Double) : Currency(java.util.Currency.getInstance("EUR")) {
    companion object {
        fun from(euro: Long, cent: Double) = EuroPrice(euro * 100 + cent)
    }

    override val units: Long = cents.toLong() / 100
    override val subunits: Long = ((cents % 100) * 10_000_000.0).toLong()

    override fun toDbMap() = mapOf<String, Any>(
        "currencyCode" to javaCurrency.currencyCode,
        "units" to units,
        "nanos" to subunits
    )

    override fun toString(): String = "%.2f${javaCurrency.symbol}".format(units + subunits / 1_000_000_000.0)
}

fun Number.euro(): EuroPrice = EuroPrice(this.toDouble() * 100)
