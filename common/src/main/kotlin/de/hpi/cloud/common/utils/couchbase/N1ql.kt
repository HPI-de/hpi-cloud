package de.hpi.cloud.common.utils.couchbase

import com.couchbase.client.java.query.dsl.Expression
import com.couchbase.client.java.query.dsl.Expression.*
import com.couchbase.client.java.query.dsl.Sort
import com.couchbase.client.java.query.dsl.Sort.asc
import com.couchbase.client.java.query.dsl.Sort.desc
import de.hpi.cloud.common.utils.protobuf.TIMESTAMP_MILLIS
import de.hpi.cloud.common.utils.protobuf.TIMESTAMP_NANOS

fun and(vararg expressions: Expression?): Expression {
    return expressions.filterNotNull().run {
        if (isEmpty()) TRUE()
        else reduce { e1, e2 -> e1.and(e2) }
    }
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