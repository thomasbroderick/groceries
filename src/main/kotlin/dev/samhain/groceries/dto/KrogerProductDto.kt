package dev.samhain.groceries.dto

data class KrogerProductResponse(
    val productId: String,
    val description: String,
    val upc: String?,
    val price: Double?,
    val imageUrl: String?
)

data class ProductSearchRequest(
    val term: String,
    val locationId: String? = null
)
