package de.hpi.cloud.crashreporting

import com.couchbase.client.java.Bucket
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.utils.couchbase.buildJsonDocument
import de.hpi.cloud.common.utils.grpc.checkArgNotSet
import de.hpi.cloud.common.utils.grpc.checkArgRequired
import de.hpi.cloud.common.utils.grpc.unary
import de.hpi.cloud.common.utils.protobuf.toDbMap
import de.hpi.cloud.common.utils.then
import de.hpi.cloud.crashreporting.v1test.CrashReport
import de.hpi.cloud.crashreporting.v1test.CrashReportingServiceGrpc
import de.hpi.cloud.crashreporting.v1test.CreateCrashReportRequest
import io.grpc.stub.StreamObserver
import java.util.*

fun main(args: Array<String>) {
    val service = Service("crashreporting", args.firstOrNull()?.toInt()) { CrashReportingServiceImpl(it) }
    service.blockUntilShutdown()
}

class CrashReportingServiceImpl(private val bucket: Bucket) :
    CrashReportingServiceGrpc.CrashReportingServiceImplBase() {
    companion object {
        const val TYPE_CRASH_REPORT = "crashReport"
    }

    override fun createCrashReport(
        request: CreateCrashReportRequest?,
        responseObserver: StreamObserver<CrashReport>?
    ) = unary(request, responseObserver, "createCrashReport") { req ->
        checkArgRequired(req.hasCrashReport(), "crash_report")
        checkArgNotSet(req.crashReport.id, "crash_report.id")
        checkArgRequired(req.crashReport.appName, "crash_report.app_name")
        checkArgRequired(req.crashReport.appVersion, "crash_report.app_version")
        checkArgRequired(req.crashReport.appVersionCode != 0, "crash_report.app_version_code")
        if (req.crashReport.hasDevice()) {
            checkArgRequired(
                req.crashReport.device.brand,
                "crash_report.device.brand",
                ifArgSet = "crash_report.device"
            )
            checkArgRequired(
                req.crashReport.device.model,
                "crash_report.device.model",
                ifArgSet = "crash_report.device"
            )
        }
        if (req.crashReport.hasOperatingSystem()) {
            checkArgRequired(
                req.crashReport.operatingSystem.os,
                "crash_report.operating_system.os",
                ifArgSet = "crash_report.operating_system"
            )
            checkArgRequired(
                req.crashReport.operatingSystem.version,
                "crash_report.operating_system.version",
                ifArgSet = "crash_report.operating_system"
            )
        }
        checkArgRequired(req.crashReport.hasTimestamp(), "crash_report.timestamp")
        checkArgRequired(req.crashReport.exception, "crash_report.exception")
        checkArgRequired(req.crashReport.stackTrace, "crash_report.stack_trace")

        val id = UUID.randomUUID().toString()

        bucket.insert(
            buildJsonDocument(
                id, TYPE_CRASH_REPORT, 1, mapOf(
                    "appName" to req.crashReport.appName,
                    "appVersion" to req.crashReport.appVersion,
                    "appVersionCode" to req.crashReport.appVersionCode,
                    "device" to req.crashReport.hasDevice().then(req.crashReport.device) {
                        mapOf(
                            "brand" to it.brand,
                            "model" to it.model
                        )
                    },
                    "operatingSystem" to req.crashReport.hasOperatingSystem().then(req.crashReport.operatingSystem) {
                        mapOf(
                            "os" to it.os,
                            "version" to it.version
                        )
                    },
                    "timestamp" to req.crashReport.timestamp.toDbMap(),
                    "exception" to req.crashReport.exception,
                    "stackTrace" to req.crashReport.stackTrace,
                    "log" to req.crashReport.log
                )
            )
        )
        CrashReport.newBuilder(req.crashReport)
            .setId(id)
            .build()
    }
    // endregion
}
