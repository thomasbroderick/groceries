package dev.samhain.groceries.dto

data class CartItem(val upc: String, val quantity: Int)

data class AddToCartRequest(val items: List<CartItem>)

data class AddMealsToCartRequest(val mealIds: List<Long>)
