package dev.samhain.groceries.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder
import org.springframework.security.web.SecurityFilterChain
import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec

@Configuration
@EnableWebSecurity
class SecurityConfig(@Value("\${app.jwt.secret}") secret: String) {

    private val keyBytes = MessageDigest.getInstance("SHA-256").digest(secret.toByteArray())

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/api/auth/**").permitAll()
                    .requestMatchers("/api/kroger/auth/callback").permitAll()
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            .oauth2ResourceServer { it.jwt { jwt -> jwt.decoder(jwtDecoder()) } }
            .headers { it.frameOptions { f -> f.sameOrigin() } }
        return http.build()
    }

    @Bean
    fun jwtDecoder(): JwtDecoder =
        NimbusJwtDecoder.withSecretKey(SecretKeySpec(keyBytes, "HmacSHA256")).build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
