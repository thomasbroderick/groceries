package dev.samhain.groceries.service

import dev.samhain.groceries.dto.CreateMealRequest
import dev.samhain.groceries.entity.AppUser
import dev.samhain.groceries.entity.Ingredient
import dev.samhain.groceries.entity.Meal
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.IngredientRepository
import dev.samhain.groceries.repository.MealRepository
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import java.util.Optional
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

@ExtendWith(MockitoExtension::class)
class MealServiceTest {

    @Mock lateinit var mealRepository: MealRepository
    @Mock lateinit var ingredientRepository: IngredientRepository
    @Mock lateinit var userRepository: AppUserRepository
    @InjectMocks lateinit var mealService: MealService

    private val userId = 1L

    // -------------------------------------------------------------------------
    // parseIngredient
    // -------------------------------------------------------------------------

    @Test
    fun `parseIngredient plain number then name`() {
        val (name, qty) = mealService.parseIngredient("2 onions")
        assertEquals("onions", name)
        assertEquals("2", qty)
    }

    @Test
    fun `parseIngredient number with unit then multi-word name`() {
        val (name, qty) = mealService.parseIngredient("1 lb ground beef")
        assertEquals("ground beef", name)
        assertEquals("1 lb", qty)
    }

    @Test
    fun `parseIngredient non-numeric first token treated as name with null qty`() {
        val (name, qty) = mealService.parseIngredient("2% milk")
        assertEquals("2% milk", name)
        assertNull(qty)
    }

    @Test
    fun `parseIngredient single word with no quantity`() {
        val (name, qty) = mealService.parseIngredient("salt")
        assertEquals("salt", name)
        assertNull(qty)
    }

    @Test
    fun `parseIngredient fractional quantity with unit`() {
        val (name, qty) = mealService.parseIngredient("1/2 cup flour")
        assertEquals("flour", name)
        assertEquals("1/2 cup", qty)
    }

    @Test
    fun `parseIngredient decimal quantity no unit`() {
        val (name, qty) = mealService.parseIngredient("2.5 apples")
        assertEquals("apples", name)
        assertEquals("2.5", qty)
    }

    @Test
    fun `parseIngredient plural unit cups`() {
        val (name, qty) = mealService.parseIngredient("3 cups chicken broth")
        assertEquals("chicken broth", name)
        assertEquals("3 cups", qty)
    }

    @Test
    fun `parseIngredient trims leading and trailing whitespace`() {
        val (name, qty) = mealService.parseIngredient("  4 oz cheddar  ")
        assertEquals("cheddar", name)
        assertEquals("4 oz", qty)
    }

    // -------------------------------------------------------------------------
    // consolidateIngredients
    // -------------------------------------------------------------------------

    @Test
    fun `consolidateIngredients sums integer quantities for same ingredient across meals`() {
        whenever(mealRepository.existsByIdAndUserId(1L, userId)).thenReturn(true)
        whenever(mealRepository.existsByIdAndUserId(2L, userId)).thenReturn(true)
        whenever(ingredientRepository.findByMealId(1L)).thenReturn(listOf(ingredient(1L, 1L, "onions", "2")))
        whenever(ingredientRepository.findByMealId(2L)).thenReturn(listOf(ingredient(2L, 2L, "onions", "3")))

        val result = mealService.consolidateIngredients(listOf(1L, 2L), userId)

        assertEquals(1, result.size)
        assertEquals("onions", result[0].name)
        assertEquals("5", result[0].consolidatedQuantity)
    }

    @Test
    fun `consolidateIngredients joins mixed quantities with plus`() {
        whenever(mealRepository.existsByIdAndUserId(1L, userId)).thenReturn(true)
        whenever(mealRepository.existsByIdAndUserId(2L, userId)).thenReturn(true)
        whenever(ingredientRepository.findByMealId(1L)).thenReturn(listOf(ingredient(1L, 1L, "flour", "1 cup")))
        whenever(ingredientRepository.findByMealId(2L)).thenReturn(listOf(ingredient(2L, 2L, "flour", "2 cups")))

        val result = mealService.consolidateIngredients(listOf(1L, 2L), userId)

        assertEquals("1 cup + 2 cups", result[0].consolidatedQuantity)
    }

    @Test
    fun `consolidateIngredients groups by lowercase trimmed name`() {
        whenever(mealRepository.existsByIdAndUserId(1L, userId)).thenReturn(true)
        whenever(ingredientRepository.findByMealId(1L)).thenReturn(listOf(
            ingredient(1L, 1L, "Onions", "2"),
            ingredient(2L, 1L, " onions ", "3")
        ))

        val result = mealService.consolidateIngredients(listOf(1L), userId)

        assertEquals(1, result.size)
        assertEquals("5", result[0].consolidatedQuantity)
    }

