package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.CreateOrderRequest
import dev.samhain.groceries.dto.OrderResponse
import dev.samhain.groceries.service.OrderService
import org.springframework.http.HttpStatus
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(private val orderService: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createOrder(@RequestBody request: CreateOrderRequest, auth: Authentication): OrderResponse =
        orderService.createOrder(request, auth.name.toLong())

    @GetMapping
    fun getRecentOrders(auth: Authentication): List<OrderResponse> =
        orderService.getRecentOrders(auth.name.toLong())
}
