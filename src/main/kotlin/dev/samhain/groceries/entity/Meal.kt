package dev.samhain.groceries.entity

import jakarta.persistence.*

@Entity
@Table(name = "meals")
class Meal(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String = "",

    @OneToMany(mappedBy = "meal", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ingredients: MutableList<Ingredient> = mutableListOf()
)
