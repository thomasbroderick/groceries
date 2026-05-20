package dev.samhain.groceries.dto

data class RegisterRequest(val username: String, val password: String)
data class LoginRequest(val username: String, val password: String)
data class UserDto(val id: Long, val username: String, val canUseAi: Boolean)
data class AuthResponse(val token: String, val user: UserDto)
