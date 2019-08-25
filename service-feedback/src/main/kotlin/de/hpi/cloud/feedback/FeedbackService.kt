package de.hpi.cloud.feedback

import com.couchbase.client.java.Bucket
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.encodeBase64
import de.hpi.cloud.common.utils.grpc.buildJsonDocument
import de.hpi.cloud.common.utils.grpc.throwException
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.common.utils.tryParseUri
import de.hpi.cloud.feedback.v1test.CreateFeedbackRequest
import de.hpi.cloud.feedback.v1test.Feedback
import de.hpi.cloud.feedback.v1test.FeedbackServiceGrpc
import io.grpc.Status
import io.grpc.stub.StreamObserver
import java.util.*

fun main(args: Array<String>) {
    val service = Service("feedback", args.firstOrNull()?.toInt()) { FeedbackService(it) }
    service.blockUntilShutdown()
}

class FeedbackService(private val bucket: Bucket) : FeedbackServiceGrpc.FeedbackServiceImplBase() {
    companion object {
        const val ENTITY_FEEDBACK = "feedback"
    }

    // region Feedback
    override fun createFeedback(
        request: CreateFeedbackRequest?,
        responseObserver: StreamObserver<Feedback>?
    ) = unary(request, responseObserver, "createFeedback") { req ->
        if (!req.hasFeedback()) Status.INVALID_ARGUMENT.throwException("Field feedback is required")
        if (!req.feedback.id.isNullOrBlank()) Status.INVALID_ARGUMENT.throwException("feedback.id must not be set")
        if (req.feedback.message.isNullOrBlank()) Status.INVALID_ARGUMENT.throwException("feedback.message is required")
        val screenUri =
            if (req.feedback.screenUri.isNullOrBlank()) null
            else req.feedback.screenUri.tryParseUri()
                ?: Status.INVALID_ARGUMENT.throwException("feedback.screenUri is not a valid URI")
        val id = UUID.randomUUID().toString()

        bucket.insert(
            buildJsonDocument(
                id, ENTITY_FEEDBACK, 1, mapOf(
                    "message" to req.feedback.message,
                    "screenUri" to screenUri?.toString(),
                    "user" to req.feedback.user,
                    "screenshot" to req.feedback.screenshot.toByteArray().encodeBase64(),
                    "log" to req.feedback.log
                )
            )
        )
        Feedback.newBuilder(req.feedback)
            .setId(id)
            .build()
    }
    // endregion
}
