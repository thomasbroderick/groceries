package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.*
import dev.samhain.groceries.service.KrogerApiService
import dev.samhain.groceries.service.KrogerAuthService
import dev.samhain.groceries.service.MealService
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/kroger")
class KrogerController(
    private val krogerAuthService: KrogerAuthService,
    private val krogerApiService: KrogerApiService,
    private val mealService: MealService,
    @Value("\${app.base-url:/}") private val baseUrl: String
) {

    @GetMapping("/auth/url")
    fun getAuthUrl(auth: Authentication): AuthUrlResponse =
        krogerAuthService.generateAuthUrl(auth.name.toLong())

    @GetMapping("/auth/callback")
    fun handleCallback(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<Void> {
        krogerAuthService.handleCallback(code, state)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("${baseUrl}settings?auth=success"))
            .build()
    }

    @PostMapping("/products/search")
    fun searchProducts(@RequestBody request: ProductSearchRequest, auth: Authentication): List<KrogerProductResponse> =
        krogerApiService.searchProducts(request, auth.name.toLong())

    @PostMapping("/locations/search")
    fun searchLocations(@RequestBody request: LocationSearchRequest, auth: Authentication): List<KrogerLocationResponse> =
        krogerApiService.searchLocations(request, auth.name.toLong())

    @PostMapping("/cart")
    fun addToCart(@RequestBody request: AddToCartRequest, auth: Authentication): Map<String, String> =
        mapOf("message" to krogerApiService.addToCart(request, auth.name.toLong()))

    @PostMapping("/cart/meals")
    fun addMealsToCart(@RequestBody request: AddMealsToCartRequest, auth: Authentication): Map<String, String> {
        val userId = auth.name.toLong()
        val consolidated = mealService.consolidateIngredients(request.mealIds, userId)
        val cartItems = consolidated
            .filter { it.krogerProductId != null }
            .map { CartItem(upc = it.krogerProductId!!, quantity = 1) }

        if (cartItems.isEmpty()) return mapOf("message" to "No Kroger products linked to ingredients")

        return mapOf("message" to krogerApiService.addToCart(AddToCartRequest(cartItems), userId))
    }
}
