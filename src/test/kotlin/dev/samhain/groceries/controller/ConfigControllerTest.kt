package dev.samhain.groceries.controller

import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.exception.GlobalExceptionHandler
import dev.samhain.groceries.repository.KrogerConfigRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ConfigControllerTest {

    @Mock lateinit var krogerConfigRepository: KrogerConfigRepository
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(ConfigController(krogerConfigRepository))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `GET api config kroger returns clientId but never clientSecret`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "my-client-id", clientSecret = "top-secret"))
        )

        mockMvc.perform(get("/api/config/kroger"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value("my-client-id"))
            .andExpect(jsonPath("$.clientSecret").doesNotExist())
    }

    @Test
    fun `GET api config kroger when not configured returns empty clientId`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.empty())

        mockMvc.perform(get("/api/config/kroger"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value(""))
            .andExpect(jsonPath("$.clientSecret").doesNotExist())
    }

    @Test
    fun `PUT api config kroger inserts new config and returns without secret`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.empty())
        whenever(krogerConfigRepository.save(any())).thenReturn(
            KrogerConfig(id = 1L, clientId = "new-id", clientSecret = "new-secret")
        )

        mockMvc.perform(put("/api/config/kroger")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"clientId":"new-id","clientSecret":"new-secret"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value("new-id"))
            .andExpect(jsonPath("$.clientSecret").doesNotExist())
    }

    @Test
    fun `PUT api config kroger updates existing config and returns locationId`() {
        val existing = KrogerConfig(id = 1L, clientId = "old-id", clientSecret = "old-secret")
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.of(existing))
        whenever(krogerConfigRepository.save(any())).thenReturn(
            KrogerConfig(id = 1L, clientId = "updated-id", clientSecret = "updated-secret", locationId = "loc-123")
        )

        mockMvc.perform(put("/api/config/kroger")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"clientId":"updated-id","clientSecret":"updated-secret","locationId":"loc-123"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.clientId").value("updated-id"))
            .andExpect(jsonPath("$.locationId").value("loc-123"))
            .andExpect(jsonPath("$.clientSecret").doesNotExist())
    }

    @Test
    fun `PATCH api config kroger location updates locationId`() {
        val existing = KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec")
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.of(existing))
        whenever(krogerConfigRepository.save(any())).thenReturn(
            KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec", locationId = "store-42")
        )

        mockMvc.perform(patch("/api/config/kroger/location").param("locationId", "store-42"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.locationId").value("store-42"))
    }

    @Test
    fun `PATCH api config kroger location when not configured returns 400`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.empty())

        mockMvc.perform(patch("/api/config/kroger/location").param("locationId", "store-99"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("Kroger config not set"))
    }
}
