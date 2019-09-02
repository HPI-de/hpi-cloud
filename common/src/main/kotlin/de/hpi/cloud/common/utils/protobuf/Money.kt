package de.hpi.cloud.common.utils.protobuf

import com.couchbase.client.java.document.json.JsonObject
import com.google.type.Money
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import de.hpi.cloud.common.utils.grpc.buildWith

private const val MONEY_CURRENCY_CODE = "currencyCode"
private const val MONEY_UNITS = "units"
private const val MONEY_NANOS = "nanos"


fun Number.money(currencyCode: String): Money = Money.newBuilder()
    .setCurrencyCode(currencyCode)
    .setUnits(toLong())
    .setNanos(((toDouble() % 1) * 1_000_000_000).toInt())
    .build()

fun Number.euros() = money("EUR")

fun JsonObject.getMoney(name: String): Money? {
    return Money.newBuilder().buildWith(getNestedObject(name)) {
        currencyCode = it.getString(MONEY_CURRENCY_CODE) ?: return null
        units = it.getLong(MONEY_UNITS) ?: return null
        nanos = it.getInt(MONEY_NANOS) ?: return null
    }
}

fun Money.toDbMap() = mapOf(
    MONEY_CURRENCY_CODE to currencyCode,
    MONEY_UNITS to units,
    MONEY_NANOS to nanos
)