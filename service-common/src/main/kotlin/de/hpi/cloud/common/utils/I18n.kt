package de.hpi.cloud.common.utils

import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import de.hpi.cloud.common.utils.grpc.preferredLocales
import java.util.*


const val LANGUAGE_FALLBACK = "en"
fun JsonObject.getI18nString(name: String, request: GeneratedMessageV3): String? {
    val obj = getNestedObject(name)

    val preferredLocales = Service.metadataForRequest(request).preferredLocales
    val availableLocales = obj?.toMap()?.keys ?: return null

    return Locale.lookupTag(preferredLocales, availableLocales)?.let { obj.getString(it) }
        ?: obj.getString(LANGUAGE_FALLBACK)
        ?: (obj.toMap()?.values?.firstOrNull() as? String)
}
