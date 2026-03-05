package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.*
import dev.samhain.groceries.service.MealService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/meals")
class MealController(private val mealService: MealService) {

    @GetMapping
    fun getAllMeals(auth: Authentication): List<MealSummaryResponse> =
        mealService.getAllMeals(auth.name.toLong())

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMeal(@RequestBody request: CreateMealRequest, auth: Authentication): MealDetailResponse =
        mealService.createMeal(request, auth.name.toLong())

    @GetMapping("/{id}")
    fun getMeal(@PathVariable id: Long, auth: Authentication): MealDetailResponse =
        mealService.getMeal(id, auth.name.toLong())

    @PutMapping("/{id}")
    fun updateMeal(@PathVariable id: Long, @RequestBody request: UpdateMealRequest, auth: Authentication): MealDetailResponse =
        mealService.updateMeal(id, request, auth.name.toLong())

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMeal(@PathVariable id: Long, auth: Authentication) =
        mealService.deleteMeal(id, auth.name.toLong())

    @GetMapping("/{id}/ingredients")
    fun getIngredients(@PathVariable id: Long, auth: Authentication): List<IngredientResponse> =
        mealService.getIngredients(id, auth.name.toLong())

    @PostMapping("/{id}/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    fun addIngredient(@PathVariable id: Long, @RequestBody request: AddIngredientRequest, auth: Authentication): IngredientResponse =
        mealService.addIngredient(id, request, auth.name.toLong())

    @GetMapping("/{id}/ingredients/{iid}")
    fun getIngredient(@PathVariable id: Long, @PathVariable iid: Long, auth: Authentication): IngredientResponse =
        mealService.getIngredient(id, iid, auth.name.toLong())

    @PutMapping("/{id}/ingredients/{iid}")
    fun updateIngredient(
        @PathVariable id: Long,
        @PathVariable iid: Long,
        @RequestBody request: UpdateIngredientRequest,
        auth: Authentication
    ): IngredientResponse = mealService.updateIngredient(id, iid, request, auth.name.toLong())

    @DeleteMapping("/{id}/ingredients/{iid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteIngredient(@PathVariable id: Long, @PathVariable iid: Long, auth: Authentication) =
        mealService.deleteIngredient(id, iid, auth.name.toLong())

    @PostMapping("/consolidate")
    fun consolidate(@RequestBody mealIds: List<Long>, auth: Authentication): List<ConsolidatedIngredientResponse> =
        mealService.consolidateIngredients(mealIds, auth.name.toLong())

    @PatchMapping("/ingredients/link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun linkProduct(@RequestBody request: LinkProductRequest, auth: Authentication) =
        mealService.linkProduct(request, auth.name.toLong())
}
