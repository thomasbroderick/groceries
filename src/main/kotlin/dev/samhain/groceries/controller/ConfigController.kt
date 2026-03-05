package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.KrogerConfigRequest
import dev.samhain.groceries.dto.KrogerConfigResponse
import dev.samhain.groceries.dto.LocationUpdateRequest
import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/config")
class ConfigController(
    private val krogerConfigRepository: KrogerConfigRepository,
    private val krogerTokenRepository: KrogerTokenRepository,
    private val userRepository: AppUserRepository
) {

    private fun hasUserToken(userId: Long): Boolean =
        krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.USER).isPresent

    private fun KrogerConfig.toResponse(userId: Long) = KrogerConfigResponse(
        clientId = clientId,
        locationId = locationId,
        locationName = locationName,
        hasToken = hasUserToken(userId)
    )

    @GetMapping("/kroger")
    fun getKrogerConfig(auth: Authentication): KrogerConfigResponse {
        val userId = auth.name.toLong()
        val config = krogerConfigRepository.findByUserId(userId).orElse(null)
            ?: return KrogerConfigResponse(clientId = "", locationId = null, locationName = null, hasToken = hasUserToken(userId))
        return config.toResponse(userId)
    }

    @PutMapping("/kroger")
    fun upsertKrogerConfig(@RequestBody request: KrogerConfigRequest, auth: Authentication): KrogerConfigResponse {
        val userId = auth.name.toLong()
        val existing = krogerConfigRepository.findByUserId(userId).orElse(null)
        val config = if (existing != null) {
            existing.clientId = request.clientId
            existing.clientSecret = request.clientSecret
            existing.locationId = request.locationId
            existing
        } else {
            KrogerConfig(
                clientId = request.clientId,
                clientSecret = request.clientSecret,
                locationId = request.locationId,
                user = userRepository.getReferenceById(userId)
            )
        }
        return krogerConfigRepository.save(config).toResponse(userId)
    }

    @PatchMapping("/kroger/location")
    fun updateLocation(@RequestBody request: LocationUpdateRequest, auth: Authentication): KrogerConfigResponse {
        val userId = auth.name.toLong()
        val config = krogerConfigRepository.findByUserId(userId)
            .orElseThrow { IllegalArgumentException("Kroger config not set") }
        config.locationId = request.locationId
        config.locationName = request.locationName
        return krogerConfigRepository.save(config).toResponse(userId)
    }
}
