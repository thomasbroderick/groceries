package dev.samhain.groceries.dto

data class MealSummaryResponse(
    val id: Long,
    val name: String,
    val ingredientCount: Int
)

data class MealDetailResponse(
    val id: Long,
    val name: String,
    val ingredients: List<IngredientResponse>
)

data class CreateMealRequest(val name: String)

data class UpdateMealRequest(val name: String)
