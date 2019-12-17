package de.hpi.cloud.common.entity

import com.google.protobuf.GeneratedMessageV3
import de.hpi.cloud.common.Context
import de.hpi.cloud.common.Persistable
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer
import java.net.URI
import kotlin.reflect.KClass
import kotlin.reflect.full.companionObjectInstance

abstract class Entity<E : Entity<E>> : Persistable<E>() {
    abstract class Companion<E : Entity<E>>(
        val type: String,
        val version: Int = 1
    )

    abstract class ProtoSerializer<E : Entity<E>, Proto : GeneratedMessageV3> : Persistable.ProtoSerializer<E, Proto> {
        abstract override fun toProto(@Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE") entity: E, context: Context): Proto
        fun toProto(wrapper: Wrapper<E>, context: Context): Proto = toProto(wrapper.value, context).apply {
            this::class.java.getMethod("setId", String::class.java).invoke(this, wrapper.id)
        }
    }

    fun companion(): Companion<E> {
        @Suppress("UNCHECKED_CAST")
        return this::class.companionObjectInstance as Companion<E>
    }

    fun createNewWrapper(
        context: Context,
        id: Id<E> = Id.random(),
        sources: List<L10n<URI>> = emptyList(),
        permissions: Permissions = emptyMap(),
        published: Boolean = true
    ): Wrapper<E> {
        val companion = companion()

        @Suppress("UNCHECKED_CAST")
        return Wrapper.create(
            context = context,
            companion = companion,
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
    return this::class.companionObjectInstance as Entity.Companion<E>
}

@UseExperimental(ImplicitReflectionSerializer::class)
fun <E : Entity<E>> KClass<E>.jsonSerializer(): KSerializer<E> {
    return this.serializer()
}

fun <P : Persistable<P>, Proto : GeneratedMessageV3> KClass<P>.protoSerializer(): Persistable.ProtoSerializer<P, Proto> {
    @Suppress("UNCHECKED_CAST")
    return this.nestedClasses.first { it.simpleName == "ProtoSerializer" }.objectInstance as Persistable.ProtoSerializer<P, Proto>
}

inline fun <reified P : Persistable<P>> GeneratedMessageV3.parse(context: Context): P {
    return P::class.protoSerializer<P, GeneratedMessageV3>().fromProto(this, context)
}
