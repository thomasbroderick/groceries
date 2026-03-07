package dev.samhain.groceries.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "orders")
class Order(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    var user: AppUser? = null,

    @Column(nullable = false)
    val createdAt: Instant = Instant.now(),

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "order_meals", joinColumns = [JoinColumn(name = "order_id")])
    @Column(name = "meal_name")
    val mealNames: MutableList<String> = mutableListOf()
)
