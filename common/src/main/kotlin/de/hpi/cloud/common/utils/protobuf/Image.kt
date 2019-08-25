package de.hpi.cloud.common.utils.protobuf

import com.couchbase.client.java.document.json.JsonObject
import de.hpi.cloud.common.utils.couchbase.getI18nString
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import de.hpi.cloud.common.v1test.Image

enum class ImageSize {
    ORIGINAL
}

fun JsonObject.getImage(name: String, size: ImageSize = ImageSize.ORIGINAL, preferredLanguage: String = "en"): Image? {
    val obj = getNestedObject(name) ?: return null
    val source = obj.getObject("source").getString(size.name.toLowerCase()) ?: return null
    return Image.newBuilder()
        .setSource(source)
        .setAlt(obj.getI18nString("alt", preferredLanguage))
        .apply {
            obj.getDouble("aspectRatio")?.toFloat()?.let {
                aspectRatio = it
            }
        }
        .build()
}
