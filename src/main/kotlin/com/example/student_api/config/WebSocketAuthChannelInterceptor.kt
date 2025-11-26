package com.example.student_api.config


import com.example.student_api.security.JWTUtil
import com.example.student_api.service.UserDetailsServiceImpl
import org.springframework.messaging.Message
import org.springframework.messaging.MessageChannel
import org.springframework.messaging.simp.stomp.StompCommand
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.messaging.support.ChannelInterceptor
import org.springframework.messaging.support.MessageHeaderAccessor
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Component
import org.slf4j.LoggerFactory

@Component
class WebSocketAuthChannelInterceptor(
    private val jwtUtil: JWTUtil,
    private val userDetailsService: UserDetailsServiceImpl
) : ChannelInterceptor {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun preSend(message: Message<*>, channel: MessageChannel): Message<*>? {
        val accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor::class.java)

        if (StompCommand.CONNECT == accessor?.command) {
            val authorizationHeader = accessor.getFirstNativeHeader("Authorization")

            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                val jwt = authorizationHeader.substring(7)

                return runCatching {
                    val username = jwtUtil.extractUsername(jwt)

                    if (username != null && jwtUtil.validateToken(jwt)) {
                        val userDetails = userDetailsService.loadUserByUsername(username)
                        val authentication = UsernamePasswordAuthenticationToken(
                            userDetails, null, userDetails.authorities
                        )
                        // CRITICAL FIX: Use accessor.setUser() to properly set the principal
                        accessor.setUser(authentication)
                        logger.info("STOMP CONNECT authenticated user: $username")
                        message
                    } else {
                        logger.warn("STOMP CONNECT rejected: JWT validation failed or username not found.")
                        null
                    }
                }.getOrElse { e ->
                    logger.error("STOMP CONNECT failed due to token processing error. Details: ${e.message}", e)
                    null
                }
            } else {
                logger.warn("STOMP CONNECT rejected: No valid Authorization header found.")
                return null
            }
        }
        return message
    }
}