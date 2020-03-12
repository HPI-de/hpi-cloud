package de.hpi.cloud.common

import de.hpi.cloud.common.entity.Id
import de.hpi.cloud.common.entity.asId
import java.util.*

data class Context(
    val author: Id<Party>,
    val languageRanges: List<Locale.LanguageRange>
) {
    companion object {
        fun forInternalService(serviceName: String): Context = Context(
            author = serviceName.asId(),
            languageRanges = listOf()
        )
    }
}
