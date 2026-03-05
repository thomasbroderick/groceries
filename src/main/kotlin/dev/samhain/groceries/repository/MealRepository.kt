package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.Meal
import org.springframework.data.jpa.repository.EntityGraph
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface MealRepository : JpaRepository<Meal, Long> {
    @EntityGraph(attributePaths = ["ingredients"])
    fun findAllByUserId(userId: Long): List<Meal>

    fun findByIdAndUserId(id: Long, userId: Long): Optional<Meal>

    @EntityGraph(attributePaths = ["ingredients"])
    fun findWithIngredientsByIdAndUserId(id: Long, userId: Long): Optional<Meal>

    fun existsByIdAndUserId(id: Long, userId: Long): Boolean

    // Kept for backward compatibility
    @EntityGraph(attributePaths = ["ingredients"])
    fun findWithIngredientsById(id: Long): Optional<Meal>
}
