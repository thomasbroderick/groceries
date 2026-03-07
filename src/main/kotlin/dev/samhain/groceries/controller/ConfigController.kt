package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.KrogerConfigResponse
import dev.samhain.groceries.dto.LocationUpdateRequest
import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import dev.samhain.groceries.service.KrogerAuthService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/api/config")
class ConfigController(
    private val krogerConfigRepository: KrogerConfigRepository,
    private val krogerTokenRepository: KrogerTokenRepository,
    private val userRepository: AppUserRepository,
    private val krogerAuthService: KrogerAuthService
) {

    private fun hasUserToken(userId: Long): Boolean {
        val token = krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.USER).orElse(null)
            ?: return false
        // Access token still fresh — no need to hit Kroger
        if (token.expiresAt.isAfter(Instant.now().plusSeconds(60))) return true
        // Access token expired — proactively attempt refresh so a stale token is detected now
        return try {
            krogerAuthService.getValidUserToken(userId)
            true
        } catch (e: Exception) {
            false
        }
    }

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
