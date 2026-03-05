package dev.samhain.groceries.entity

import jakarta.persistence.*

@Entity
@Table(name = "app_users")
class AppUser(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var username: String = "",

    @Column(nullable = false)
    var passwordHash: String = ""
)