    @Test
    fun `consolidateIngredients keeps last non-null kroger product mapping`() {
        val first = ingredient(1L, 1L, "beef", "1 lb").apply {
            krogerProductId = "upc-old"; krogerProductName = "Old Beef"
        }
        val second = ingredient(2L, 2L, "beef", "2 lb").apply {
            krogerProductId = "upc-new"; krogerProductName = "New Beef"
        }
        whenever(mealRepository.existsByIdAndUserId(1L, userId)).thenReturn(true)
        whenever(mealRepository.existsByIdAndUserId(2L, userId)).thenReturn(true)
        whenever(ingredientRepository.findByMealId(1L)).thenReturn(listOf(first))
        whenever(ingredientRepository.findByMealId(2L)).thenReturn(listOf(second))

        val result = mealService.consolidateIngredients(listOf(1L, 2L), userId)

        assertEquals("upc-new", result[0].krogerProductId)
        assertEquals("New Beef", result[0].krogerProductName)
    }

    @Test
    fun `consolidateIngredients returns null quantity when all quantities are null`() {
        whenever(mealRepository.existsByIdAndUserId(1L, userId)).thenReturn(true)
        whenever(ingredientRepository.findByMealId(1L)).thenReturn(listOf(ingredient(1L, 1L, "salt", null)))

        val result = mealService.consolidateIngredients(listOf(1L), userId)

        assertEquals(1, result.size)
        assertNull(result[0].consolidatedQuantity)
    }

    @Test
    fun `consolidateIngredients keeps distinct ingredients separate`() {
        whenever(mealRepository.existsByIdAndUserId(1L, userId)).thenReturn(true)
        whenever(ingredientRepository.findByMealId(1L)).thenReturn(listOf(
            ingredient(1L, 1L, "garlic", "3"),
            ingredient(2L, 1L, "onions", "1")
        ))

        val result = mealService.consolidateIngredients(listOf(1L), userId)

        assertEquals(2, result.size)
    }

    // -------------------------------------------------------------------------
    // CRUD exception and happy-path cases
    // -------------------------------------------------------------------------

    @Test
    fun `getMeal throws EntityNotFoundException when meal not found`() {
        whenever(mealRepository.findWithIngredientsByIdAndUserId(99L, userId)).thenReturn(Optional.empty())

        assertFailsWith<EntityNotFoundException> { mealService.getMeal(99L, userId) }
    }

    @Test
    fun `deleteMeal throws EntityNotFoundException when meal not found`() {
        whenever(mealRepository.existsByIdAndUserId(99L, userId)).thenReturn(false)

        assertFailsWith<EntityNotFoundException> { mealService.deleteMeal(99L, userId) }
    }

    @Test
    fun `getIngredients throws EntityNotFoundException when meal not found`() {
        whenever(mealRepository.existsByIdAndUserId(99L, userId)).thenReturn(false)

        assertFailsWith<EntityNotFoundException> { mealService.getIngredients(99L, userId) }
    }

    @Test
    fun `createMeal saves and returns detail response with empty ingredients`() {
        val saved = Meal(id = 1L, name = "Pasta")
        whenever(userRepository.getReferenceById(userId)).thenReturn(AppUser(id = userId, username = "test"))
        whenever(mealRepository.save(any())).thenReturn(saved)

        val result = mealService.createMeal(CreateMealRequest("Pasta"), userId)

        assertEquals(1L, result.id)
        assertEquals("Pasta", result.name)
        assertEquals(emptyList(), result.ingredients)
    }

    @Test
    fun `addIngredient parses raw string and saves ingredient`() {
        val meal = Meal(id = 1L, name = "Pasta")
        val saved = Ingredient(id = 10L, meal = meal, name = "onions", quantity = "2")
        whenever(mealRepository.findByIdAndUserId(1L, userId)).thenReturn(Optional.of(meal))
        whenever(ingredientRepository.save(any())).thenReturn(saved)

        val result = mealService.addIngredient(1L, dev.samhain.groceries.dto.AddIngredientRequest("2 onions"), userId)

        assertEquals("onions", result.name)
        assertEquals("2", result.quantity)
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun ingredient(id: Long, mealId: Long, name: String, quantity: String?) =
        Ingredient(id = id, meal = Meal(id = mealId, name = "Meal$mealId"), name = name, quantity = quantity)
}
