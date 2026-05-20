package dev.samhain.groceries.controller

import dev.samhain.groceries.agent.MealPlanAgentService
import dev.samhain.groceries.exception.GlobalExceptionHandler
import dev.samhain.groceries.repository.AppUserRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant

@ExtendWith(MockitoExtension::class)
class AgentControllerTest {

    @Mock lateinit var agentService: MealPlanAgentService
    @Mock lateinit var userRepository: AppUserRepository
    lateinit var mockMvc: MockMvc
    lateinit var mockAuth: JwtAuthenticationToken
    private val userId = 1L

    @BeforeEach
    fun setUp() {
        val jwt = Jwt.withTokenValue("test-token")
            .header("alg", "HS256")
            .subject(userId.toString())
            .claim("username", "testuser")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600))
            .build()
        mockAuth = JwtAuthenticationToken(jwt, emptyList())
        SecurityContextHolder.getContext().authentication = mockAuth

        mockMvc = MockMvcBuilders
            .standaloneSetup(AgentController(agentService, userRepository))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `POST api agent meal-plan returns 403 when user lacks access`() {
        whenever(userRepository.existsByIdAndCanUseAiTrue(userId)).thenReturn(false)

        mockMvc.perform(post("/api/agent/meal-plan").principal(mockAuth)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"mealsPerDay":3,"numberOfDays":7}"""))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.error").value("AI meal planning is not enabled for this user"))
    }

    @Test
    fun `POST api agent meal-plan returns generated summary when user has access`() {
        whenever(userRepository.existsByIdAndCanUseAiTrue(userId)).thenReturn(true)
        runBlocking {
            whenever(agentService.generateMealPlan(any(), eq(userId))).thenReturn("Meal plan summary")
        }

        mockMvc.perform(post("/api/agent/meal-plan").principal(mockAuth)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"mealsPerDay":3,"numberOfDays":7}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.summary").value("Meal plan summary"))
    }
}
