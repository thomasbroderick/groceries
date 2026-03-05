package dev.samhain.groceries.dto

data class KrogerConfigResponse(
    val clientId: String,
    val locationId: String?,
    val locationName: String?,
    val hasToken: Boolean
)

data class LocationUpdateRequest(
    val locationId: String,
    val locationName: String?
)
