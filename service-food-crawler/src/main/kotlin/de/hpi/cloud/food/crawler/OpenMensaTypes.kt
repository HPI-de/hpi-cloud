package de.hpi.cloud.food.crawler

data class OpenMensaMeal(
    val id: Int,
    val name: String,
    val category: String,
    val prices: Map<String, Double>,
    val notes: List<String>
) {
    fun categoryMatches(string: String) = category.startsWith(string, ignoreCase = true)
}

data class CanteenData(
    val id: String,
    val openMensaId: Int,
    val counterFinder: (OpenMensaMeal) -> String? = { null }
)
