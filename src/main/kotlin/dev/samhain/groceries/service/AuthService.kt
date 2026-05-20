package dev.samhain.groceries.service

import dev.samhain.groceries.dto.AuthResponse
import dev.samhain.groceries.dto.LoginRequest
import dev.samhain.groceries.dto.RegisterRequest
import dev.samhain.groceries.dto.UserDto
import dev.samhain.groceries.entity.AppUser
import jakarta.persistence.EntityNotFoundException
import dev.samhain.groceries.repository.AppUserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class AuthService(
    private val userRepository: AppUserRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder
) {
    fun register(req: RegisterRequest): AuthResponse {
        if (!req.username.matches(Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$"))) {
            throw IllegalArgumentException("Username must be a valid email address")
        }
        if (userRepository.existsByUsername(req.username)) {
            throw IllegalArgumentException("Username already taken")
        }
        val user = userRepository.save(AppUser(
            username = req.username,
            passwordHash = passwordEncoder.encode(req.password)!!
        ))
        val token = jwtService.generateToken(user.id, user.username)
        return AuthResponse(token = token, user = user.toDto())
    }

    fun login(req: LoginRequest): AuthResponse {
        val user = userRepository.findByUsername(req.username)
            .orElseThrow { IllegalArgumentException("Invalid credentials") }
        if (!passwordEncoder.matches(req.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid credentials")
        }
        val token = jwtService.generateToken(user.id, user.username)
        return AuthResponse(token = token, user = user.toDto())
    }

    @Transactional(readOnly = true)
    fun getCurrentUser(userId: Long): UserDto =
        userRepository.findById(userId)
            .orElseThrow { EntityNotFoundException("User not found: $userId") }
            .toDto()

    private fun AppUser.toDto() = UserDto(
        id = id,
        username = username,
        canUseAi = canUseAi
    )
}
