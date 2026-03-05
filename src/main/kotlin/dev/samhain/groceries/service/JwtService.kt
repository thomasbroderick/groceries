package dev.samhain.groceries.service

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.MACSigner
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.security.MessageDigest
import java.time.Instant
import java.util.Date

@Service
class JwtService(
    @Value("\${app.jwt.secret}") secret: String,
    @Value("\${app.jwt.expiration-seconds:2592000}") private val expirationSeconds: Long
) {
    private val keyBytes = MessageDigest.getInstance("SHA-256").digest(secret.toByteArray())

    fun generateToken(userId: Long, username: String): String {
        val now = Instant.now()
        val claims = JWTClaimsSet.Builder()
            .subject(userId.toString())
            .claim("username", username)
            .issueTime(Date.from(now))
            .expirationTime(Date.from(now.plusSeconds(expirationSeconds)))
            .build()
        val jwt = SignedJWT(JWSHeader(JWSAlgorithm.HS256), claims)
        jwt.sign(MACSigner(keyBytes))
        return jwt.serialize()
    }
}
