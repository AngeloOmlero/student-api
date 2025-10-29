package com.example.student_api.dto


import com.example.student_api.model.Course
import com.example.student_api.model.Student
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern

data class CreateStudentRequest(

    @field:NotBlank(message = "Name is required")
    @field:Pattern(
        regexp = "^[a-zA-Z\\s]+$",
        message = "Name must only contain letters and spaces"
    )
    val name: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Email must be valid")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z.-]+\\.[a-zA-Z]{2,6}$",
        message = "Email contains invalid characters"
    )
    val email : String,

    @field:Min(value = 1 , message = "Age should be greater than 1")
    val age: Int,

    @field:NotBlank(message = "Course name is required")
    val courseName: String

)

fun CreateStudentRequest.toCreate(course: Course) : Student =
    Student(
        name = this.name,
        email = this.email,
        age = this.age,
        course = course
    )

