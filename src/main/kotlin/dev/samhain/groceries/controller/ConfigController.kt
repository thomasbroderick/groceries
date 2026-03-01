package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.KrogerConfigRequest
import dev.samhain.groceries.dto.KrogerConfigResponse
import dev.samhain.groceries.dto.LocationUpdateRequest
import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/config")
class ConfigController(
    private val krogerConfigRepository: KrogerConfigRepository,
    private val krogerTokenRepository: KrogerTokenRepository
) {

    private fun hasUserToken(): Boolean =
        krogerTokenRepository.findByGrantType(GrantType.USER).isPresent

    private fun KrogerConfig.toResponse() = KrogerConfigResponse(
        clientId = clientId,
        locationId = locationId,
        locationName = locationName,
        hasToken = hasUserToken()
    )

    @GetMapping("/kroger")
    fun getKrogerConfig(): KrogerConfigResponse {
        val config = krogerConfigRepository.findById(1L).orElse(null)
            ?: return KrogerConfigResponse(clientId = "", locationId = null, locationName = null, hasToken = hasUserToken())
        return config.toResponse()
    }

    @PutMapping("/kroger")
    fun upsertKrogerConfig(@RequestBody request: KrogerConfigRequest): KrogerConfigResponse {
        val existing = krogerConfigRepository.findById(1L).orElse(null)
        val config = if (existing != null) {
            existing.clientId = request.clientId
            existing.clientSecret = request.clientSecret
            existing.locationId = request.locationId
            existing
        } else {
            KrogerConfig(id = 1L, clientId = request.clientId, clientSecret = request.clientSecret, locationId = request.locationId)
        }
        return krogerConfigRepository.save(config).toResponse()
    }

    @PatchMapping("/kroger/location")
    fun updateLocation(@RequestBody request: LocationUpdateRequest): KrogerConfigResponse {
        val config = krogerConfigRepository.findById(1L)
            .orElseThrow { IllegalArgumentException("Kroger config not set") }
        config.locationId = request.locationId
        config.locationName = request.locationName
        return krogerConfigRepository.save(config).toResponse()
    }
}
