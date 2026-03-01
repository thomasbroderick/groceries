package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.Ingredient
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface IngredientRepository : JpaRepository<Ingredient, Long> {
    fun findByMealId(mealId: Long): List<Ingredient>
    fun findByMealIdAndName(mealId: Long, name: String): Optional<Ingredient>
    fun deleteByMealId(mealId: Long)
}
