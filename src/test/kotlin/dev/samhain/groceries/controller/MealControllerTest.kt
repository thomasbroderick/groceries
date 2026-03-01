package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.*
import dev.samhain.groceries.exception.GlobalExceptionHandler
import dev.samhain.groceries.service.MealService
import jakarta.persistence.EntityNotFoundException
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@ExtendWith(MockitoExtension::class)
class MealControllerTest {

    @Mock lateinit var mealService: MealService
    lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setUp() {
        mockMvc = MockMvcBuilders
            .standaloneSetup(MealController(mealService))
            .setControllerAdvice(GlobalExceptionHandler())
            .build()
    }

    @Test
    fun `GET api meals returns 200 with list of meal summaries`() {
        whenever(mealService.getAllMeals()).thenReturn(listOf(
            MealSummaryResponse(1L, "Pasta"),
            MealSummaryResponse(2L, "Salad")
        ))

        mockMvc.perform(get("/api/meals").accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].name").value("Pasta"))
            .andExpect(jsonPath("$[1].name").value("Salad"))
    }

    @Test
    fun `POST api meals returns 201 with created meal`() {
        whenever(mealService.createMeal(any())).thenReturn(
            MealDetailResponse(1L, "Pasta", emptyList())
        )

        mockMvc.perform(post("/api/meals")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Pasta"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.name").value("Pasta"))
            .andExpect(jsonPath("$.ingredients").isArray())
    }

    @Test
    fun `GET api meals id returns 200 with meal and ingredients`() {
        val ingredients = listOf(IngredientResponse(1L, "onions", "2", null, null))
        whenever(mealService.getMeal(1L)).thenReturn(MealDetailResponse(1L, "Pasta", ingredients))

        mockMvc.perform(get("/api/meals/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Pasta"))
            .andExpect(jsonPath("$.ingredients[0].name").value("onions"))
            .andExpect(jsonPath("$.ingredients[0].quantity").value("2"))
    }

    @Test
    fun `GET api meals unknown id returns 404 with error message`() {
        whenever(mealService.getMeal(99L)).thenThrow(EntityNotFoundException("Meal not found: 99"))

        mockMvc.perform(get("/api/meals/99"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Meal not found: 99"))
    }

    @Test
    fun `DELETE api meals id returns 204`() {
        mockMvc.perform(delete("/api/meals/1"))
            .andExpect(status().isNoContent())
    }

    @Test
    fun `PUT api meals id returns 200 with updated meal`() {
        whenever(mealService.updateMeal(any(), any())).thenReturn(
            MealDetailResponse(1L, "Ramen", emptyList())
        )

        mockMvc.perform(put("/api/meals/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"name":"Ramen"}"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Ramen"))
    }

    @Test
    fun `POST api meals id ingredients returns 201 with parsed ingredient`() {
        whenever(mealService.addIngredient(any(), any())).thenReturn(
            IngredientResponse(10L, "onions", "2", null, null)
        )

        mockMvc.perform(post("/api/meals/1/ingredients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"raw":"2 onions"}"""))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("onions"))
            .andExpect(jsonPath("$.quantity").value("2"))
    }

    @Test
    fun `GET api meals id ingredients unknown meal returns 404`() {
        whenever(mealService.getIngredients(99L)).thenThrow(EntityNotFoundException("Meal not found: 99"))

        mockMvc.perform(get("/api/meals/99/ingredients"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("Meal not found: 99"))
    }

    @Test
    fun `DELETE api meals id ingredients iid returns 204`() {
        mockMvc.perform(delete("/api/meals/1/ingredients/10"))
            .andExpect(status().isNoContent())
    }

    @Test
    fun `POST api meals consolidate returns 200 with merged ingredient list`() {
        whenever(mealService.consolidateIngredients(listOf(1L, 2L))).thenReturn(listOf(
            ConsolidatedIngredientResponse("onions", "5", null, null),
            ConsolidatedIngredientResponse("flour", "1 cup + 2 cups", null, null)
        ))

        mockMvc.perform(post("/api/meals/consolidate")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""[1,2]"""))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("onions"))
            .andExpect(jsonPath("$[0].quantity").value("5"))
            .andExpect(jsonPath("$[1].quantity").value("1 cup + 2 cups"))
    }

    @Test
    fun `illegal argument exception maps to 400 bad request`() {
        whenever(mealService.addIngredient(any(), any())).thenThrow(IllegalArgumentException("bad input"))

        mockMvc.perform(post("/api/meals/1/ingredients")
            .contentType(MediaType.APPLICATION_JSON)
            .content("""{"raw":""}"""))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.error").value("bad input"))
    }
}
