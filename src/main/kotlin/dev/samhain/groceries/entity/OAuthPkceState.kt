package dev.samhain.groceries.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "oauth_pkce_state")
class OAuthPkceState(
    @Id
    val state: String = "",

    @Column(nullable = false, length = 512)
    val codeVerifier: String = "",

    @Column(nullable = false)
    val expiresAt: Instant = Instant.now(),

    @Column(nullable = false)
    val userId: Long = 0
)
