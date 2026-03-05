package dev.samhain.groceries.service

import dev.samhain.groceries.dto.*
import dev.samhain.groceries.entity.Ingredient
import dev.samhain.groceries.entity.Meal
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.IngredientRepository
import dev.samhain.groceries.repository.MealRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class MealService(
    private val mealRepository: MealRepository,
    private val ingredientRepository: IngredientRepository,
    private val userRepository: AppUserRepository
) {

    @Transactional(readOnly = true)
    fun getAllMeals(userId: Long): List<MealSummaryResponse> =
        mealRepository.findAllByUserId(userId).map { MealSummaryResponse(it.id, it.name, it.ingredients.size) }

    fun createMeal(request: CreateMealRequest, userId: Long): MealDetailResponse {
        val meal = mealRepository.save(Meal(name = request.name, user = userRepository.getReferenceById(userId)))
        return MealDetailResponse(meal.id, meal.name, emptyList())
    }

    @Transactional(readOnly = true)
    fun getMeal(id: Long, userId: Long): MealDetailResponse =
        mealRepository.findWithIngredientsByIdAndUserId(id, userId)
            .orElseThrow { EntityNotFoundException("Meal not found: $id") }
            .toDetailResponse()

    fun updateMeal(id: Long, request: UpdateMealRequest, userId: Long): MealDetailResponse {
        val meal = mealRepository.findWithIngredientsByIdAndUserId(id, userId)
            .orElseThrow { EntityNotFoundException("Meal not found: $id") }
        meal.name = request.name
        return mealRepository.save(meal).toDetailResponse()
    }

    fun deleteMeal(id: Long, userId: Long) {
        if (!mealRepository.existsByIdAndUserId(id, userId)) throw EntityNotFoundException("Meal not found: $id")
        mealRepository.deleteById(id)
    }

    @Transactional(readOnly = true)
    fun getIngredients(mealId: Long, userId: Long): List<IngredientResponse> {
        if (!mealRepository.existsByIdAndUserId(mealId, userId)) throw EntityNotFoundException("Meal not found: $mealId")
        return ingredientRepository.findByMealId(mealId).map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun getIngredient(mealId: Long, ingredientId: Long, userId: Long): IngredientResponse {
        if (!mealRepository.existsByIdAndUserId(mealId, userId)) throw EntityNotFoundException("Meal not found: $mealId")
        val ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow { EntityNotFoundException("Ingredient not found: $ingredientId") }
        if (ingredient.meal?.id != mealId) throw EntityNotFoundException("Ingredient $ingredientId not in meal $mealId")
        return ingredient.toResponse()
    }

    fun addIngredient(mealId: Long, request: AddIngredientRequest, userId: Long): IngredientResponse {
        val meal = mealRepository.findByIdAndUserId(mealId, userId)
            .orElseThrow { EntityNotFoundException("Meal not found: $mealId") }
        val (name, quantity) = parseIngredient(request.raw)
        return ingredientRepository.save(Ingredient(meal = meal, name = name, quantity = quantity)).toResponse()
    }

    fun updateIngredient(mealId: Long, ingredientId: Long, request: UpdateIngredientRequest, userId: Long): IngredientResponse {
        if (!mealRepository.existsByIdAndUserId(mealId, userId)) throw EntityNotFoundException("Meal not found: $mealId")
        val ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow { EntityNotFoundException("Ingredient not found: $ingredientId") }
        if (ingredient.meal?.id != mealId) throw EntityNotFoundException("Ingredient $ingredientId not in meal $mealId")
        request.name?.let { ingredient.name = it }
        request.quantity?.let { ingredient.quantity = it }
        request.krogerProductId?.let { ingredient.krogerProductId = it }
        request.krogerProductName?.let { ingredient.krogerProductName = it }
        return ingredientRepository.save(ingredient).toResponse()
    }

    fun deleteIngredient(mealId: Long, ingredientId: Long, userId: Long) {
        if (!mealRepository.existsByIdAndUserId(mealId, userId)) throw EntityNotFoundException("Meal not found: $mealId")
        val ingredient = ingredientRepository.findById(ingredientId)
            .orElseThrow { EntityNotFoundException("Ingredient not found: $ingredientId") }
        if (ingredient.meal?.id != mealId) throw EntityNotFoundException("Ingredient $ingredientId not in meal $mealId")
        ingredientRepository.deleteById(ingredientId)
    }

    @Transactional(readOnly = true)
    fun consolidateIngredients(mealIds: List<Long>, userId: Long): List<ConsolidatedIngredientResponse> {
        data class Acc(
            val quantities: MutableList<String?> = mutableListOf(),
            var krogerProductId: String? = null,
            var krogerProductName: String? = null
        )

        val grouped = mutableMapOf<String, Acc>()
        for (mealId in mealIds) {
            if (!mealRepository.existsByIdAndUserId(mealId, userId)) throw EntityNotFoundException("Meal not found: $mealId")
            for (ingredient in ingredientRepository.findByMealId(mealId)) {
                val key = ingredient.name.trim().lowercase()
                val acc = grouped.getOrPut(key) { Acc() }
                acc.quantities.add(ingredient.quantity)
                if (ingredient.krogerProductId != null) {
                    acc.krogerProductId = ingredient.krogerProductId
                    acc.krogerProductName = ingredient.krogerProductName
                }
            }
        }

        return grouped.map { (name, acc) ->
            val nonNull = acc.quantities.filterNotNull()
            val combinedQty = when {
                nonNull.isEmpty() -> null
                nonNull.all { it.matches(Regex("^\\d+$")) } -> nonNull.sumOf { it.toInt() }.toString()
                else -> nonNull.joinToString(" + ")
            }
            ConsolidatedIngredientResponse(
                name = name,
                consolidatedQuantity = combinedQty,
                krogerProductId = acc.krogerProductId,
                krogerProductName = acc.krogerProductName
            )
        }
    }

    fun linkProduct(request: LinkProductRequest, userId: Long) {
        val nameKey = request.name.trim().lowercase()
        for (mealId in request.mealIds) {
            if (!mealRepository.existsByIdAndUserId(mealId, userId)) throw EntityNotFoundException("Meal not found: $mealId")
            ingredientRepository.findByMealId(mealId)
                .filter { it.name.trim().lowercase() == nameKey }
                .forEach {
                    it.krogerProductId = request.krogerProductId
                    it.krogerProductName = request.krogerProductName
                    ingredientRepository.save(it)
                }
        }
    }

    fun parseIngredient(raw: String): Pair<String, String?> {
        val parts = raw.trim().split(Regex("\\s+"))
        if (parts.isEmpty()) return Pair(raw.trim(), null)

        val firstToken = parts[0]
        if (!firstToken.matches(Regex("^\\d[\\d./]*$"))) return Pair(raw.trim(), null)

        val units = setOf(
            "lb", "lbs", "oz", "g", "kg", "cup", "cups", "tsp", "tbsp",
            "ml", "l", "liter", "litre", "gallon", "gallons", "quart", "quarts",
            "pint", "pints", "fl", "bunch", "clove", "cloves", "slice", "slices",
            "can", "cans", "pkg", "package", "packages"
        )

        return if (parts.size >= 3 && parts[1].lowercase() in units) {
            Pair(parts.drop(2).joinToString(" "), "${parts[0]} ${parts[1]}")
        } else if (parts.size >= 2) {
            Pair(parts.drop(1).joinToString(" "), parts[0])
        } else {
            Pair(raw.trim(), null)
        }
    }

    private fun Meal.toDetailResponse() = MealDetailResponse(id, name, ingredients.map { it.toResponse() })

    private fun Ingredient.toResponse() =
        IngredientResponse(id, name, quantity, krogerProductId, krogerProductName)
}
