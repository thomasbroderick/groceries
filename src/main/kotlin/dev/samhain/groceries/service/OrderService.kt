package dev.samhain.groceries.service

import dev.samhain.groceries.dto.CreateOrderRequest
import dev.samhain.groceries.dto.OrderResponse
import dev.samhain.groceries.entity.Order
import dev.samhain.groceries.repository.AppUserRepository
import dev.samhain.groceries.repository.MealRepository
import dev.samhain.groceries.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OrderService(
    private val orderRepository: OrderRepository,
    private val mealRepository: MealRepository,
    private val userRepository: AppUserRepository
) {

    fun createOrder(request: CreateOrderRequest, userId: Long): OrderResponse {
        val mealNames = request.mealIds.mapNotNull { mealId ->
            mealRepository.findByIdAndUserId(mealId, userId).map { it.name }.orElse(null)
        }
        val order = Order(
            user = userRepository.getReferenceById(userId),
            mealNames = mealNames.toMutableList()
        )
        val saved = orderRepository.save(order)
        return OrderResponse(saved.id, saved.createdAt, saved.mealNames)
    }

    @Transactional(readOnly = true)
    fun getRecentOrders(userId: Long): List<OrderResponse> =
        orderRepository.findTop5ByUserIdOrderByCreatedAtDesc(userId)
            .map { OrderResponse(it.id, it.createdAt, it.mealNames) }
}
