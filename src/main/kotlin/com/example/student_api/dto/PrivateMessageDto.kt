package com.example.student_api.dto

import com.example.student_api.model.Message

enum class MessageType {
    CHAT,
    TYPING,
    STOP_TYPING
}

data class PrivateMessageDto(
    val id: Long? = null,
    val sender: String,
    val receiver: String,
    val content: String,
    val timestamp: Long,
    val fileUrl: String? = null,
    val type: MessageType = MessageType.CHAT
)

fun Message.toDto() = PrivateMessageDto(
    id = this.id,
    sender = this.sender.username,
    receiver = this.receiver.username,
    content = this.content,
    timestamp = this.createdAt.toEpochMilli(),
    fileUrl = this.fileUrl,
    type = MessageType.CHAT // Default to CHAT for persisted messages
)