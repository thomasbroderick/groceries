package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.GrantType
import dev.samhain.groceries.entity.KrogerToken
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface KrogerTokenRepository : JpaRepository<KrogerToken, Long> {
    fun findByGrantType(grantType: GrantType): Optional<KrogerToken>
}
