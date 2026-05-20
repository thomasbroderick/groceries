package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.AppUser
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface AppUserRepository : JpaRepository<AppUser, Long> {
    fun findByUsername(username: String): Optional<AppUser>
    fun existsByUsername(username: String): Boolean
    fun existsByIdAndCanUseAiTrue(id: Long): Boolean
}
