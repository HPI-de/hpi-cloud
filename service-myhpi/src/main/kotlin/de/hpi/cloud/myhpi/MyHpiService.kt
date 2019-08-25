package de.hpi.cloud.myhpi

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.buildWith
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.myhpi.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

fun main(args: Array<String>) {
    val service = Service("myhpi", args.firstOrNull()?.toInt()) { MyHpiServiceImpl(it) }
    service.blockUntilShutdown()
}

class MyHpiServiceImpl(private val bucket: Bucket) : MyHpiServiceGrpc.MyHpiServiceImplBase() {
    companion object {
        const val DESIGN_INFO_BIT = "infoBit"
        const val DESIGN_ACTION = "action"
    }

    // region InfoBit
    override fun listInfoBits(
        request: ListInfoBitsRequest?,
        responseObserver: StreamObserver<ListInfoBitsResponse>?
    ) = unary(request, responseObserver, "listInfoBits") { _ ->
        val infoBits = bucket.query(ViewQuery.from(DESIGN_INFO_BIT, VIEW_BY_ID)).allRows()
            .map { it.document().content().parseInfoBit() }
        ListInfoBitsResponse.newBuilder()
            .addAllInfoBits(infoBits)
            .build()
    }

    override fun getInfoBit(request: GetInfoBitRequest?, responseObserver: StreamObserver<InfoBit>?) =
        unary(request, responseObserver, "getInfoBit") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("InfoBit ID is required")

            bucket.get(DESIGN_INFO_BIT, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseInfoBit()
                ?: Status.NOT_FOUND.throwException("InfoBit with ID ${req.id} not found")
        }

    private fun JsonObject.parseInfoBit(): InfoBit? {
        return InfoBit.newBuilder().buildWith(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title")
            description = it.getI18nString("description")
            addAllActionIds(it.getStringArray("actionIds"))
        }
    }
    // endregion

    // region Action
    override fun listActions(request: ListActionsRequest?, responseObserver: StreamObserver<ListActionsResponse>?) =
        unary(request, responseObserver, "listActions") { _ ->
            val actions = bucket.query(ViewQuery.from(DESIGN_ACTION, VIEW_BY_ID)).allRows()
                .map { it.document().content().parseAction() }
            ListActionsResponse.newBuilder()
                .addAllActions(actions)
                .build()
        }

    override fun getAction(request: GetActionRequest?, responseObserver: StreamObserver<Action>?) =
        unary(request, responseObserver, "getAction") { req ->
            if (req.id.isNullOrEmpty()) Status.INVALID_ARGUMENT.throwException("Argument ID is required")

            bucket.get(DESIGN_ACTION, VIEW_BY_ID, req.id)
                ?.document()?.content()?.parseAction()
                ?: Status.NOT_FOUND.throwException("Action with ID ${req.id} not found")
        }

    private fun JsonObject.parseAction(): Action? {
        return Action.newBuilder().buildWith(this) {
            id = getString(KEY_ID)
            title = it.getI18nString("title")
            it.getString("icon")?.let { i -> icon = i }
            when {
                it.containsKey("link") ->
                    link = it.getObject("link").let { link ->
                        Action.Link.newBuilder()
                            .setUrl(link.getI18nString("url"))
                            .build()
                    }
                it.containsKey("text") ->
                    text = it.getObject("text").let { text ->
                        Action.Text.newBuilder()
                            .setContent(text.getI18nString("content"))
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
