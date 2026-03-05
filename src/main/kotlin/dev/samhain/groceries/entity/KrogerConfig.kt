package dev.samhain.groceries.entity

import jakarta.persistence.*

@Entity
@Table(name = "kroger_config")
class KrogerConfig(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true, unique = true)
    var user: AppUser? = null,

    @Column(nullable = false)
    var clientId: String = "",

    @Column(nullable = false)
    var clientSecret: String = "",

    var locationId: String? = null,

    var locationName: String? = null
)
