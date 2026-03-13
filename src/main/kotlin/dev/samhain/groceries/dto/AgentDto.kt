package dev.samhain.groceries.dto

data class FitnessGoalRequest(
    val dailyCalories: Int? = null,
    val proteinGrams: Int? = null,
    val carbsGrams: Int? = null,
    val fatGrams: Int? = null,
    val dietaryRestrictions: List<String> = emptyList(),
    val cuisinePreferences: List<String> = emptyList(),
    val mealsPerDay: Int = 3,
    val numberOfDays: Int = 7,
    val notes: String? = null
)

data class MealPlanResponse(val summary: String)
