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
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service


@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JWTUtil,
    private val authenticationManager: AuthenticationManager,
    private val userDetailsService: UserDetailsServiceImpl,
    private val userPresenceService: UserPresenceService // Inject UserPresenceService
) {

    fun register(createUserDto: CreateUserDto): UserDto {
        userRepository.findByUsername(createUserDto.username)
             ?.let { throw IllegalArgumentException("Username '${createUserDto.username}' is already taken.") }

        val role = try {
            Role.valueOf(createUserDto.role.uppercase())
        } catch (e: Exception) {
            Role.USER
        }

        val user = Users(
            username = createUserDto.username,
            password = passwordEncoder.encode(createUserDto.password),
            fullName = createUserDto.fullName,
            role = role
        )
        val savedUser = userRepository.save(user)
        return UserDto(savedUser.id, savedUser.username, savedUser.fullName,savedUser.role.name, false) // New user is offline by default
    }
    fun getCurrentUser(username: String): UserDto {
        val user = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found: $username")
        return UserDto(user.id, user.username, user.fullName,user.role.name, userPresenceService.isUserOnline(user.username))
    }


    fun login(authRequestDto: AuthRequestDto): AuthResponseDto{
        return try {
            userDetailsService.loadUserByUsername(authRequestDto.username)

            val authentication = authenticationManager.authenticate(
                UsernamePasswordAuthenticationToken(authRequestDto.username, authRequestDto.password)
            )

            SecurityContextHolder.getContext().authentication = authentication

            val userDetails = authentication.principal as UserDetails
            val token = jwtUtil.generateToken(userDetails)

            AuthResponseDto(token)

        } catch (ex: Exception) {
            when (ex) {
                is BadCredentialsException -> throw BadCredentialsException("Invalid username or password")
                is UsernameNotFoundException -> throw UsernameNotFoundException("Invalid username: ${authRequestDto.username}")
                else -> throw RuntimeException("Authentication failed: ${ex.message}")
            }
        }
    }

    fun getAllUsers(): List<UserDto> {
        return userRepository.findAll().map {
            UserDto(it.id, it.username, it.fullName, it.role.name, userPresenceService.isUserOnline(it.username))
        }
    }
}