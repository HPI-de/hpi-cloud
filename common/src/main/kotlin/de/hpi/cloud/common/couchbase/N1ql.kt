package de.hpi.cloud.common.couchbase

import com.couchbase.client.java.query.dsl.Expression
import com.couchbase.client.java.query.dsl.Expression.*

fun and(vararg expressions: Expression?): Expression {
    return expressions.filterNotNull().run {
        if (isEmpty()) TRUE()
        else reduce { e1, e2 -> e1.and(e2) }
    }
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
