package dev.samhain.groceries.dto

data class KrogerLocationResponse(
    val locationId: String,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?
)

data class LocationSearchRequest(val zipCode: String)
