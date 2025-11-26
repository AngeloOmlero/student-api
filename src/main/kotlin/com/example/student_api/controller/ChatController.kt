package com.example.student_api.controller

import com.example.student_api.dto.PrivateMessageDto
import com.example.student_api.dto.toDto
import com.example.student_api.service.MessageService
import org.springframework.data.domain.Page
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(private val messageService: MessageService) {
    @GetMapping("/messages/{otherUsername}")
    fun getConversation(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable otherUsername : String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): Page<PrivateMessageDto> {
        val messages = messageService.getConversationBetween(userDetails.username, otherUsername, page, size)
        messageService.markAllAsRead(userDetails.username,otherUsername)
        return messages.map { it.toDto()  }
    }

}