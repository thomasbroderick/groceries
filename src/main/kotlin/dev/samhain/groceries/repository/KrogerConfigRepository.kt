package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.KrogerConfig
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface KrogerConfigRepository : JpaRepository<KrogerConfig, Long> {
    fun findByUserId(userId: Long): Optional<KrogerConfig>
}
