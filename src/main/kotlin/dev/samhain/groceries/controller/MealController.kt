package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.*
import dev.samhain.groceries.service.MealService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/meals")
class MealController(private val mealService: MealService) {

    @GetMapping
    fun getAllMeals(): List<MealSummaryResponse> = mealService.getAllMeals()

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createMeal(@RequestBody request: CreateMealRequest): MealDetailResponse =
        mealService.createMeal(request)

    @GetMapping("/{id}")
    fun getMeal(@PathVariable id: Long): MealDetailResponse = mealService.getMeal(id)

    @PutMapping("/{id}")
    fun updateMeal(@PathVariable id: Long, @RequestBody request: UpdateMealRequest): MealDetailResponse =
        mealService.updateMeal(id, request)

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteMeal(@PathVariable id: Long) = mealService.deleteMeal(id)

    @GetMapping("/{id}/ingredients")
    fun getIngredients(@PathVariable id: Long): List<IngredientResponse> = mealService.getIngredients(id)

    @PostMapping("/{id}/ingredients")
    @ResponseStatus(HttpStatus.CREATED)
    fun addIngredient(@PathVariable id: Long, @RequestBody request: AddIngredientRequest): IngredientResponse =
        mealService.addIngredient(id, request)

    @GetMapping("/{id}/ingredients/{iid}")
    fun getIngredient(@PathVariable id: Long, @PathVariable iid: Long): IngredientResponse =
        mealService.getIngredient(id, iid)

    @PutMapping("/{id}/ingredients/{iid}")
    fun updateIngredient(
        @PathVariable id: Long,
        @PathVariable iid: Long,
        @RequestBody request: UpdateIngredientRequest
    ): IngredientResponse = mealService.updateIngredient(id, iid, request)

    @DeleteMapping("/{id}/ingredients/{iid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteIngredient(@PathVariable id: Long, @PathVariable iid: Long) =
        mealService.deleteIngredient(id, iid)

    @PostMapping("/consolidate")
    fun consolidate(@RequestBody mealIds: List<Long>): List<ConsolidatedIngredientResponse> =
        mealService.consolidateIngredients(mealIds)

    @PatchMapping("/ingredients/link")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun linkProduct(@RequestBody request: LinkProductRequest) =
        mealService.linkProduct(request)
}
