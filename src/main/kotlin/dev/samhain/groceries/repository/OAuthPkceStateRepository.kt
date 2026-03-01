package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.OAuthPkceState
import org.springframework.data.jpa.repository.JpaRepository
import java.time.Instant

interface OAuthPkceStateRepository : JpaRepository<OAuthPkceState, String> {
    fun deleteByExpiresAtBefore(instant: Instant)
}
