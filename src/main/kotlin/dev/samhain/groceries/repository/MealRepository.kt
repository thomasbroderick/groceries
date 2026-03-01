package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.Meal
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MealRepository : JpaRepository<Meal, Long> {
    fun findByName(name: String): Optional<Meal>

    @EntityGraph(attributePaths = ["ingredients"])
    fun findWithIngredientsById(id: Long): Optional<Meal>

    @EntityGraph(attributePaths = ["ingredients"])
    override fun findAll(): List<Meal>
}
