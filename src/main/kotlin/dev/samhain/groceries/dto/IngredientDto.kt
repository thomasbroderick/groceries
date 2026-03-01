package dev.samhain.groceries.dto

data class IngredientResponse(
    val id: Long,
    val name: String,
    val quantity: String?,
    val krogerProductId: String?,
    val krogerProductName: String?
)

data class AddIngredientRequest(val raw: String)

data class UpdateIngredientRequest(
    val name: String? = null,
    val quantity: String? = null,
    val krogerProductId: String? = null,
    val krogerProductName: String? = null
)

data class ConsolidatedIngredientResponse(
    val name: String,
    val consolidatedQuantity: String?,
    val krogerProductId: String?,
    val krogerProductName: String?
)

data class LinkProductRequest(
    val name: String,
    val mealIds: List<Long>,
    val krogerProductId: String,
    val krogerProductName: String
)
