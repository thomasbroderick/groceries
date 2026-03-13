package dev.samhain.groceries.agent

import ai.koog.agents.core.tools.SimpleTool
import ai.koog.agents.core.tools.annotations.LLMDescription
import dev.samhain.groceries.dto.AddIngredientRequest
import dev.samhain.groceries.dto.CreateMealRequest
import dev.samhain.groceries.service.MealService
import kotlinx.serialization.Serializable

@Serializable
data class CreateMealWithIngredientsArgs(
    @property:LLMDescription("The name of the meal, e.g. 'Grilled Salmon with Quinoa'")
    val name: String,
    @property:LLMDescription("List of ingredients in raw format: '<quantity> <unit> <name>', e.g. ['2 cups brown rice', '1 lb chicken breast', '3 cloves garlic']")
    val ingredients: List<String>
)

class CreateMealWithIngredientsTool(private val mealService: MealService, private val userId: Long)
    : SimpleTool<CreateMealWithIngredientsArgs>(
        argsSerializer = CreateMealWithIngredientsArgs.serializer(),
        name = "create_meal",
        description = "Creates a new meal with all its ingredients in one call."
    ) {
    override suspend fun execute(args: CreateMealWithIngredientsArgs): String {
        val meal = mealService.createMeal(CreateMealRequest(args.name), userId)
        val added = args.ingredients.map { raw ->
            val ingredient = mealService.addIngredient(meal.id, AddIngredientRequest(raw), userId)
            ingredient.name
        }
        return "Created meal '${meal.name}' with ${added.size} ingredients: ${added.joinToString()}"
    }
}
