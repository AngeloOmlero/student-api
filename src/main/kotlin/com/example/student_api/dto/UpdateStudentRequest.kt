package com.example.student_api.dto

import com.example.student_api.model.Course
import com.example.student_api.model.Student
import com.example.student_api.model.Users
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class UpdateStudentRequest (
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    val email : String,

    @field:Min(value = 1 , message = "Age should be greater than 1")
    val age: Int,

    @field:NotBlank(message = "Course name is required")
    val courseName: String
)

fun UpdateStudentRequest.toUpdate(student: Student,course: Course): Student =
    student.copy(
        name = this.name,
        email = this.email,
        age = this.age,
        course = course
    )