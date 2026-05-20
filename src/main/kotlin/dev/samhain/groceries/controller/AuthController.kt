package dev.samhain.groceries.controller

import dev.samhain.groceries.dto.AuthResponse
import dev.samhain.groceries.dto.LoginRequest
import dev.samhain.groceries.dto.RegisterRequest
import dev.samhain.groceries.dto.UserDto
import dev.samhain.groceries.service.AuthService
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val authService: AuthService) {

    @PostMapping("/register")
    fun register(@RequestBody req: RegisterRequest): AuthResponse = authService.register(req)

    @PostMapping("/login")
    fun login(@RequestBody req: LoginRequest): AuthResponse = authService.login(req)

    @GetMapping("/me")
    fun me(auth: Authentication): UserDto = authService.getCurrentUser(auth.name.toLong())
}
