package com.example.student_api.config

import org.springframework.context.annotation.Configuration
import org.springframework.messaging.simp.config.ChannelRegistration
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocketMessageBroker
class WebSocketConfig(
    private val webSocketAuthChannelInterceptor: WebSocketAuthChannelInterceptor
): WebSocketMessageBrokerConfigurer {
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("http://localhost:5173", "http://127.0.0.1:5500", "http://localhost:3000", "http://localhost:63342")
            .withSockJS()
    }

    override fun configureMessageBroker(registry: MessageBrokerRegistry) {
        registry.enableSimpleBroker("/topic", "/queue")
        registry.setApplicationDestinationPrefixes("/app")
        registry.setUserDestinationPrefix("/user")
    }

    override fun configureClientInboundChannel(registration: ChannelRegistration) {
        registration.interceptors(webSocketAuthChannelInterceptor)
    }


}