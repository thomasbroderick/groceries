package dev.samhain.groceries.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.singleRunStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleLLMClient
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import dev.samhain.groceries.dto.FitnessGoalRequest
import dev.samhain.groceries.service.MealService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

private val Gemini3_1FlashLitePreview = LLModel(
    provider = LLMProvider.Google,
    id = "gemini-3.1-flash-lite-preview",
    capabilities = listOf(
        LLMCapability.Temperature,
        LLMCapability.Completion,
        LLMCapability.MultipleChoices,
        LLMCapability.Vision.Image,
        LLMCapability.Vision.Video,
        LLMCapability.Audio,
        LLMCapability.Tools,
        LLMCapability.ToolChoice,
        LLMCapability.Schema.JSON.Basic,
        LLMCapability.Schema.JSON.Standard,
    ),
    contextLength = 1_048_576,
    maxOutputTokens = 65_536,
)

@Service
class MealPlanAgentService(
    private val mealService: MealService,
    @Value("\${google.api.key}") private val googleApiKey: String
) {
    private val systemPrompt = """
        You are a nutritional AI assistant. Given the user's fitness goals, create a set of
        personalized meal recipes by calling the provided tools.

        For each meal, call create_meal with the meal name and all its ingredients at once.
        Use realistic quantities (e.g. "6 oz salmon", "1 cup quinoa", "2 tbsp olive oil").

        After creating all meals, return a concise summary of the meal plan including
        estimated macros per meal and how the set meets the user's goals.
    """.trimIndent()

    suspend fun generateMealPlan(goals: FitnessGoalRequest, userId: Long): String {
        val executor = SingleLLMPromptExecutor(GoogleLLMClient(googleApiKey))
        val agentConfig = AIAgentConfig(
            prompt = prompt("meal-plan") {
                system(systemPrompt)
            },
            model = Gemini3_1FlashLitePreview,
            maxAgentIterations = 200
        )
        val agent = AIAgent(
            promptExecutor = executor,
            strategy = singleRunStrategy(),
            agentConfig = agentConfig,
            toolRegistry = ToolRegistry {
                tool(CreateMealWithIngredientsTool(mealService, userId))
            }
        )
        return agent.run(goals.toPrompt())
    }
}

private fun FitnessGoalRequest.toPrompt(): String = buildString {
    val totalMeals = mealsPerDay * numberOfDays
    appendLine("Please create $totalMeals unique meal recipes for a $numberOfDays-day plan with $mealsPerDay meals per day.")
    dailyCalories?.let { appendLine("Daily calories: $it kcal") }
    proteinGrams?.let { appendLine("Daily protein: ${it}g") }
    carbsGrams?.let { appendLine("Daily carbs: ${it}g") }
    fatGrams?.let { appendLine("Daily fat: ${it}g") }
    if (dietaryRestrictions.isNotEmpty()) appendLine("Dietary restrictions: ${dietaryRestrictions.joinToString()}")
    if (cuisinePreferences.isNotEmpty()) appendLine("Cuisine preferences: ${cuisinePreferences.joinToString()}")
    notes?.let { appendLine("Additional notes: $it") }
}
