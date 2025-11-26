package com.example.student_api.dto

import com.example.student_api.model.Message

data class PrivateMessageDto(
    val id: Long? = null,
    val sender: String,
    val receiver: String,
    val content: String,
    val timestamp: Long
)

fun Message.toDto() = PrivateMessageDto(
    id = this.id,
    sender = this.sender.username,
    receiver = this.receiver.username,
    content = this.content,
    timestamp = this.createdAt.toEpochMilli()

)