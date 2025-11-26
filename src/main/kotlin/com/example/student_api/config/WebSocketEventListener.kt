package com.example.student_api.config

import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener {

    private val logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)

    @EventListener
    fun handleWebSocketConnectEvent(event: SessionConnectedEvent) {
        logger.info("Received a new websocket connection:" + event.message)

    }
    @EventListener
    fun handleWebSocketDisconnectEvent(event: SessionDisconnectEvent){
        val stompHeaderAccessor = StompHeaderAccessor.wrap(event.message)
        val username = stompHeaderAccessor.sessionAttributes!!["username"] as String?
        if (username != null) {
            logger.info("User disconnected: $username")
        }

    }

}