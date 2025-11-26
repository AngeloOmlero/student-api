package com.example.student_api.controller

import com.example.student_api.dto.UserDto
import com.example.student_api.service.UserService
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RequestMapping("/api/users")
@RestController
@CrossOrigin(origins = ["http://localhost:5173", "http://127.0.0.1:5500"])
class UserController (private val userService: UserService){
    @GetMapping
  fun getAllUsers(): List<UserDto> = userService.getAllUsers()

}