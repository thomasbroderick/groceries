package dev.samhain.groceries.service

import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.entity.KrogerToken
import dev.samhain.groceries.entity.OAuthPkceState
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import dev.samhain.groceries.repository.OAuthPkceStateRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class KrogerAuthServiceTest {

    @Mock lateinit var krogerConfigRepository: KrogerConfigRepository
    @Mock lateinit var krogerTokenRepository: KrogerTokenRepository
    @Mock lateinit var pkceStateRepository: OAuthPkceStateRepository
    @Mock lateinit var userRepository: AppUserRepository
    @Mock lateinit var restClient: RestClient

    lateinit var krogerAuthService: KrogerAuthService

    private val userId = 1L

    @BeforeEach
    fun setUp() {
        krogerAuthService = KrogerAuthService(
            krogerConfigRepository,
            krogerTokenRepository,
            pkceStateRepository,
            userRepository,
            restClient,
            "http://localhost:8080/api/kroger/auth/callback"
        )
    }

    // -------------------------------------------------------------------------
    // getValidClientToken
    // -------------------------------------------------------------------------

    @Test
    fun `getValidClientToken returns cached token when not yet expired`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec"))
        )
        val valid = KrogerToken(id = 1L, accessToken = "cached-token",
            expiresAt = Instant.now().plusSeconds(300), grantType = GrantType.CLIENT)
        whenever(krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.CLIENT)).thenReturn(Optional.of(valid))

        val result = krogerAuthService.getValidClientToken(userId)

        assertEquals("cached-token", result)
    }

    @Test
    fun `getValidClientToken throws IllegalStateException when Kroger not configured`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(Optional.empty())

        assertFailsWith<IllegalStateException> { krogerAuthService.getValidClientToken(userId) }
    }

    @Test
    fun `getValidClientToken treats token expiring within 60s as expired`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec"))
        )
        val almostExpired = KrogerToken(id = 1L, accessToken = "stale-token",
            expiresAt = Instant.now().plusSeconds(30), grantType = GrantType.CLIENT)
        whenever(krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.CLIENT)).thenReturn(Optional.of(almostExpired))
        assertFailsWith<Exception> { krogerAuthService.getValidClientToken(userId) }
    }

    // -------------------------------------------------------------------------
    // getValidUserToken
    // -------------------------------------------------------------------------

    @Test
    fun `getValidUserToken returns cached user token when not yet expired`() {
        val valid = KrogerToken(id = 2L, accessToken = "user-token",
            expiresAt = Instant.now().plusSeconds(600), grantType = GrantType.USER)
        whenever(krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.USER)).thenReturn(Optional.of(valid))

        val result = krogerAuthService.getValidUserToken(userId)

        assertEquals("user-token", result)
    }

    @Test
    fun `getValidUserToken throws when no user token stored`() {
        whenever(krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.USER)).thenReturn(Optional.empty())

        val ex = assertFailsWith<IllegalStateException> { krogerAuthService.getValidUserToken(userId) }
        assertTrue(ex.message!!.contains("not authenticated"))
    }

    @Test
    fun `getValidUserToken throws when user token expired and no refresh token`() {
        val expired = KrogerToken(id = 2L, accessToken = "old-token", refreshToken = null,
            expiresAt = Instant.now().minusSeconds(60), grantType = GrantType.USER)
        whenever(krogerTokenRepository.findByUserIdAndGrantType(userId, GrantType.USER)).thenReturn(Optional.of(expired))

        val ex = assertFailsWith<IllegalStateException> { krogerAuthService.getValidUserToken(userId) }
        assertTrue(ex.message!!.contains("refresh token"))
    }

    // -------------------------------------------------------------------------
    // generateAuthUrl
    // -------------------------------------------------------------------------

    @Test
    fun `generateAuthUrl throws when Kroger not configured`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(Optional.empty())

        assertFailsWith<IllegalStateException> { krogerAuthService.generateAuthUrl(userId) }
    }

    @Test
    fun `generateAuthUrl returns URL containing required PKCE and OAuth parameters`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "my-client", clientSecret = "sec"))
        )

        val response = krogerAuthService.generateAuthUrl(userId)

        assertNotNull(response.state)
        assertTrue(response.authorizationUrl.contains("client_id=my-client"))
        assertTrue(response.authorizationUrl.contains("response_type=code"))
        assertTrue(response.authorizationUrl.contains("code_challenge_method=S256"))
        assertTrue(response.authorizationUrl.contains("state=${response.state}"))
        assertTrue(response.authorizationUrl.contains("code_challenge="))
    }

    @Test
    fun `generateAuthUrl produces unique state on each call`() {
        whenever(krogerConfigRepository.findByUserId(userId)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "cid", clientSecret = "sec"))
        )

        val first = krogerAuthService.generateAuthUrl(userId)
        val second = krogerAuthService.generateAuthUrl(userId)

        assertTrue(first.state != second.state, "Each call must produce a distinct state token")
    }

    // -------------------------------------------------------------------------
    // handleCallback
    // -------------------------------------------------------------------------

    @Test
    fun `handleCallback throws IllegalArgumentException when state not found`() {
        whenever(pkceStateRepository.findById("bad-state")).thenReturn(Optional.empty())

        val ex = assertFailsWith<IllegalArgumentException> {
            krogerAuthService.handleCallback("auth-code", "bad-state")
        }
        assertTrue(ex.message!!.contains("Invalid") || ex.message!!.contains("expired"))
    }

    @Test
    fun `handleCallback throws IllegalArgumentException when state is expired`() {
        val expiredState = OAuthPkceState(
            state = "old-state",
            codeVerifier = "verifier",
            expiresAt = Instant.now().minusSeconds(60),
            userId = userId
        )
        whenever(pkceStateRepository.findById("old-state")).thenReturn(Optional.of(expiredState))

        assertFailsWith<IllegalArgumentException> {
            krogerAuthService.handleCallback("code", "old-state")
        }
    }
}
