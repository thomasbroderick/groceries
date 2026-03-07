package dev.samhain.groceries.dto

import java.time.Instant

data class OrderResponse(val id: Long, val createdAt: Instant, val mealNames: List<String>)
data class CreateOrderRequest(val mealIds: List<Long>)
