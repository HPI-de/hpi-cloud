package de.hpi.cloud.common.utils.protobuf

import com.couchbase.client.java.document.json.JsonObject
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.utils.getI18nString
import de.hpi.cloud.common.utils.couchbase.getNestedObject
import de.hpi.cloud.common.utils.grpc.buildWith
import de.hpi.cloud.common.v1test.Image

enum class ImageSize {
    ORIGINAL;

    val dbKey = toString().toLowerCase()
}

fun JsonObject.getImage(name: String, request: GeneratedMessageV3, size: ImageSize = ImageSize.ORIGINAL): Image? {
    return Image.newBuilder().buildWith(getNestedObject(name)) {
        source = it.getObject("source")?.getString(size.dbKey) ?: return null
        it.getI18nString("alt", request)?.let { a -> alt = a }
        it.getDouble("aspectRatio")?.toFloat()?.let { a -> aspectRatio = a }
    }
}
