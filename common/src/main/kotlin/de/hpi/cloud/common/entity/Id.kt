package de.hpi.cloud.common.entity

import com.couchbase.client.java.Bucket
import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.grpc.throwAlreadyExists
import de.hpi.cloud.common.grpc.throwInvalidArgument
import kotlinx.serialization.*
import kotlinx.serialization.internal.StringDescriptor
import java.util.*

@Suppress("unused")
@Serializable(with = Id.JsonSerializer::class)
data class Id<E : Entity<E>>(val value: String) {
    companion object {
        const val LENGTH_MIN = 4
        const val LENGTH_MAX = 64
        val PATTERN = "[a-z0-9\\-_:.]+".toRegex(RegexOption.IGNORE_CASE)

        fun <E : Entity<E>> random(): Id<E> =
            UUID.randomUUID().toString().asId()

        fun <E : Entity<E>> fromParts(vararg parts: String): Id<E> =
            parts.joinToString(separator = "_").asId()

        inline fun <reified E : Entity<E>, reified Proto : GeneratedMessageV3> fromClientSupplied(
            requestedValue: String,
            bucket: Bucket
        ): Id<E> {
            return when {
                requestedValue.isEmpty() -> random()

                requestedValue.length < LENGTH_MIN ->
                    throwInvalidArgument("id must be at least $LENGTH_MIN characters long", requestedValue)
                requestedValue.length > LENGTH_MAX ->
                    throwInvalidArgument("id must be at most $LENGTH_MAX characters long", requestedValue)
                !PATTERN.matches(requestedValue) ->
                    throwInvalidArgument(
                        "id must match the case-insensitive regex \"${PATTERN.pattern}\"",
                        requestedValue
                    )

                bucket.exists(requestedValue) -> throwAlreadyExists<Proto>(Id<E>(requestedValue))
                else -> Id(requestedValue)
            }
        }
    }

    fun truncated(maxLength: Int = LENGTH_MAX): Id<E> =
        copy(value = value.take(maxLength))

    @Serializer(forClass = Id::class)
    class JsonSerializer<E : Entity<E>>(
        private val entitySerializer: KSerializer<E>
    ) : KSerializer<Id<E>> {
        override val descriptor: SerialDescriptor = StringDescriptor.withName("Id")

        override fun serialize(encoder: Encoder, obj: Id<E>) = encoder.encodeString(obj.value)
        override fun deserialize(decoder: Decoder): Id<E> = Id(decoder.decodeString())
    }

    inline fun <reified E : Entity<E>> documentId(): String = "${E::class.entityCompanion().type}:$value"
    internal fun documentId(type: String): String = "$type:$value"
}

fun <E : Entity<E>> String.asId(): Id<E> = Id(this)
