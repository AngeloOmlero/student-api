package com.example.student_api.security

import io.jsonwebtoken.Claims
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

    private val signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))

    fun generateToken(user: UserDetails): String {
        val now = Date()
        val expiry = Date(now.time + expiration)

        return Jwts.builder()
            .subject(user.username)
            .issuedAt(now)
            .expiration(expiry)
            .signWith(signingKey)
            .compact()
    }

    fun extractUsername(token: String): String? =
        runCatching { parseClaims(token).subject }.getOrNull()

    fun validateToken(token: String): Boolean =
        runCatching {
            val claims = parseClaims(token)
            claims.expiration.after(Date())
        }.getOrDefault(false)

    private fun parseClaims(token: String): Claims {
        val jwt = token.removePrefix("Bearer ").trim()

        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(jwt)
            .payload
    }


}