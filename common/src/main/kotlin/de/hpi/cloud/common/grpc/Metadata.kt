package de.hpi.cloud.common.grpc

import io.grpc.Metadata
import java.util.*

private const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
private val KEY_ACCEPT_LANGUAGE = Metadata.Key.of(HEADER_ACCEPT_LANGUAGE, SimpleAsciiMarshaller)

object SimpleAsciiMarshaller : Metadata.AsciiMarshaller<String> {
    override fun toAsciiString(value: String?): String = value ?: "";
    override fun parseAsciiString(serialized: String?): String = serialized ?: "";
}

val Metadata?.preferredLocales: List<Locale.LanguageRange>
    get() {
        return this?.get(KEY_ACCEPT_LANGUAGE)
            ?.let { Locale.LanguageRange.parse(it) }
            ?: emptyList()
    }
