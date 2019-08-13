package de.hpi.cloud.myhpi

import com.couchbase.client.java.Bucket
import com.couchbase.client.java.document.json.JsonObject
import com.couchbase.client.java.view.ViewQuery
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.*
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.myhpi.v1test.*
import io.grpc.Status
import io.grpc.stub.StreamObserver

const val PORT_DEFAULT = 50050

fun main(args: Array<String>) {
    val service = Service("myhpi", args.firstOrNull()?.toInt() ?: PORT_DEFAULT) { CourseServiceImpl(it) }
    service.blockUntilShutdown()
}

class CourseServiceImpl(val bucket: Bucket) : MyHpiServiceGrpc.MyHpiServiceImplBase() {
    companion object {
        const val DESIGN_INFO_BIT = "infoBit"
        const val DESIGN_ACTION = "action"
    }

    // region InfoBit
    override fun listInfoBits(
        request: ListInfoBitsRequest?,
        responseObserver: StreamObserver<ListInfoBitsResponse>?
    ) = unary(request, responseObserver, "listInfoBits") { req ->
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

    private fun JsonObject.parseInfoBit(): InfoBit {
        val value = getObject(KEY_VALUE)
        return InfoBit.newBuilder()
            .setId(getString(KEY_ID))
            .setTitle(value.getI18nString("title"))
            .setDescription(value.getI18nString("description"))
            .addAllActionIds(value.getStringArray("actionIds"))
            .build()
    }
    // endregion

    // region Action
    override fun listActions(request: ListActionsRequest?, responseObserver: StreamObserver<ListActionsResponse>?) =
        unary(request, responseObserver, "listActions") { req ->
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
        val value = getObject(KEY_VALUE)
        return Action.newBuilder()
            .setId(getString(KEY_ID))
            .setTitle(value.getI18nString("title"))
            .also {
                if (value.containsKey("link")) {
                    val link = value.getObject("link")
                    it.setLink(
                        Action.Link.newBuilder()
                            .setUrl(link.getI18nString("url"))
                    )
                } else if (value.containsKey("text")) {
                    val text = value.getObject("text")
                    it.setText(
                        Action.Text.newBuilder()
                            .setContent(text.getI18nString("content"))
                    )
                } else {
                    println("Action with ID ${getString(KEY_ID)} does not have a valid type")
                    return null
                }
            }
            .build()
    }
    // endregion
}
