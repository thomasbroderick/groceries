package dev.samhain.groceries.entity

import jakarta.persistence.*

@Entity
@Table(name = "meals", uniqueConstraints = [UniqueConstraint(columnNames = ["user_id", "name"])])
class Meal(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    var user: AppUser? = null,

    @OneToMany(mappedBy = "meal", cascade = [CascadeType.ALL], orphanRemoval = true)
    val ingredients: MutableList<Ingredient> = mutableListOf()
)
