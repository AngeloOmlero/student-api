package com.example.student_api.service

import com.example.student_api.dto.CreateStudentRequest
import com.example.student_api.dto.mapper.StudentDTO
import com.example.student_api.dto.UpdateStudentRequest
import com.example.student_api.repository.StudentFilter
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


interface StudentService {
    fun save(request: CreateStudentRequest): StudentDTO
    fun getAll(filter: StudentFilter, pageable: Pageable): Page<StudentDTO>
    fun findById(id: Long): StudentDTO
    fun groupByCourse(): Map<String?, List<StudentDTO>>
    fun update(id:Long,request: UpdateStudentRequest) : StudentDTO
    fun delete(id:Long)
}