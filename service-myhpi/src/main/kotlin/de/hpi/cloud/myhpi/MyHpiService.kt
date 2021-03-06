package de.hpi.cloud.myhpi

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.query.dsl.Expression.s
import com.couchbase.client.java.query.dsl.Expression.x
import com.couchbase.client.java.query.dsl.Sort.asc
import com.couchbase.client.java.view.ViewQuery
import com.google.protobuf.ByteString
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.decodeBase64
import de.hpi.cloud.common.utils.getI18nString
import de.hpi.cloud.common.utils.grpc.*
import de.hpi.cloud.common.utils.protobuf.getImage
import de.hpi.cloud.myhpi.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun main(args: Array<String>) {
    val service = Service("myhpi", args.firstOrNull()?.toInt()) { MyHpiServiceImpl(it) }
    service.blockUntilShutdown()
}

class MyHpiServiceImpl(private val bucket: Bucket) : MyHpiServiceGrpc.MyHpiServiceImplBase() {
    companion object {
        const val ENTITY_INFO_BIT = "infoBit"
        const val ENTITY_TAG = "tag"
        const val ENTITY_ACTION = "action"
    }

    // region InfoBit
    override fun listInfoBits(
        request: ListInfoBitsRequest?,
        responseObserver: StreamObserver<ListInfoBitsResponse>?
    ) = unary(request, responseObserver, "listInfoBits") { req ->
        val parentId = req.parentId?.trim()?.takeIf { it.isNotEmpty() }

        val (infoBits, newToken) = paginate(bucket, {
            where(
                and(
                    x(KEY_TYPE).eq(s(ENTITY_INFO_BIT)),
                    parentId?.let { v("parentId").eq(s(parentId)) }
                )
            )
                .orderBy(
                    asc(v("parentId")),
                    asc(v("order"))
                )
        }, req.pageSize, req.pageToken) { it.parseInfoBit(req) }

        ListInfoBitsResponse.newBuilder().buildWith {
            addAllInfoBits(infoBits)
            nextPageToken = newToken
        }
    }

    override fun getInfoBit(request: GetInfoBitRequest?, responseObserver: StreamObserver<InfoBit>?) =
        unary(request, responseObserver, "getInfoBit") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("InfoBit ID is required")

            getInfoBit(req.id, req) ?: notFound<InfoBit>(req.id)
        }

    private fun getInfoBit(id: String, request: GeneratedMessageV3): InfoBit? {
        return bucket.getContent(ENTITY_INFO_BIT, VIEW_BY_ID, id)?.parseInfoBit(request)
    }

    private fun JsonObject.parseInfoBit(request: GeneratedMessageV3): InfoBit? {
        return InfoBit.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            it.getString("parentId")?.let { p -> parentId = p }
            title = it.getI18nString("title", request)
            it.getI18nString("subtitle", request)?.let { s -> subtitle = s }
            it.getImage("cover", request)?.let { c -> cover = c }
            description = it.getI18nString("description", request)
            it.getI18nString("content", request)?.let { c -> content = c }
            childDisplay = it.getString("childDisplay").parseInfoBitChildDisplay()
            addAllActionIds(it.getStringArray("actionIds"))
            addAllTagIds(it.allInfoBitTagIds(request))
        }
    }

    private fun String.parseInfoBitChildDisplay(): InfoBit.ChildDisplay {
        return InfoBit.ChildDisplay.values().first { it.name.equals(this, true) }
    }

    private fun JsonObject.allInfoBitTagIds(request: GeneratedMessageV3): List<String> {
        val tags = this.getStringArray("tagIds").filterNotNull().toMutableList()
        if (!getString("parentId").isNullOrEmpty())
            getInfoBit(getString("parentId"), request)?.tagIdsList?.let { tags.addAll(it) }
        return tags
    }
    // endregion

    // region InfoBitTag
    override fun listInfoBitTags(
        request: ListInfoBitTagsRequest?,
        responseObserver: StreamObserver<ListInfoBitTagsResponse>?
    ) = unary(request, responseObserver, "listInfoBitTags") { req ->
        val (tags, newToken) = ViewQuery.from(ENTITY_TAG, VIEW_BY_ID)
            .paginate(bucket, req.pageSize, req.pageToken) { it.parseTag(req) }

        ListInfoBitTagsResponse.newBuilder().buildWith {
            addAllTags(tags)
            nextPageToken = newToken
        }
    }

    override fun getInfoBitTag(request: GetInfoBitTagRequest?, responseObserver: StreamObserver<InfoBitTag>?) =
        unary(request, responseObserver, "getInfoBitTag") { req ->
            checkArgRequired(req.id, "id")

            bucket.getContent(ENTITY_TAG, VIEW_BY_ID, req.id)?.parseTag(req)
                ?: notFound<InfoBitTag>(req.id)
        }

    private fun JsonObject.parseTag(request: GeneratedMessageV3): InfoBitTag? {
        return InfoBitTag.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
        }
    }
    // endregion

    // region Action
    override fun listActions(request: ListActionsRequest?, responseObserver: StreamObserver<ListActionsResponse>?) =
        unary(request, responseObserver, "listActions") { req ->
            val (actions, newToken) = ViewQuery.from(ENTITY_ACTION, VIEW_BY_ID)
                .paginate(bucket, req.pageSize, req.pageToken) { it.parseAction(req) }

            ListActionsResponse.newBuilder().buildWith {
                addAllActions(actions)
                nextPageToken = newToken
            }
        }

    override fun getAction(request: GetActionRequest?, responseObserver: StreamObserver<Action>?) =
        unary(request, responseObserver, "getAction") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(ENTITY_ACTION, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseAction(req)
                ?: Status.NOT_FOUND.throwException("Action with ID ${req.id} not found")
        }

    private fun JsonObject.parseAction(request: GeneratedMessageV3): Action? {
        return Action.newBuilder().buildWithDocument(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title", request)
            it.getString("icon")?.let { i -> icon = ByteString.copyFrom(i.decodeBase64()) }
            when {
                it.containsKey("link") ->
                    link = it.getObject("link").let { link ->
                        LinkAction.newBuilder()
                            .setUrl(link.getI18nString("url", request))
                            .build()
                    }
                it.containsKey("text") ->
                    text = it.getObject("text").let { text ->
                        TextAction.newBuilder()
                            .setContent(text.getI18nString("content", request))
                            .build()
                    }
                else -> {
                    println("Action with ID ${getString(KEY_ID)} does not have a valid type")
                }
            }
        }
    }
    // endregion
}
