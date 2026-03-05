package dev.samhain.groceries.entity

import jakarta.persistence.*
import java.time.Instant

enum class GrantType { CLIENT, USER }

@Entity
@Table(name = "kroger_tokens", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "grant_type"])])
class KrogerToken(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    var user: AppUser? = null,

    @Column(nullable = false, length = 2048)
    var accessToken: String = "",

    @Column(length = 2048)
    var refreshToken: String? = null,

    @Column(nullable = false)
    var expiresAt: Instant = Instant.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var grantType: GrantType = GrantType.CLIENT
)
