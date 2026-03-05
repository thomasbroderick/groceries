package dev.samhain.groceries.service

import dev.samhain.groceries.dto.*
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class KrogerApiService(
    private val krogerAuthService: KrogerAuthService,
    private val restClient: RestClient
) {
    companion object {
        private const val BASE_URL = "https://api.kroger.com/v1"
    }

    fun searchProducts(request: ProductSearchRequest, userId: Long): List<KrogerProductResponse> {
        val token = krogerAuthService.getValidClientToken(userId)
        val locationId = request.locationId

        val response = if (locationId != null) {
            restClient.get()
                .uri("$BASE_URL/products?filter.term={term}&filter.locationId={loc}&filter.limit=10", request.term, locationId)
                .header("Authorization", "Bearer $token")
                .retrieve()
                .body(KrogerProductApiResponse::class.java)
        } else {
            restClient.get()
                .uri("$BASE_URL/products?filter.term={term}&filter.limit=10", request.term)
                .header("Authorization", "Bearer $token")
                .retrieve()
                .body(KrogerProductApiResponse::class.java)
        }

        return response?.data?.map { product ->
            val item = product.items?.firstOrNull()
            KrogerProductResponse(
                productId = product.productId,
                description = product.description,
                upc = item?.upc ?: product.productId,
                price = item?.price?.regular,
                imageUrl = product.images
                    ?.firstOrNull { it.perspective == "front" }
                    ?.sizes?.firstOrNull { it.size == "thumbnail" }?.url
            )
        } ?: emptyList()
    }

    fun searchLocations(request: LocationSearchRequest, userId: Long): List<KrogerLocationResponse> {
        val token = krogerAuthService.getValidClientToken(userId)
        val response = restClient.get()
            .uri("$BASE_URL/locations?filter.zipCode.near={zip}&filter.limit=10", request.zipCode)
            .header("Authorization", "Bearer $token")
            .retrieve()
            .body(KrogerLocationApiResponse::class.java)

        return response?.data?.map { loc ->
            KrogerLocationResponse(
                locationId = loc.locationId,
                name = loc.name,
                address = loc.address?.addressLine1,
                city = loc.address?.city,
                state = loc.address?.state,
                zipCode = loc.address?.zipCode
            )
        } ?: emptyList()
    }

    fun addToCart(request: AddToCartRequest, userId: Long): String {
        val token = krogerAuthService.getValidUserToken(userId)
        val body = mapOf("items" to request.items.map { mapOf("upc" to it.upc, "quantity" to it.quantity) })

        restClient.put()
            .uri("$BASE_URL/cart/add")
            .header("Authorization", "Bearer $token")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .toBodilessEntity()

        return "Items added to cart"
    }
}

private data class KrogerProductApiResponse(val data: List<KrogerProductData>?)
private data class KrogerProductData(
    val productId: String,
    val description: String,
    val items: List<KrogerProductItem>?,
    val images: List<KrogerProductImage>?
)
private data class KrogerProductItem(val upc: String?, val price: KrogerProductPrice?)
private data class KrogerProductPrice(val regular: Double?)
private data class KrogerProductImage(val perspective: String, val sizes: List<KrogerImageSize>?)
private data class KrogerImageSize(val size: String, val url: String)

private data class KrogerLocationApiResponse(val data: List<KrogerLocationData>?)
private data class KrogerLocationData(val locationId: String, val name: String, val address: KrogerLocationAddress?)
private data class KrogerLocationAddress(
    val addressLine1: String?,
    val city: String?,
    val state: String?,
    val zipCode: String?
)
