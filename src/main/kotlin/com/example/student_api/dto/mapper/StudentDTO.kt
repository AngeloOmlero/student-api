package com.example.student_api.dto.mapper

import com.example.student_api.model.Student


data class StudentDTO(
    val id: Long,
    val name: String,
    val email: String,
    val age: Int,
    val courseId: Long? = null,
    val courseName: String? = null
)

fun Student.toDTO(): StudentDTO =
    StudentDTO(
    id = this.id,
    name = this.name.trim(),
    email = this.email.trim().lowercase(),
    age = this.age,
    courseId = this.course?.id,
    courseName = this.course?.name
)