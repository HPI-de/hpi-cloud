package de.hpi.cloud.food

data class OpenMensaInformation(
    val id: Int,
    val name: String,
    val city: String,
    val address: String,
    val coordinates: List<Double>
) {}
