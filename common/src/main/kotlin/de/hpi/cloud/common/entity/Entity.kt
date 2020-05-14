package de.hpi.cloud.common.entity

import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.protobuf.setId
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance
import de.hpi.cloud.common.serializers.proto.ProtoSerializer as AnyProtoSerializer

abstract class Entity<E : Entity<E>> {
    abstract class Companion<E : Entity<E>>(
        val type: String,
        val version: Int = 1
    )

    abstract class ProtoSerializer<E : Entity<E>, Proto : GeneratedMessageV3, B : GeneratedMessageV3.Builder<B>> :
        AnyProtoSerializer<E, Proto> {
        override fun toProto(persistable: E, context: Context): Proto {
            throw UnsupportedOperationException("Entities can only be serialized using their wrapper")
        }

        abstract fun toProtoBuilder(entity: E, context: Context): B

        fun toProto(wrapper: Wrapper<E>, context: Context): Proto {
            val builder = toProtoBuilder(wrapper.value, context)
            builder.setId(wrapper.id.value)

            @Suppress("UNCHECKED_CAST")
            return builder.build() as Proto
        }
    }

    fun companion(): Companion<E> {
        @Suppress("UNCHECKED_CAST")
        return (this::class as KClass<E>).entityCompanion()
    }

    fun createNewWrapper(
        context: Context,
        id: Id<E> = Id.random(),
        permissions: Permissions = emptyMap(),
        published: Boolean = true
    ): Wrapper<E> {
        @Suppress("UNCHECKED_CAST")
        return Wrapper.create(
            context = context,
            companion = companion(),
            id = id,
            permissions = permissions,
            value = this as E,
            published = published
        )
    }
}

fun <E : Entity<E>> KClass<E>.entityCompanion(): Entity.Companion<E> {
    @Suppress("UNCHECKED_CAST")
    return companionObjectInstance as Entity.Companion<E>
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <E : Entity<E>> KClass<E>.jsonSerializer(): KSerializer<E> = serializer()
