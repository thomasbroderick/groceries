package dev.samhain.groceries.controller

import dev.samhain.groceries.agent.MealPlanAgentService
import dev.samhain.groceries.dto.FitnessGoalRequest
import dev.samhain.groceries.dto.MealPlanResponse
import dev.samhain.groceries.repository.AppUserRepository
import kotlinx.coroutines.runBlocking
import org.springframework.security.core.Authentication
import org.springframework.security.access.AccessDeniedException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/agent")
class AgentController(
    private val agentService: MealPlanAgentService,
    private val userRepository: AppUserRepository
) {

    @PostMapping("/meal-plan")
    fun generateMealPlan(
        @RequestBody goals: FitnessGoalRequest,
        authentication: Authentication
    ): MealPlanResponse {
        val userId = authentication.name.toLong()
        if (!userRepository.existsByIdAndCanUseAiTrue(userId)) {
            throw AccessDeniedException("AI meal planning is not enabled for this user")
        }
        val summary = runBlocking { agentService.generateMealPlan(goals, userId) }
        return MealPlanResponse(summary)
    }
}
