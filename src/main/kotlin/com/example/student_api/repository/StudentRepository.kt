package com.example.student_api.repository

import com.example.student_api.model.Student
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.stereotype.Repository

@Repository
interface StudentRepository : JpaRepository<Student, Long>, JpaSpecificationExecutor<Student>{
    fun findByName(name: String): Student?
}

