package de.hpi.cloud.common.utils

import de.hpi.cloud.common.types.L10n

fun String.trimNotEmpty(): String = trim().also {
    require(it.isNotEmpty()) { "String must not be blank" }
}

fun L10n<String>.trimNotEmpty(): L10n<String> =
    L10n(
        values.mapValues { (locale, value) ->
            value.trim().also {
                require(it.isNotEmpty()) { "String must not be blank (locale $locale" }
            }
        }
    )

fun String.cut(vararg delimiters: Char, ignoreCase: Boolean = false) =
    indexOfAny(delimiters, ignoreCase = ignoreCase).let { index ->
        if (index != -1) substring(0, index)
        else this
    }

fun Pair<String, String>.trim() = first.trim() to second.trim()
fun String.splitAsPair(delimiter: String, ignoreCase: Boolean = false, fromEnd: Boolean = false): Pair<String, String> {
    val index = if (fromEnd) lastIndexOf(delimiter, ignoreCase = ignoreCase)
    else indexOf(delimiter, ignoreCase = ignoreCase)

    return if (index == -1) error("Missing split delimiter")
    else substring(0, index) to substring(index + 1)
}
