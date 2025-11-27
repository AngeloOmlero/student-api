package com.example.student_api.dto

enum class PresenceStatus {
    ONLINE,
    OFFLINE
}

data class UserPresenceDto(
    val username: String,
    val status: PresenceStatus
)