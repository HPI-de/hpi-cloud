package de.hpi.cloud.crashreporting

import com.couchbase.client.java.Bucket
import de.hpi.cloud.common.Service
import de.hpi.cloud.common.couchbase.tryInsertOrFail
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.parse
import de.hpi.cloud.common.grpc.checkArgRequired
import de.hpi.cloud.common.grpc.unary
import de.hpi.cloud.crashreporting.entities.CrashReport
import de.hpi.cloud.crashreporting.entities.toProto
import de.hpi.cloud.crashreporting.v1test.CrashReportingServiceGrpc
import de.hpi.cloud.crashreporting.v1test.CreateCrashReportRequest
import io.grpc.stub.StreamObserver
import de.hpi.cloud.crashreporting.v1test.CrashReport as ProtoCrashReport

fun main(args: Array<String>) {
    val service = Service("crashreporting", args.firstOrNull()?.toInt()) { CrashReportingServiceImpl(it) }
    service.blockUntilShutdown()
}

class CrashReportingServiceImpl(private val bucket: Bucket) :
    CrashReportingServiceGrpc.CrashReportingServiceImplBase() {
    override fun createCrashReport(
        request: CreateCrashReportRequest?,
        responseObserver: StreamObserver<ProtoCrashReport>?
    ) = unary(request, responseObserver, "createCrashReport") { req ->
        checkArgRequired(req.hasCrashReport(), "crash_report")
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

        val id = Id.fromClientSupplied<CrashReport, ProtoCrashReport>(req.crashReport.id, bucket)

        val crashReport = req.crashReport.parse<CrashReport>(this)
        val wrapper = crashReport.createNewWrapper(this, id)

        bucket.tryInsertOrFail<CrashReport, ProtoCrashReport>(wrapper)

        wrapper.toProto(this)
    }
    // endregion
}
