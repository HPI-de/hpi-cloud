package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.query.dsl.Expression
import com.couchbase.client.java.query.dsl.Expression.*
import com.couchbase.client.java.query.dsl.Sort
import com.couchbase.client.java.query.dsl.Sort.asc
import com.couchbase.client.java.query.dsl.Sort.desc
import de.hpi.cloud.common.types.Instant

fun and(vararg expressions: Expression?): Expression {
    return expressions.filterNotNull().run {
        if (isEmpty()) TRUE()
        else reduce { e1, e2 -> e1.and(e2) }
    }
}

fun ascTimestamp(field: Expression): Array<Sort> {
    return arrayOf(
        asc("$field.${Instant.JsonSerializer.KEY_MILLIS}"),
        asc("$field.${Instant.JsonSerializer.KEY_NANOS}")
    )
}

fun descTimestamp(field: Expression): Array<Sort> {
    return arrayOf(
        desc("$field.${Instant.JsonSerializer.KEY_MILLIS}"),
        desc("$field.${Instant.JsonSerializer.KEY_NANOS}")
    )
}

/**
 * Builds an expression for a *nested* field with proper escaping.
 */
fun n(vararg part: String): Expression {
    return x(part.joinToString(NESTED_SEPARATOR) { i(it).toString() })
}

/**
 * Builds an expression for a *nested* field inside the *value* ([KEY_VALUE]) section of a document with proper escaping.
 *
 * Basically, it's just a shorthand for `n(KEY_VALUE, part...)`
 */
fun v(vararg part: String) = n(KEY_VALUE, *part)
