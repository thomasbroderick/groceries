package dev.samhain.groceries.repository

import dev.samhain.groceries.entity.KrogerConfig
import org.springframework.data.jpa.repository.JpaRepository

interface KrogerConfigRepository : JpaRepository<KrogerConfig, Long>
