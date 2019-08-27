package de.hpi.cloud.food

data class StaticCanteenData(
    val canteenOpenMensaId: Int,
    val canteenId: String,
    val counterFinder: (OpenMensaMeal) -> String?
) {}
