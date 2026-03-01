package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.*
import dev.samhain.groceries.service.KrogerApiService
import dev.samhain.groceries.service.KrogerAuthService
import dev.samhain.groceries.service.MealService
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.net.URI

@RestController
@RequestMapping("/api/kroger")
class KrogerController(
    private val krogerAuthService: KrogerAuthService,
    private val krogerApiService: KrogerApiService,
    private val mealService: MealService
) {

    @GetMapping("/auth/url")
    fun getAuthUrl(): AuthUrlResponse = krogerAuthService.generateAuthUrl()

    @GetMapping("/auth/callback")
    fun handleCallback(
        @RequestParam code: String,
        @RequestParam state: String
    ): ResponseEntity<Void> {
        krogerAuthService.handleCallback(code, state)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create("/settings?auth=success"))
            .build()
    }

    @PostMapping("/products/search")
    fun searchProducts(@RequestBody request: ProductSearchRequest): List<KrogerProductResponse> =
        krogerApiService.searchProducts(request)

    @PostMapping("/locations/search")
    fun searchLocations(@RequestBody request: LocationSearchRequest): List<KrogerLocationResponse> =
        krogerApiService.searchLocations(request)

    @PostMapping("/cart")
    fun addToCart(@RequestBody request: AddToCartRequest): Map<String, String> =
        mapOf("message" to krogerApiService.addToCart(request))

    @PostMapping("/cart/meals")
    fun addMealsToCart(@RequestBody request: AddMealsToCartRequest): Map<String, String> {
        val consolidated = mealService.consolidateIngredients(request.mealIds)
        val cartItems = consolidated
            .filter { it.krogerProductId != null }
            .map { CartItem(upc = it.krogerProductId!!, quantity = 1) }

        if (cartItems.isEmpty()) return mapOf("message" to "No Kroger products linked to ingredients")

        return mapOf("message" to krogerApiService.addToCart(AddToCartRequest(cartItems)))
    }
}
