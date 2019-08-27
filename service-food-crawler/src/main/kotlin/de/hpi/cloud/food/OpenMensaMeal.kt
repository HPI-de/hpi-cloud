package de.hpi.cloud.food

data class OpenMensaMeal(
    val id: Int,
    val name: String,
    val category: String,
    val prices: Map<String, Double>,
    val notes: List<String>
) {
    fun categoryMatches(string: String) = this.category.startsWith(string, ignoreCase = true)
}
