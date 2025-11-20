package com.example.student_api.dto.mapper

import com.example.student_api.model.Student
import java.time.LocalDateTime


data class StudentDTO(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val courseId: Long? = null,
    val courseName: String? = null,
    val createdAt: LocalDateTime? = null,
    val updatedAt: LocalDateTime? = null
)

fun Student.toDTO(): StudentDTO =
    StudentDTO(
    id = this.id,
    name = this.name.trim(),
    email = this.email.trim().lowercase(),
    age = this.age,
    courseId = this.course?.id,
    courseName = this.course?.name,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt



)