package dev.samhain.groceries.controller

import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.exception.GlobalExceptionHandler
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ConfigControllerTest {

    @Mock lateinit var krogerConfigRepository: KrogerConfigRepository
    @Mock lateinit var krogerTokenRepository: KrogerTokenRepository
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
            .standaloneSetup(ConfigController(krogerConfigRepository, krogerTokenRepository, userRepository))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `GET api config kroger returns clientId but never clientSecret`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "my-client-id", clientSecret = "top-secret"))
        )

        mockMvc.perform(get("/api/config/kroger").principal(mockAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value("my-client-id"))
            .andExpect(jsonPath("$.clientSecret").doesNotExist())
    }

    @Test
    fun `GET api config kroger when not configured returns empty clientId`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(Optional.empty())

        mockMvc.perform(get("/api/config/kroger").principal(mockAuth))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value(""))
            .andExpect(jsonPath("$.clientSecret").doesNotExist())
    }

    @Test
    fun `PATCH api config kroger location updates locationId`() {
        val existing = KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec")
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(Optional.of(existing))
        whenever(krogerConfigRepository.save(any())).thenReturn(
            KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec", locationId = "store-42", locationName = "My Store")
        )

        mockMvc.perform(patch("/api/config/kroger/location").principal(mockAuth)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"locationId":"store-42","locationName":"My Store"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locationId").value("store-42"))
    }

    @Test
    fun `PATCH api config kroger location when not configured returns 400`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(Optional.empty())

        mockMvc.perform(patch("/api/config/kroger/location").principal(mockAuth)
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"locationId":"store-99","locationName":null}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Kroger config not set"))
    }
}
