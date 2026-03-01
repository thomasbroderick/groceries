package dev.samhain.groceries.service

import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerConfig
import dev.samhain.groceries.entity.KrogerToken
import dev.samhain.groceries.entity.OAuthPkceState
import dev.samhain.groceries.repository.KrogerConfigRepository
import dev.samhain.groceries.repository.KrogerTokenRepository
import dev.samhain.groceries.repository.OAuthPkceStateRepository
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
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
    @Mock lateinit var restClient: RestClient
    @InjectMocks lateinit var krogerAuthService: KrogerAuthService

    // -------------------------------------------------------------------------
    // getValidClientToken
    // -------------------------------------------------------------------------

    @Test
    fun `getValidClientToken returns cached token when not yet expired`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec"))
        )
        val valid = KrogerToken(id = 1L, accessToken = "cached-token",
            expiresAt = Instant.now().plusSeconds(300), grantType = GrantType.CLIENT)
        whenever(krogerTokenRepository.findByGrantType(GrantType.CLIENT)).thenReturn(Optional.of(valid))

        val result = krogerAuthService.getValidClientToken()

        assertEquals("cached-token", result)
        // RestClient is never touched — no stubbing needed, confirming no outbound call
    }

    @Test
    fun `getValidClientToken throws IllegalStateException when Kroger not configured`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.empty())

        assertFailsWith<IllegalStateException> { krogerAuthService.getValidClientToken() }
    }

    @Test
    fun `getValidClientToken treats token expiring within 60s as expired`() {
        // Token expires in 30s — within the 60s buffer, so a new fetch is attempted.
        // We don't stub RestClient, so it will throw — that's fine; we just confirm
        // the cached token path was NOT taken (no short-circuit return).
        whenever(krogerConfigRepository.findById(1L)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "cid", clientSecret = "csec"))
        )
        val almostExpired = KrogerToken(id = 1L, accessToken = "stale-token",
            expiresAt = Instant.now().plusSeconds(30), grantType = GrantType.CLIENT)
        whenever(krogerTokenRepository.findByGrantType(GrantType.CLIENT)).thenReturn(Optional.of(almostExpired))
        // No RestClient stub → calling restClient.post() returns a mock that will
        // eventually NPE or throw, confirming we attempted a refresh.
        assertFailsWith<Exception> { krogerAuthService.getValidClientToken() }
    }

    // -------------------------------------------------------------------------
    // getValidUserToken
    // -------------------------------------------------------------------------

    @Test
    fun `getValidUserToken returns cached user token when not yet expired`() {
        val valid = KrogerToken(id = 2L, accessToken = "user-token",
            expiresAt = Instant.now().plusSeconds(600), grantType = GrantType.USER)
        whenever(krogerTokenRepository.findByGrantType(GrantType.USER)).thenReturn(Optional.of(valid))

        val result = krogerAuthService.getValidUserToken()

        assertEquals("user-token", result)
    }

    @Test
    fun `getValidUserToken throws when no user token stored`() {
        whenever(krogerTokenRepository.findByGrantType(GrantType.USER)).thenReturn(Optional.empty())

        val ex = assertFailsWith<IllegalStateException> { krogerAuthService.getValidUserToken() }
        assertTrue(ex.message!!.contains("not authenticated"))
    }

    @Test
    fun `getValidUserToken throws when user token expired and no refresh token`() {
        val expired = KrogerToken(id = 2L, accessToken = "old-token", refreshToken = null,
            expiresAt = Instant.now().minusSeconds(60), grantType = GrantType.USER)
        whenever(krogerTokenRepository.findByGrantType(GrantType.USER)).thenReturn(Optional.of(expired))

        val ex = assertFailsWith<IllegalStateException> { krogerAuthService.getValidUserToken() }
        assertTrue(ex.message!!.contains("refresh token"))
    }

    // -------------------------------------------------------------------------
    // generateAuthUrl
    // -------------------------------------------------------------------------

    @Test
    fun `generateAuthUrl throws when Kroger not configured`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(Optional.empty())

        assertFailsWith<IllegalStateException> { krogerAuthService.generateAuthUrl() }
    }

    @Test
    fun `generateAuthUrl returns URL containing required PKCE and OAuth parameters`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "my-client", clientSecret = "sec"))
        )
        // pkceStateRepository.save() is called but its return value is unused — no stub needed

        val response = krogerAuthService.generateAuthUrl()

        assertNotNull(response.state)
        assertTrue(response.authorizationUrl.contains("client_id=my-client"))
        assertTrue(response.authorizationUrl.contains("response_type=code"))
        assertTrue(response.authorizationUrl.contains("code_challenge_method=S256"))
        assertTrue(response.authorizationUrl.contains("state=${response.state}"))
        assertTrue(response.authorizationUrl.contains("code_challenge="))
    }

    @Test
    fun `generateAuthUrl produces unique state on each call`() {
        whenever(krogerConfigRepository.findById(1L)).thenReturn(
            Optional.of(KrogerConfig(id = 1L, clientId = "cid", clientSecret = "sec"))
        )

        val first = krogerAuthService.generateAuthUrl()
        val second = krogerAuthService.generateAuthUrl()

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
            expiresAt = Instant.now().minusSeconds(60)
        )
        whenever(pkceStateRepository.findById("old-state")).thenReturn(Optional.of(expiredState))

        assertFailsWith<IllegalArgumentException> {
            krogerAuthService.handleCallback("code", "old-state")
        }
    }
}
