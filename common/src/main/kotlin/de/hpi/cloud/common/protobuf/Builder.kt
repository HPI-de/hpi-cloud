package de.hpi.cloud.common.protobuf

import com.google.protobuf.GeneratedMessageV3
import com.google.protobuf.GeneratedMessageV3.Builder

inline fun <M : GeneratedMessageV3, B : Builder<B>> B.build(builder: B.() -> Unit): M {
    builder()
    @Suppress("UNCHECKED_CAST")
    return build() as M
}

inline fun <T : Any, B : Builder<B>> B.builder(source: T, builder: B.(T) -> Unit): B {
    builder(source)
    return this
}

inline fun <T : Any, M : GeneratedMessageV3, B : Builder<B>> B.build(source: T, builder: B.(T) -> Unit): M {
    builder(source)
    @Suppress("UNCHECKED_CAST")
    return build() as M
}

fun <B : Builder<B>> B.setId(id: String) {
    this::class.java.getMethod("setId", String::class.java)
        .invoke(this, id)
}
