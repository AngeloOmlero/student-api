package com.example.student_api.dto

data class UserDto(
    val id: Long,
    val username: String,
    val fullName: String,
    val role: String,
    var isOnline: Boolean = false // New field for online status
)
