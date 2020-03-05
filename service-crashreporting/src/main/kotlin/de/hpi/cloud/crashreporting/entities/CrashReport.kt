package de.hpi.cloud.crashreporting.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.entity.parse
import de.hpi.cloud.common.protobuf.build
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.serializers.json.InstantSerializer
import de.hpi.cloud.common.serializers.proto.toProto
import kotlinx.serialization.Serializable
import java.time.Instant
import de.hpi.cloud.common.serializers.proto.ProtoSerializer as BaseProtoSerializer
import de.hpi.cloud.crashreporting.v1test.CrashReport as ProtoCrashReport

@Serializable
data class CrashReport(
    val appName: String,
    val appVersion: String,
    val appVersionCode: Int,
    val device: Device,
    val operatingSystem: OperatingSystem,
    val timestamp: @Serializable(InstantSerializer::class) Instant,
    val exception: String,
    val stackTrace: String,
    val log: String
) : Entity<CrashReport>() {
    companion object : Entity.Companion<CrashReport>("crashReport")
    object ProtoSerializer : Entity.ProtoSerializer<CrashReport, ProtoCrashReport, ProtoCrashReport.Builder>() {
        override fun fromProto(proto: ProtoCrashReport, context: Context): CrashReport = CrashReport(
            appName = proto.appName.trim(),
            appVersion = proto.appVersion.trim(),
            appVersionCode = proto.appVersionCode,
            device = proto.device.parse(context),
            operatingSystem = proto.operatingSystem.parse(context),
            timestamp = proto.timestamp.parse(context),
            exception = proto.exception.trim(),
            stackTrace = proto.stackTrace.trim(),
            log = proto.log.trim()
        )

        override fun toProtoBuilder(entity: CrashReport, context: Context): ProtoCrashReport.Builder =
            ProtoCrashReport.newBuilder().builder(entity) {
                appName = it.appName
                appVersion = it.appVersion
                appVersionCode = it.appVersionCode
                device = it.device.toProto(context)
                operatingSystem = it.operatingSystem.toProto(context)
                timestamp = it.timestamp.toProto(context)
                exception = it.exception
                stackTrace = it.stackTrace
                log = it.log
            }
    }

    @Serializable
    data class Device(
        val brand: String,
        val model: String
    ) {
        object ProtoSerializer : BaseProtoSerializer<Device, ProtoCrashReport.Device> {
            override fun fromProto(proto: ProtoCrashReport.Device, context: Context): Device = Device(
                brand = proto.brand.trim(),
                model = proto.model.trim()
            )

            override fun toProto(entity: Device, context: Context): ProtoCrashReport.Device =
                ProtoCrashReport.Device.newBuilder().build(entity) {
                    brand = it.brand
                    model = it.model
                }
        }

        fun toProto(context: Context) = ProtoSerializer.toProto(this, context)
    }

    @Serializable
    data class OperatingSystem(
        val os: String,
        val version: String
    ) {
        object ProtoSerializer : BaseProtoSerializer<OperatingSystem, ProtoCrashReport.OperatingSystem> {
            override fun fromProto(proto: ProtoCrashReport.OperatingSystem, context: Context): OperatingSystem =
                OperatingSystem(
                    os = proto.os.trim(),
                    version = proto.version.trim()
                )

            override fun toProto(persistable: OperatingSystem, context: Context): ProtoCrashReport.OperatingSystem =
                ProtoCrashReport.OperatingSystem.newBuilder().build(persistable) {
                    os = it.os
                    version = it.version
                }
        }

        fun toProto(context: Context) = ProtoSerializer.toProto(this, context)
    }
}

fun Wrapper<CrashReport>.toProto(context: Context): ProtoCrashReport =
    CrashReport.ProtoSerializer.toProto(this, context)
