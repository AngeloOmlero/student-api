package com.example.student_api.config

import com.example.student_api.dto.PresenceStatus
import com.example.student_api.dto.UserPresenceDto
import com.example.student_api.service.UserPresenceService
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(
    private val userPresenceService: UserPresenceService,
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)

    @EventListener
    fun handleWebSocketConnectEvent(event: SessionConnectedEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val username = headerAccessor.user?.name // Get username from authenticated principal

        if (username != null) {
            userPresenceService.userConnected(username)
            logger.info("User connected: $username. Online users: ${userPresenceService.getOnlineUsers()}")

            // Broadcast user online status
            val presenceUpdate = UserPresenceDto(username, PresenceStatus.ONLINE)
            messagingTemplate.convertAndSend("/topic/public.presence", presenceUpdate)
            logger.info("Broadcasted ONLINE status for user: $username")
        } else {
            logger.warn("User connected but username is null in headerAccessor.user for session: ${headerAccessor.sessionId}")
        }
    }

    @EventListener
    fun handleWebSocketDisconnectEvent(event: SessionDisconnectEvent) {
        val headerAccessor = StompHeaderAccessor.wrap(event.message)
        val username = headerAccessor.user?.name // Get username from authenticated principal

        if (username != null) {
            userPresenceService.userDisconnected(username)
            logger.info("User disconnected: $username. Online users: ${userPresenceService.getOnlineUsers()}")

            // Broadcast user offline status
            val presenceUpdate = UserPresenceDto(username, PresenceStatus.OFFLINE)
            messagingTemplate.convertAndSend("/topic/public.presence", presenceUpdate)
            logger.info("Broadcasted OFFLINE status for user: $username")
        } else {
            logger.warn("User disconnected but username is null in headerAccessor.user for session: ${headerAccessor.sessionId}")
        }
    }
}