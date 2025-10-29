package com.example.student_api.controller

import com.example.student_api.dto.AuthRequestDto
import com.example.student_api.dto.AuthResponseDto
import com.example.student_api.dto.CreateUserDto
import com.example.student_api.dto.UserDto
import com.example.student_api.service.UserService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
) {

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    fun register(@RequestBody createUserDto: CreateUserDto): UserDto {
        return userService.register(createUserDto)
    }

    @PostMapping("/login")
    fun login(@RequestBody authRequestDto: AuthRequestDto): AuthResponseDto {
        return userService.login(authRequestDto)
    }
}