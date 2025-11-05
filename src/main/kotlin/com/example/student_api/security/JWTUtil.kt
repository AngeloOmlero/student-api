package com.example.student_api.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.io.Decoders
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JWTUtil(
    @param:Value("\${app.jwt.secret}") private val secret: String,
    @param:Value("\${app.jwt.expiration}") private val expiration: Long
) {

    private val key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))


    fun generateToken(userDetails: UserDetails): String {
        val role = userDetails.authorities.firstOrNull()?.authority ?: "ROLE_USER"
        val now = Date()
        val expiryDate = Date(now.time + expiration)

        return Jwts.builder()
            .subject(userDetails.username)
            .claim("role", role)
            .issuedAt(now)
            .expiration(expiryDate)
            .signWith(key)
            .compact()
    }

    fun extractUsername(token: String): String? = try {
        Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload.subject
    } catch (e: Exception) {
        null
    }

    fun validateToken(token: String): Boolean = try {
        val claims = Jwts.parser().verifyWith(key).build()
            .parseSignedClaims(token).payload
        !claims.expiration.before(Date())
    } catch (e: Exception) {
        false
    }
}