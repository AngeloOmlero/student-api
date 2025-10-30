package com.example.student_api.service

import com.example.student_api.dto.AuthRequestDto
import com.example.student_api.dto.AuthResponseDto
import com.example.student_api.dto.CreateUserDto
import com.example.student_api.dto.UserDto
import com.example.student_api.model.Role
import com.example.student_api.model.Users
import com.example.student_api.repository.UserRepository
import com.example.student_api.security.JWTUtil
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JWTUtil,
    private val authenticationManager: AuthenticationManager
) {

    fun register(createUserDto: CreateUserDto): UserDto {
        // Default to USER role if the provided role is invalid or null
        val role = try {
            Role.valueOf(createUserDto.role.uppercase())
        } catch (e: Exception) {
            Role.USER
        }

        val user = Users(
            username = createUserDto.username,
            password = passwordEncoder.encode(createUserDto.password),
            role = role
        )
        val savedUser = userRepository.save(user)
        return UserDto(savedUser.id, savedUser.username, savedUser.role.name)
    }


    fun login(authRequestDto: AuthRequestDto): AuthResponseDto{
        return try {
            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authRequestDto.username, authRequestDto.password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val userDetails = authentication.principal as UserDetails
            val token = jwtUtil.generateToken(userDetails)

            AuthResponseDto(token)

        } catch (ex:BadCredentialsException) {
            throw BadCredentialsException("Invalid username or password")
        } catch (ex: Exception) {
            throw RuntimeException("Authentication failed: ${ex.message}")
        }
    }
}