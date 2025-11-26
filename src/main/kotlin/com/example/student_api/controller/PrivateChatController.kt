package com.example.student_api.controller

import com.example.student_api.dto.PrivateMessageDto
import com.example.student_api.dto.toDto
import com.example.student_api.service.MessageService
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class PrivateChatController(
    private val messageService: MessageService,
    private val messagingTemp: SimpMessagingTemplate
) {
    @MessageMapping("/chat.privateMessage")
    fun sendPrivateMessage(incoming: PrivateMessageDto){
        val message = messageService.savePrivateMessage(incoming)
        messageService.markAsDelivered(message.id)

        val dto = message.toDto()

        messagingTemp.convertAndSendToUser(
            incoming.receiver,
            "/queue/private",
            dto
        )

        messagingTemp.convertAndSendToUser(
            incoming.sender,
            "/queue/private",
            dto
        )

    }
}