package de.hpi.cloud.common.utils

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

fun Collection<String>.contains(value: String, ignoreCase: Boolean = false): Boolean =
    any { it.equals(value, ignoreCase) }
