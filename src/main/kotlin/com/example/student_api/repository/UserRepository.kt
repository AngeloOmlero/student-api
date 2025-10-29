package com.example.student_api.repository

import com.example.student_api.model.Users
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<Users,Long>{
    fun findByUsername(username:String): Users?
}