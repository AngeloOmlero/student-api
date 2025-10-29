package com.example.student_api.security

import com.example.student_api.service.UserDetailsServiceImpl
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthFilter(
    private val jwtUtil: JWTUtil,
    private val userDetailsService: UserDetailsServiceImpl
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            val token = authHeader?.takeIf { it.startsWith("Bearer ") }?.substring(7)

            if (token != null && SecurityContextHolder.getContext().authentication == null) {
                val username = jwtUtil.extractUsername(token)

                if (username != null && jwtUtil.validateToken(token)) {
                    val userDetails = userDetailsService.loadUserByUsername(username)
                    val auth = UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.authorities
                    )
                    auth.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = auth
                }
            }
        } catch (ex: Exception) {
            // Optionally log invalid token errors here
            logger.warn("JWT authentication failed: ${ex.message}")
        } finally {
            filterChain.doFilter(request, response)
        }
    }
}
