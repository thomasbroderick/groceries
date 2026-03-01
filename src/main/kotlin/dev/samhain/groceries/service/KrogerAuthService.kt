package dev.samhain.groceries.service

import dev.samhain.groceries.dto.AuthUrlResponse
import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerToken
import dev.samhain.groceries.entity.OAuthPkceState
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import dev.samhain.groceries.repository.OAuthPkceStateRepository
import org.springframework.http.MediaType
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestClient
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64

@Service
@Transactional
class KrogerAuthService(
    private val krogerConfigRepository: KrogerConfigRepository,
    private val krogerTokenRepository: KrogerTokenRepository,
    private val pkceStateRepository: OAuthPkceStateRepository,
    private val restClient: RestClient
) {
    companion object {
        private const val TOKEN_URL = "https://api.kroger.com/v1/connect/oauth2/token"
        private const val AUTH_BASE_URL = "https://api.kroger.com/v1/connect/oauth2/authorize"
        private const val REDIRECT_URI = "http://localhost:8080/api/kroger/auth/callback"
        private const val CART_SCOPE = "cart.basic:write profile.compact"
        private const val CLIENT_SCOPE = "product.compact"
    }

    fun getValidClientToken(): String {
        val config = krogerConfigRepository.findById(1L)
            .orElseThrow { IllegalStateException("Kroger not configured") }

        val existing = krogerTokenRepository.findByGrantType(GrantType.CLIENT).orElse(null)
        if (existing != null && existing.expiresAt.isAfter(Instant.now().plusSeconds(60))) {
            return existing.accessToken
        }

        return fetchClientToken(config.clientId, config.clientSecret)
    }

    fun getValidUserToken(): String {
        val token = krogerTokenRepository.findByGrantType(GrantType.USER)
            .orElseThrow { IllegalStateException("User not authenticated. Visit /api/kroger/auth/url") }

        if (token.expiresAt.isAfter(Instant.now().plusSeconds(60))) return token.accessToken

        val refreshToken = token.refreshToken
            ?: throw IllegalStateException("User token expired and no refresh token available")
        val config = krogerConfigRepository.findById(1L)
            .orElseThrow { IllegalStateException("Kroger not configured") }

        return refreshUserToken(config.clientId, config.clientSecret, refreshToken, token)
    }

    private fun fetchClientToken(clientId: String, clientSecret: String): String {
        val credentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        val response = restClient.post()
            .uri(TOKEN_URL)
            .header("Authorization", "Basic $credentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("grant_type=client_credentials&scope=$CLIENT_SCOPE")
            .retrieve()
            .body(KrogerTokenApiResponse::class.java)
            ?: throw IllegalStateException("Empty response from Kroger token endpoint")

        val expiresAt = Instant.now().plusSeconds(response.expires_in.toLong())
        val existing = krogerTokenRepository.findByGrantType(GrantType.CLIENT).orElse(null)
        val token = existing?.apply {
            accessToken = response.access_token
            this.expiresAt = expiresAt
        } ?: KrogerToken(accessToken = response.access_token, expiresAt = expiresAt, grantType = GrantType.CLIENT)
        krogerTokenRepository.save(token)
        return response.access_token
    }

    private fun refreshUserToken(
        clientId: String,
        clientSecret: String,
        refreshToken: String,
        existing: KrogerToken
    ): String {
        val credentials = Base64.getEncoder().encodeToString("$clientId:$clientSecret".toByteArray())
        val response = restClient.post()
            .uri(TOKEN_URL)
            .header("Authorization", "Basic $credentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("grant_type=refresh_token&refresh_token=$refreshToken")
            .retrieve()
            .body(KrogerTokenApiResponse::class.java)
            ?: throw IllegalStateException("Empty response from Kroger token refresh")

        existing.accessToken = response.access_token
        existing.expiresAt = Instant.now().plusSeconds(response.expires_in.toLong())
        response.refresh_token?.let { existing.refreshToken = it }
        krogerTokenRepository.save(existing)
        return response.access_token
    }

    fun generateAuthUrl(): AuthUrlResponse {
        val config = krogerConfigRepository.findById(1L)
            .orElseThrow { IllegalStateException("Kroger not configured") }

        val secureRandom = SecureRandom()

        val verifierBytes = ByteArray(96)
        secureRandom.nextBytes(verifierBytes)
        val codeVerifier = Base64.getUrlEncoder().withoutPadding().encodeToString(verifierBytes)

        val challengeBytes = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        val codeChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(challengeBytes)

        val stateBytes = ByteArray(16)
        secureRandom.nextBytes(stateBytes)
        val state = Base64.getUrlEncoder().withoutPadding().encodeToString(stateBytes)

        pkceStateRepository.save(OAuthPkceState(state = state, codeVerifier = codeVerifier, expiresAt = Instant.now().plusSeconds(600)))

        val encodedScope = CART_SCOPE.replace(" ", "%20")
        val authUrl = "$AUTH_BASE_URL?client_id=${config.clientId}" +
            "&redirect_uri=$REDIRECT_URI" +
            "&response_type=code" +
            "&scope=$encodedScope" +
            "&state=$state" +
            "&code_challenge=$codeChallenge" +
            "&code_challenge_method=S256"

        return AuthUrlResponse(authorizationUrl = authUrl, state = state)
    }

    fun handleCallback(code: String, state: String): String {
        val pkceState = pkceStateRepository.findById(state)
            .orElseThrow { IllegalArgumentException("Invalid or expired state") }

        if (pkceState.expiresAt.isBefore(Instant.now())) {
            pkceStateRepository.deleteById(state)
            throw IllegalArgumentException("OAuth state expired")
        }

        val config = krogerConfigRepository.findById(1L)
            .orElseThrow { IllegalStateException("Kroger not configured") }
        val credentials = Base64.getEncoder().encodeToString("${config.clientId}:${config.clientSecret}".toByteArray())

        val response = restClient.post()
            .uri(TOKEN_URL)
            .header("Authorization", "Basic $credentials")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body("grant_type=authorization_code&code=$code&redirect_uri=$REDIRECT_URI&code_verifier=${pkceState.codeVerifier}")
            .retrieve()
            .body(KrogerTokenApiResponse::class.java)
            ?: throw IllegalStateException("Empty response from Kroger authorization code exchange")

        pkceStateRepository.deleteById(state)

        val expiresAt = Instant.now().plusSeconds(response.expires_in.toLong())
        val existing = krogerTokenRepository.findByGrantType(GrantType.USER).orElse(null)
        val token = existing?.apply {
            accessToken = response.access_token
            refreshToken = response.refresh_token
            this.expiresAt = expiresAt
        } ?: KrogerToken(
            accessToken = response.access_token,
            refreshToken = response.refresh_token,
            expiresAt = expiresAt,
            grantType = GrantType.USER
        )
        krogerTokenRepository.save(token)
        return "Authentication successful"
    }

    @Scheduled(fixedDelay = 3_600_000)
    fun cleanupExpiredStates() {
        pkceStateRepository.deleteByExpiresAtBefore(Instant.now())
    }
}

private data class KrogerTokenApiResponse(
    val access_token: String,
    val refresh_token: String?,
    val expires_in: Int,
    val token_type: String
)
