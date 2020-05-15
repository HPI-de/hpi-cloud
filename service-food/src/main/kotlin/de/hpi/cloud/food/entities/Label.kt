package de.hpi.cloud.food.entities

import de.hpi.cloud.common.Context
import de.hpi.cloud.common.entity.Entity
import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.Wrapper
import de.hpi.cloud.common.entity.asId
import de.hpi.cloud.common.protobuf.builder
import de.hpi.cloud.common.serializers.json.UriSerializer
import de.hpi.cloud.common.types.L10n
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.net.URI
import java.util.Locale.GERMAN
import de.hpi.cloud.food.v1test.Label as ProtoLabel

@Serializable
data class Label(
    val title: L10n<String>,
    val aliases: Set<String> = setOf(),
    val iconUri: @Serializable(UriSerializer::class) URI? = null
) : Entity<Label>() {
    companion object : Entity.Companion<Label>("label")

    @Transient
    val id: Id<Label> = title[GERMAN]!!.toLowerCase().asId()

    object ProtoSerializer : Entity.ProtoSerializer<Label, ProtoLabel, ProtoLabel.Builder>() {
        override fun fromProto(proto: ProtoLabel, context: Context): Label =
            throw UnsupportedOperationException("Labels cannot be created via protobuf")

        override fun toProtoBuilder(entity: Label, context: Context): ProtoLabel.Builder =
            ProtoLabel.newBuilder().builder(entity) {
                title = it.title[context]
                icon = it.iconUri.toString()
            }
    }

    fun matches(label: String, startOnly: Boolean = false, ignoreCase: Boolean = true): Boolean {
        val searchSet = aliases + title.values.values
        return if (startOnly)
            searchSet.any { label.startsWith(it, ignoreCase) }
        else
            searchSet.any { label.equals(it, ignoreCase) }
    }
}

fun ProtoLabel.parse(context: Context): Label =
    Label.ProtoSerializer.fromProto(this, context)

fun Wrapper<Label>.toProto(context: Context): ProtoLabel =
    Label.ProtoSerializer.toProto(this, context)
