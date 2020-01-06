package de.hpi.cloud.common

import de.hpi.cloud.common.entity.Id
import java.util.*

data class Context(
    val author: Id<Party>,
    val languageRanges: List<Locale.LanguageRange>
)
