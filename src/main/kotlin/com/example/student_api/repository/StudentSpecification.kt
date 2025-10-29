package com.example.student_api.repository

import com.example.student_api.model.Course
import com.example.student_api.model.Student
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.jpa.domain.Specification

data class StudentFilter(
    @field:Schema(description = "Filter by name", nullable = true)
    val name: String? = null,

    @field:Schema(description = "Filter by course name", nullable = true)
    val course: String? = null,

    @field:Schema(description = "Filter by age", nullable = true)
    val age: Int? = null,

    @field:Schema(description = "Filter by email", nullable = true)
    val email: String? = null
)

object StudentSpecification {

   fun filter( filter: StudentFilter): Specification<Student> =
       listOfNotNull(
           filter.name?.let { nameContains(it) },
           filter.age?.let { ageEquals(it) },
           filter.email?.let { emailEquals(it) },
           filter.course?.let { courseMatches(it) },
       ).reduceOrNull(Specification<Student>::and)?: Specification.allOf()

    private fun nameContains(name: String): Specification<Student> =
        Specification { root, _, builder ->
            builder.like(
                builder.lower(root.get("name")),
                "%${name.lowercase()}%"
            )
        }

    private fun courseMatches(value: String): Specification<Student> =
        Specification { root, _, builder ->
            val courseJoin = root.join<Student, Course>("course")
            builder.like(builder.lower(courseJoin.get("name")), "%${value.lowercase()}%")
        }

    private fun ageEquals(age: Int): Specification<Student> =
        Specification { root, _, builder ->
            builder.equal(root.get<Int>("age"), age)
        }

    private fun emailEquals(email: String): Specification<Student> =
        Specification { root, _, builder ->
            builder.equal(
                builder.lower(root.get("email")),
                email.lowercase()
            )
        }

}
