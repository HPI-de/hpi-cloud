package de.hpi.cloud.common.couchbase

const val KEY_TYPE = "type"
const val KEY_VERSION = "version"
const val KEY_ID = "id"
const val KEY_METADATA = "meta"
const val KEY_VALUE = "value"

const val NESTED_SEPARATOR = "."

fun devDesignDoc(designDoc: String) = "dev_$designDoc"
const val VIEW_BY_ID = "byId"
