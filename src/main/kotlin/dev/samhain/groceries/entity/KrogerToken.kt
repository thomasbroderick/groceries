package dev.samhain.groceries.entity

import jakarta.persistence.*
import java.time.Instant

enum class GrantType { CLIENT, USER }

@Entity
@Table(name = "kroger_tokens")
class KrogerToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 2048)
    var accessToken: String = "",

    @Column(length = 2048)
    var refreshToken: String? = null,

    @Column(nullable = false)
    var expiresAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    var grantType: GrantType = GrantType.CLIENT
)
