package de.hpi.cloud.food.crawler.openmensa

data class OpenMensaMeal(
    val id: Int,
    val name: String,
    val category: String,
    val prices: Map<String, Double>,
    val notes: List<String>
)
