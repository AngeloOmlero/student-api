package com.example.student_api.repository

import com.example.student_api.model.Course
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CourseRepository: JpaRepository<Course, Long>{
    fun findByNameIgnoreCase(name: String): Course?
}
