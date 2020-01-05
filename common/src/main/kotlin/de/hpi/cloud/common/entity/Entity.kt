package de.hpi.cloud.common.entity

import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.protobuf.setId
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.full.allSuperclasses
import kotlin.reflect.full.companionObjectInstance

abstract class Entity<E : Entity<E>> : Persistable<E>() {
    abstract class Companion<E : Entity<E>>(
        val type: String,
        val version: Int = 1
    )

    abstract class ProtoSerializer<E : Entity<E>, Proto : GeneratedMessageV3, B : GeneratedMessageV3.Builder<B>> :
        Persistable.ProtoSerializer<E, Proto> {
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
        sources: List<L10n<URI>> = emptyList(),
        permissions: Permissions = emptyMap(),
        published: Boolean = true
    ): Wrapper<E> {
        @Suppress("UNCHECKED_CAST")
        return Wrapper.create(
            context = context,
            companion = companion(),
            id = id,
            sources = sources,
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
fun <E : Entity<E>> KClass<E>.jsonSerializer(): KSerializer<E> {
    return serializer()
}

fun <P : Persistable<P>, Proto : GeneratedMessageV3> KClass<P>.protoSerializer(): Persistable.ProtoSerializer<P, Proto> {
    @Suppress("UNCHECKED_CAST")
    return nestedClasses
        .first { Persistable.ProtoSerializer::class in it.allSuperclasses }
        .objectInstance as Persistable.ProtoSerializer<P, Proto>
}


inline fun <reified P : Persistable<P>> GeneratedMessageV3.parse(context: Context): P {
    return P::class.protoSerializer<P, GeneratedMessageV3>().fromProto(this, context)
}

inline fun <reified P : Persistable<P>> GeneratedMessageV3.parseIf(context: Context, hasField: Boolean): P? {
    return if (!hasField) null
    else P::class.protoSerializer<P, GeneratedMessageV3>().fromProto(this, context)
}
