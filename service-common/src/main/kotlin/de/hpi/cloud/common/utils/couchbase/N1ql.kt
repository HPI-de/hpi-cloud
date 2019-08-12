package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.query.dsl.Expression
import com.couchbase.client.java.query.dsl.Expression.i
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.query.dsl.Sort
import com.couchbase.client.java.query.dsl.Sort.asc
import com.couchbase.client.java.query.dsl.Sort.desc

fun and(expressions: List<Expression>): Expression {
    return if (expressions.isEmpty()) x("TRUE")
    else expressions.reduce { e1, e2 -> e1.and(e2) }
}

fun ascTimestamp(field: Expression): Array<Sort> {
    return arrayOf(asc("$field.$TIMESTAMP_MILLIS"), asc("$field.$TIMESTAMP_NANOS"))
}

fun descTimestamp(field: Expression): Array<Sort> {
    return arrayOf(desc("$field.$TIMESTAMP_MILLIS"), desc("$field.$TIMESTAMP_NANOS"))
}

fun n(vararg part: String): Expression {
    return x(part.joinToString(NESTED_SEPARATOR.toString()) { i(it).toString() })
}