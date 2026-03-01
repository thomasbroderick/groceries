package dev.samhain.groceries.entity

import jakarta.persistence.*

@Entity
@Table(name = "kroger_config")
class KrogerConfig(
    @Id
    val id: Long = 1L,

    @Column(nullable = false)
    var clientId: String = "",

    @Column(nullable = false)
    var clientSecret: String = "",

    var locationId: String? = null,

    var locationName: String? = null
)
