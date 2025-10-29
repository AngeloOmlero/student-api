package com.example.student_api.dto.mapper

import java.time.LocalDateTime

data class GenericResponse<T>(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val message: String,
    val data: T? = null
)