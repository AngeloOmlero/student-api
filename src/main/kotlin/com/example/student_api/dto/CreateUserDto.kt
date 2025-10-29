package com.example.student_api.dto

data class CreateUserDto(
    val username: String,
    val password: String,
    val role: String

)
