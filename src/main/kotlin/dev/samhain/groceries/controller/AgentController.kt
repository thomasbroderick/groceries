package dev.samhain.groceries.controller

import dev.samhain.groceries.agent.MealPlanAgentService
import dev.samhain.groceries.dto.FitnessGoalRequest
import dev.samhain.groceries.dto.MealPlanResponse
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agent")
class AgentController(private val agentService: MealPlanAgentService) {

    @PostMapping("/meal-plan")
    fun generateMealPlan(
        @RequestBody goals: FitnessGoalRequest,
        authentication: Authentication
    ): MealPlanResponse {
        val userId = authentication.name.toLong()
        val summary = runBlocking { agentService.generateMealPlan(goals, userId) }
        return MealPlanResponse(summary)
    }
}
