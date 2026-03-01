package dev.samhain.groceries.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "ingredients",
    uniqueConstraints = [UniqueConstraint(columnNames = ["meal_id", "name"])]
)
class Ingredient(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_id", nullable = false)
    var meal: Meal? = null,

    @Column(nullable = false)
    var name: String = "",

    var quantity: String? = null,
    var krogerProductId: String? = null,
    var krogerProductName: String? = null
)
