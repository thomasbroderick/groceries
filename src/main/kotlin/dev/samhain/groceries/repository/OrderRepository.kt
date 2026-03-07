package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.Order
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<Order, Long> {
    fun findTop5ByUserIdOrderByCreatedAtDesc(userId: Long): List<Order>
}
